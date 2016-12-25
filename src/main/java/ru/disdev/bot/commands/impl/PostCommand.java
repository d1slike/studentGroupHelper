package ru.disdev.bot.commands.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.disdev.api.VkApi;
import ru.disdev.bot.TelegramBot;
import ru.disdev.bot.commands.AbstractRequest;
import ru.disdev.bot.commands.CommandArgs;
import ru.disdev.bot.commands.Request;
import ru.disdev.model.Answer;
import ru.disdev.model.SuperusersList;
import ru.disdev.model.flows.PostFlow;

@Request(command = "/post")
public class PostCommand extends AbstractRequest {

    @Autowired
    private VkApi vkApi;
    @Autowired
    private TelegramBot bot;
    @Autowired
    private SuperusersList superusersList;

    @Override
    public Answer execute(CommandArgs args, long chatId, int userId) {
        if (!superusersList.isSuperUser(userId)) {
            return Answer.of("Нет прав");
        }
        bot.startFlow(PostFlow.class, chatId).appendOnFinish(o -> {
            vkApi.wallGroupPost(o.toString());
            //bot.announceToGroup(o.toString());
            bot.sendMessage(chatId, "Успешно!");
        });

        return Answer.empty();
    }
}
