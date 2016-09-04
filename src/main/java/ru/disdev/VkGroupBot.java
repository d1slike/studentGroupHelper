package ru.disdev;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.bots.commands.CommandRegistry;

import javax.annotation.PostConstruct;

@Component
public class VkGroupBot extends TelegramLongPollingBot {

    private static final Logger LOGGER = LogManager.getLogger(VkGroupBot.class);

    @Autowired
    private ApplicationContext context;
    @Autowired
    private CommandRegistry registry;
    @Autowired
    private Properties properties;

    private volatile long activeChatId;

    @PostConstruct
    public void init() {
        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            botsApi.registerBot(this);
            context.getBeansOfType(BotCommand.class).forEach((s, botCommand) -> registry.register(botCommand));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        activeChatId = -1;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            LOGGER.info(message.toString());
            if (message.isCommand())
                registry.executeCommand(this, message);
        }
    }

    @Override
    public String getBotUsername() {
        return properties.botName;
    }

    @Override
    public String getBotToken() {
        return properties.botToken;
    }

    public void announceToGroup(String message) {
        if (activeChatId == -1)
            return;
        sendMessgae(activeChatId, message);
    }

    public void sendMessgae(Long chatId, String messgae) {
        SendMessage send = new SendMessage();
        send.setChatId(chatId.toString())
                .setText(messgae)
                .enableNotification();
        try {
            sendMessage(send);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public long getActiveChatId() {
        return activeChatId;
    }

    public void setActiveChatId(long activeChatId) {
        this.activeChatId = activeChatId;
    }
}
