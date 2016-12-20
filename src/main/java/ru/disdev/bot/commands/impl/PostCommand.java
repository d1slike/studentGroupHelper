package ru.disdev.bot.commands.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import ru.disdev.api.VkApi;
import ru.disdev.bot.TelegramBot;
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
    @Autowired
    private TelegramBot bot;
    @Value("${telegram.bot.superusers}")
    public List<Integer> botSuperusers;

    @Override
    public Answer execute(CommandArgs args, long chatId, int userId) {
        if (!botSuperusers.contains(userId)) {
            return Answer.of("Нет прав");
        }
        bot.startFlow(PostFlow.class, chatId).appendOnFinish(o -> {
            //vkApi.wallGroupPost(o.toString()); //TODO fix validation
            bot.announceToGroup(o.toString());
            bot.sendMessage(chatId, "Успешно!", TelegramKeyBoards.mainKeyBoard());
        });

        return Answer.empty();
    }
}
