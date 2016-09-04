package ru.disdev.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import ru.disdev.Properties;
import ru.disdev.VkGroupBot;

public class HelloCommand extends BotCommand {

    @Autowired
    private Properties properties;

    public HelloCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (arguments.length == 0)
            return;
        String secret = arguments[0];
        if (secret.equals(properties.botSecretHello) && chat.isGroupChat()) {
            VkGroupBot bot = (VkGroupBot) absSender;
            if (bot.getActiveChatId() != -1) {
                bot.sendMessgae(chat.getId(), "Сначала нужно открепить меня от этого чата!");
                return;
            }
            bot.setActiveChatId(chat.getId());
            bot.announceToGroup("Привет!");
        }
    }
}
