package ru.disdev.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.telegram.telegrambots.api.objects.Update;
import ru.disdev.bot.TelegramBot;

@Controller
public class TelegramBotApiController {

    public static final String TELEGRAM_WEBHOOK_PATH = "/receive_update";
    @Autowired
    private TelegramBot telegramBot;

    @RequestMapping(value = TELEGRAM_WEBHOOK_PATH + "/{token}", method = RequestMethod.POST)
    public ResponseEntity<String> receiveUpdate(@PathVariable String token, @RequestBody Update update) {
        telegramBot.onWebhookUpdateReceived(update);
        return ResponseEntity.ok("ok");
    }
}
