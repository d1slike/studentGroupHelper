package ru.disdev;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.StringTokenizer;

@Component
public class VkGroupBot extends TelegramLongPollingBot {

    private static final Logger LOGGER = LogManager.getLogger(VkGroupBot.class);

    @Autowired
    private ApplicationContext context;
    @Autowired
    private CommandRegistry registry;
    @Autowired
    private Properties properties;
    @Autowired
    private BotCommand timeTableCommand;
    @Value("${telegram.bot.channel-chat-id}")
    private long activeChatId;

    @PostConstruct
    public void init() {
        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            botsApi.registerBot(this);
            context.getBeansOfType(BotCommand.class).forEach((s, botCommand) -> registry.register(botCommand));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                Message message = update.getMessage();
                LOGGER.info(message.toString());
                if (message.isCommand())
                    registry.executeCommand(this, message);
                else if (message.hasText()) {
                    String text = message.getText();
                    if (text.startsWith("TT")) {
                        StringTokenizer tokenizer = new StringTokenizer(text, ":");
                        tokenizer.nextToken();
                        String action = tokenizer.nextToken().trim();
                        String[] args = new String[0];
                        String arg = null;
                        switch (action) {
                            case "следующая пара":
                                arg = "next";
                                break;
                            case "пары сегодня":
                                break;
                            case "пары на завтра":
                                arg = "+1";
                                break;
                        }

                        if (arg != null) {
                            args = new String[1];
                            args[0] = arg;
                        }

                        timeTableCommand.execute(this, message.getFrom(), message.getChat(), args);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
        sendMessage(activeChatId, message);
    }

    public void sendMessage(Long chatId, String message) {
        SendMessage send = new SendMessage();
        send.setChatId(chatId.toString())
                .setText(message)
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

}
