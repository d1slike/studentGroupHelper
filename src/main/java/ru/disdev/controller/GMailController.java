package ru.disdev.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.disdev.VkApi;
import ru.disdev.VkGroupBot;
import ru.disdev.entity.GMailMessage;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Base64;

@Controller
@RequestMapping("/gmail/get")
public class GMailController {

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private VkApi vkApi;
    @Autowired
    private VkGroupBot groupBot;


    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON)
    private ResponseEntity<String> getNotification(@RequestBody String json) {
        try {
            String content = mapper.readTree(json)
                    .get("message")
                    .get("data")
                    .asText();
            content = new String(Base64.getDecoder().decode(content));
            GMailMessage message = mapper.readValue(content, GMailMessage.class);
            groupBot.announceToGroup("На на почте новое сообщение от " + message.getEmailAddress());
            return ResponseEntity.ok("OK");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok("ERROR");
    }

    @RequestMapping("/")
    private ResponseEntity<String> get() {
        return ResponseEntity.ok("OK");
    }
}
