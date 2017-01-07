package ru.disdev.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.disdev.api.VkApi;
import ru.disdev.bot.TelegramBot;
import ru.disdev.entity.VkPost;
import ru.disdev.service.StorageService;
import ru.disdev.service.VkSubscriberService;
import ru.disdev.util.VkUtils;

import java.io.IOException;

import static java.util.Collections.singletonList;
import static ru.disdev.util.VkUtils.wallAttachment;

@Controller
@RequestMapping("/vk")
public class VkController {

    private static final String USER_ID_FIELD = "user_id";
    private static final String NEW_POST_TYPE = "wall_post_new";
    private static final String MESSAGE_ALLOW_TYPE = "message_allow";
    private static final String MESSAGE_DENY_TYPE = "message_deny";
    private static final String OBJECT_FIELD = "object";

    @Autowired
    private TelegramBot bot;
    @Autowired
    private VkApi vkApi;
    @Autowired
    private VkSubscriberService vkSubscriberService;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private StorageService storageService;

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json;charset=UTF-8")
    public ResponseEntity<String> handleUpdate(@RequestBody String json) {
        JsonNode notification = null;
        try {
            notification = mapper.readValue(json, JsonNode.class);
        } catch (IOException ignored) {

        }
        if (notification == null)
            return ResponseEntity.status(500).body("Request body is not a json");
        String type = notification.get("type").asText();
        switch (type) {
            case NEW_POST_TYPE:
                JsonNode post = notification.get(OBJECT_FIELD);
                VkPost vkPost = VkUtils.handleNewPostBody(post);
                bot.sendMessage(bot.getActiveChatId(), vkPost.getMessageText(), true);
                vkApi.sendMessage(null, singletonList(wallAttachment(post.get("owner_id").asInt(), post.get("id").asInt())));
                if (!vkPost.getAttachments().isEmpty()) {
                    storageService.collectVkAttachments(vkPost.getAttachments(), vkPost.getTag());
                }
                break;
            case MESSAGE_ALLOW_TYPE: {
                JsonNode object = notification.get(OBJECT_FIELD);
                int userId = object.get(USER_ID_FIELD).asInt();
                vkSubscriberService.addSubscriber(userId);
                break;
            }
            case MESSAGE_DENY_TYPE: {
                JsonNode object = notification.get(OBJECT_FIELD);
                int userId = object.get(USER_ID_FIELD).asInt();
                vkSubscriberService.deleteSubscriber(userId);
                break;
            }
        }
        return ResponseEntity.ok("ok");
    }

}
