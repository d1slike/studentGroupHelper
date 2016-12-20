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
import ru.disdev.model.flows.EventFlow;
import ru.disdev.service.EventService;
import ru.disdev.util.EventUtils;

import java.util.List;

@Request(command = "/event", args = {"action", "id"})
public class EventCommand extends AbstractRequest {

    @Autowired
    private EventService service;
    @Autowired
    private VkApi vkApi;
    @Autowired
    private TelegramBot bot;
    @Value("${telegram.bot.superusers}")
    public List<Integer> botSuperusers;

    @Override
    public Answer execute(CommandArgs args, long chatId, int userId) {
        if (args.size() > 0) {
            String param = args.getString("action");
            if (param.equals("new")) {
                if (!botSuperusers.contains(userId)) {
                    return Answer.of("Нет прав");
                }
                bot.startFlow(EventFlow.class, chatId).appendOnFinish(o -> {
                    //vkApi.wallGroupPost(o.toString());
                    bot.announceToGroup(o.toString());
                    bot.sendMessage(chatId, "Успешно!", TelegramKeyBoards.eventKeyboard());
                });
            } else if (param.equals("del")) {
                if (args.size() < 2 || !botSuperusers.contains(userId)) {
                    return Answer.of("Нет прав");
                }
                service.deleteById(args.getIntOrDefault("id", -1));
            }
        } else {
            return Answer.of(EventUtils.formatList(service.findAll()));
        }

        return Answer.empty();
    }

}
