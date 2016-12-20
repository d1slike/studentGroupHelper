package ru.disdev.bot.commands.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import ru.disdev.api.VkApi;
import ru.disdev.bot.TelegramKeyBoards;
import ru.disdev.bot.commands.AbstractRequest;
import ru.disdev.bot.commands.CommandArgs;
import ru.disdev.bot.commands.Request;
import ru.disdev.entity.Answer;
import ru.disdev.model.flows.PostFlow;

import java.util.List;

@Request(command = "/post")
public class PostCommand extends AbstractRequest {

    @Autowired
    private VkApi vkApi;
    @Value("${telegram.bot.superusers}")
    public List<Integer> botSuperusers;

    @Override
    public Answer execute(CommandArgs absSender) {
        if (!botSuperusers.contains(user.getId())) {
            return;
        }
        bot.startFlow(PostFlow.class, chat.getId()).appendOnFinish(o -> {
            //vkApi.wallGroupPost(o.toString()); //TODO fix validation
            bot.announceToGroup(o.toString());
            bot.sendMessage(chat.getId(), "Успешно!", TelegramKeyBoards.defaultKeyBoard());
        });
    }
}
