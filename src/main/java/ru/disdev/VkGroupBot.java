package ru.disdev;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import javax.annotation.PostConstruct;

@Component
public class VkGroupBot extends TelegramLongPollingBot {

    @PostConstruct
    public void register() {
        TelegramBotsApi botsApi = new TelegramBotsApi();
        try {
            botsApi.registerBot(this);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {

    }

    @Override
    public String getBotUsername() {
        return "idb_14_09_bot";
    }

    @Override
    public String getBotToken() {
        return "249462433:AAEDst0a5zSsLi80-uSe-k2HeGCHutbyBXw";
    }

    public void sendMessage() {

    }
}
