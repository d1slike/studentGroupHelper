package ru.disdev.bot.commands.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import ru.disdev.api.VkApi;
import ru.disdev.bot.TelegramBot;
import ru.disdev.bot.commands.AbstractRequest;
import ru.disdev.bot.commands.CommandArgs;
import ru.disdev.bot.commands.Request;
import ru.disdev.model.Answer;
import ru.disdev.model.flows.EventFlow;
import ru.disdev.service.EventService;
import ru.disdev.util.EventUtils;
import ru.disdev.util.IOUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

@Request(command = "/event", args = {"action", "id"})
public class EventCommand extends AbstractRequest {

    @Autowired
    private EventService service;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private VkApi vkApi;
    @Autowired
    private TelegramBot bot;

    public List<Integer> botSuperusers;

    @PostConstruct
    private void init() throws IOException {
        botSuperusers = mapper.readValue(IOUtils.resourceAsStream("/superusers.json"), new TypeReference<List<Integer>>() {
        });
    }

    @Override
    public Answer execute(CommandArgs args, long chatId, int userId) {
        if (args.size() > 0) {
            String param = args.getString("action");
            if (param.equals("new")) {
                if (!botSuperusers.contains(userId)) {
                    return Answer.of("Нет прав");
                }
                bot.startFlow(EventFlow.class, chatId).appendOnFinish(event -> {
                    //vkApi.wallGroupPost(event.toString());
                    service.addEvent(event);
                    bot.announceToGroup(event.toString());
                    bot.sendMessage(chatId, "Успешно!");
                });
            } else if (param.equals("del")) {
                if (args.size() < 2 || !botSuperusers.contains(userId)) {
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
