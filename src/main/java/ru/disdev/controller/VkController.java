package ru.disdev.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.disdev.TelegramBot;
import ru.disdev.VkApi;

import java.io.IOException;

@Controller
@RequestMapping("/vk")
public class VkController {

    @Autowired
    private TelegramBot bot;
    @Autowired
    private VkApi api;

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
            bot.announceToGroup(buildAnnounce(post)); //TODO
            //api.sendMessage(null, VkUtils.wallAttachment(post.get("owner_id").asInt(), post.get("id").asInt()));
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

}
