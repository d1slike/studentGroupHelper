package ru.disdev.commands;

import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import ru.disdev.TelegramBot;
import ru.disdev.bot.TelegramKeyBoards;

public class StartCommand extends BotCommand {

    public StartCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        TelegramBot bot = (TelegramBot) absSender;
        bot.sendMessage(chat.getId(), "Привет!", TelegramKeyBoards.defaultKeyBoard());
    }
}
