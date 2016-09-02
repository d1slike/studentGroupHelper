package ru.disdev.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import ru.disdev.VkApi;
import ru.disdev.VkGroupBot;

import java.io.IOException;

@Controller
@RequestMapping("/vk")
public class VkController {

    @Autowired
    private VkGroupBot bot;
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
        }
        return ResponseEntity.ok("");
    }

    @RequestMapping(path = "/access", method = RequestMethod.GET)
    public void getAccessToken(@RequestParam("access_token") String token, @RequestParam("expires_in") int expire) {
        api.setAccessToken(token);
        api.scheduleNext(expire);
    }
}
