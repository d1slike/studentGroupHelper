package ru.disdev.bot.commands.impl;

import ru.disdev.bot.commands.AbstractRequest;
import ru.disdev.bot.commands.CommandArgs;
import ru.disdev.bot.commands.Request;
import ru.disdev.model.Answer;

import static ru.disdev.bot.TelegramKeyBoards.*;

@Request(command = "/nav", args = "target")
public class NavigationCommand extends AbstractRequest {
    @Override
    protected Answer execute(CommandArgs args, long chatId, int userId) {
        if (args.size() == 0) {
            return Answer.empty();
        }
        switch (args.getString("target")) {
            case "events":
                return Answer.of(eventKeyboard());
            case "storage":
                return Answer.of(storageKeyboard());
            case "tt":
                return Answer.of(timeTableKeyboard());
            default:
                return Answer.of(mainKeyBoard());
        }
    }
}
