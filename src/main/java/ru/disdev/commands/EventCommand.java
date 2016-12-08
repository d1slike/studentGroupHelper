package ru.disdev.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import ru.disdev.Properties;
import ru.disdev.TelegramBot;
import ru.disdev.VkApi;
import ru.disdev.bot.TelegramKeyBoards;
import ru.disdev.model.flows.EventFlow;
import ru.disdev.service.EventService;
import ru.disdev.util.EventUtils;

public class EventCommand extends BotCommand {

    @Autowired
    private Properties properties;

    @Autowired
    private EventService service;
    @Autowired
    private VkApi vkApi;

    public EventCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        TelegramBot bot = (TelegramBot) absSender;
        if (arguments.length > 0) {
            String param = arguments[0];
            if (param.equals("new")) {
                if (!properties.botSuperusers.contains(user.getId())) {
                    return;
                }
                bot.startFlow(EventFlow.class, chat.getId()).appendOnFinish(o -> {
                    //vkApi.wallGroupPost(o.toString());
                    bot.announceToGroup(o.toString());
                    bot.sendMessage(chat.getId(), "Успешно!", TelegramKeyBoards.defaultKeyBoard());
                });
            } else if (param.equals("del")) {
                if (arguments.length < 2 || !properties.botSuperusers.contains(user.getId()))
                    return;
                String idInString = arguments[1];
                int id = Integer.parseInt(idInString);
                service.deleteById(id);
            }
        } else {
            bot.sendMessage(chat.getId(), EventUtils.formatList(service.findAll()));
        }
    }

}
