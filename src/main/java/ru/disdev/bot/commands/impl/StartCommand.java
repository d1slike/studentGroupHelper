package ru.disdev.bot.commands.impl;

import ru.disdev.bot.TelegramKeyBoards;
import ru.disdev.bot.commands.AbstractRequest;
import ru.disdev.bot.commands.CommandArgs;
import ru.disdev.bot.commands.Request;
import ru.disdev.entity.Answer;

@Request(command = "/start")
public class StartCommand extends AbstractRequest {

    @Override
    public Answer execute(CommandArgs absSender) {
        return Answer.of("Привет!", TelegramKeyBoards.defaultKeyBoard());
    }
}
