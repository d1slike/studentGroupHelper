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
import ru.disdev.service.FileService;
import ru.disdev.util.VkUtils;

import java.io.IOException;

@Controller
@RequestMapping("/vk")
public class VkController {

    @Autowired
    private TelegramBot bot;
    @Autowired
    private VkApi api;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private FileService fileService;

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
            VkPost vkPost = VkUtils.handleNewPostBody(post);
            bot.announceToGroup(vkPost.getMessageText()); //TODO
            //api.sendMessage(null, VkUtils.wallAttachment(post.get("owner_id").asInt(), post.get("id").asInt()));
            if (!vkPost.getAttachments().isEmpty()) {
                fileService.collectVkAttachments(vkPost.getAttachments(), vkPost.getTag());
            }
        }
        return ResponseEntity.ok("ok");
    }

}
