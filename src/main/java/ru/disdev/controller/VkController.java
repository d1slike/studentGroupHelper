package ru.disdev.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import ru.disdev.Properties;
import ru.disdev.VkApi;
import ru.disdev.VkGroupBot;
import ru.disdev.entity.tokens.AccessResponse;
import ru.disdev.entity.tokens.AccessToken;
import ru.disdev.repository.TokenRepository;

import java.io.IOException;
import java.nio.charset.Charset;

@Controller
@RequestMapping("/vk")
public class VkController {

    private static final String GET_TOKEN_REQUEST = "https://oauth.vk.com/access_token?client_id=<client_id>&client_secret=<client_secret>&redirect_uri=<redirect_url>&code=<code>";
    private static final String GET_CODE_REQUEST_FOR_USER = "https://oauth.vk.com/authorize?client_id=<client_id>&display=page&redirect_uri=<redirect_url>&scope=<scope>&response_type=code&v=5.57";
    private static final String GET_CODE_REQUEST_FOR_GROUP = "https://oauth.vk.com/authorize?client_id=<client_id>&group_ids=<group_id>&display=page&redirect_uri=<redirect_url>&scope=<scope>&response_type=code&v=5.57";

    @Autowired
    private VkGroupBot bot;
    @Autowired
    private VkApi api;
    @Autowired
    private Properties properties;
    @Autowired
    private TokenRepository tokenRepository;

    private HttpClient client = HttpClientBuilder.create().build();

    private ObjectMapper mapper = new ObjectMapper();

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json;charset=UTF-8")
    public ResponseEntity<String> handleUpdate(@RequestBody String json) {
        JsonNode notification = null;
        try {
            notification = mapper.readValue(json, JsonNode.class);
        } catch (IOException ignored) {

        }
        if (notification == null)
            return ResponseEntity.status(500).body("Request body is not a json");
        if (notification.get("type").asText().equals("wall_post_new")) {
            JsonNode post = notification.get("object");
            bot.announceToGroup(buildAnnounce(post));
        }

        return ResponseEntity.ok("ok");
    }

    private String buildAnnounce(JsonNode post) {
        StringBuilder message = new StringBuilder("Новая запись в группе:\n")
                .append(post.get("text").asText());
        JsonNode attachments = post.get("attachments");
        if (attachments != null && attachments.size() > 0) {
            message.append("\nВложения:\n");
            attachments.forEach(jsonNode -> {
                String type = jsonNode.get("type").asText();
                if (type.equals("photo") || type.equals("doc") || type.equals("link")) {
                    JsonNode attachment = jsonNode.get(type);
                    if (attachment != null) {
                        String url = null;
                        String description = null;
                        switch (type) {
                            case "photo":
                                url = attachment.get("photo_2560").asText();
                                description = attachment.get("text").asText();
                                break;
                            case "doc":
                            case "link":
                                url = attachment.get("url").asText();
                                description = attachment.get("title").asText();
                                break;
                        }
                        message.append(url).append(" - ").append(description).append("\n");
                    }
                }

            });
        }

        return message.toString();
    }

    @RequestMapping(path = "/auth/user")
    private String authUser(@RequestParam String token) {
        if (!properties.apiToken.equals(token)) {
            return "redirect:https://vk.com/dev/authcode_flow_group?f=2.%20%D0%9E%D1%82%D0%BA%D1%80%D1%8B%D1%82%D0%B8%D0%B5%20%D0%B4%D0%B8%D0%B0%D0%BB%D0%BE%D0%B3%D0%B0%20%D0%B0%D0%B2%D1%82%D0%BE%D1%80%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D0%B8";
        }
        return "redirect:" +
                GET_CODE_REQUEST_FOR_USER.replace("<client_id>", properties.appId + "")
                        .replace("<redirect_url>", properties.appRedirect)
                        .replace("<scope>", "wall,offline");
    }

    @RequestMapping(path = "/auth/group")
    private String authGroup(@RequestParam String token) {
        if (!properties.apiToken.equals(token)) {
            return "redirect:https://vk.com/dev/authcode_flow_group?f=2.%20%D0%9E%D1%82%D0%BA%D1%80%D1%8B%D1%82%D0%B8%D0%B5%20%D0%B4%D0%B8%D0%B0%D0%BB%D0%BE%D0%B3%D0%B0%20%D0%B0%D0%B2%D1%82%D0%BE%D1%80%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D0%B8";
        }
        return "redirect:" +
                GET_CODE_REQUEST_FOR_USER.replace("<client_id>", properties.appId + "")
                        .replace("<redirect_url>", ""/*TODO change it*/)
                        .replace("<group_id>", properties.groupId + "")
                        .replace("<scope>", "messages,offline");
    }

    @RequestMapping(path = "/auth/user/callback")
    private ResponseEntity<String> makeUserAuth(@RequestParam String code) {
        try {
            AccessToken token = getToken(properties.appRedirect, code, AccessToken.USER_TOKEN_ID);
            tokenRepository.save(token);

            properties.vkUserAccessToken = token.getToken();
            properties.vkUserId = token.getId();
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.ok(e.getMessage());
        }
        return ResponseEntity.ok("OK");
    }

    @RequestMapping(path = "/auth/group/callback")
    private ResponseEntity<String> makeGroupAuth(@RequestParam String code) {
        try {
            AccessToken token = getToken(""/*TODO change it*/, code, AccessToken.GROUP_TOKEN_ID);
            tokenRepository.save(token);

            properties.vkGroupAccessToken = token.getToken();
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.ok(e.getMessage());
        }
        return ResponseEntity.ok("OK");
    }

    private AccessToken getToken(String redirectedFrom, String code, int tokenId) throws IOException {
        String url = GET_TOKEN_REQUEST.replace("<client_id>", properties.appId + "")
                .replace("<client_secret>", properties.appSecret)
                .replace("<redirect_url>", redirectedFrom)
                .replace("<code>", code);
        HttpGet get = new HttpGet(url);

        String json = IOUtils.toString(client.execute(get).getEntity().getContent(), Charset.forName("UTF-8"));
        AccessResponse response = mapper.readValue(json, AccessResponse.class);

        AccessToken token = new AccessToken();
        token.setId(tokenId);
        token.setUserId(response.getUserId());
        token.setToken(response.getAccessToken());
        return token;
    }
}
