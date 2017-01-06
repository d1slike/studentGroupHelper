package ru.disdev.bot.commands.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.disdev.api.VkApi;
import ru.disdev.bot.TelegramBot;
import ru.disdev.bot.commands.AbstractRequest;
import ru.disdev.bot.commands.CommandArgs;
import ru.disdev.bot.commands.Request;
import ru.disdev.model.Answer;
import ru.disdev.model.flows.EventFlow;
import ru.disdev.service.EventService;
import ru.disdev.service.OptionsService;
import ru.disdev.util.EventUtils;

@Request(command = "/event", args = {"action", "id"})
public class EventCommand extends AbstractRequest {

    @Autowired
    private EventService service;
    @Autowired
    private VkApi vkApi;
    @Autowired
    private TelegramBot bot;
    @Autowired
    private OptionsService optionsService;
    @Autowired
    private PostCommand postCommand;

    @Override
    public Answer execute(CommandArgs args, long chatId, int userId) {
        if (args.size() > 0) {
            String param = args.getString("action");
            if (param.equals("new")) {
                if (!optionsService.isSuperUser(userId)) {
                    return Answer.of("Нет прав");
                }
                bot.startFlow(EventFlow.class, chatId).appendOnFinish(event -> {
                    service.addEvent(event);
                    postCommand.handlePost(event, chatId);
                });
            } else if (param.equals("del")) {
                if (args.size() < 2 || !optionsService.isSuperUser(userId)) {
                    return Answer.of("Нет прав");
                }
                boolean deleted = service.deleteById(args.getIntOrDefault("id", -1));
                return Answer.of(deleted ? "Удалено" : "Не удалено");
            }
        } else {
            return Answer.of(EventUtils.formatList(service.findAll()));
        }

        return Answer.empty();
    }

}
