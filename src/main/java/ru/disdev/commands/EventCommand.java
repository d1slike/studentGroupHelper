package ru.disdev.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import ru.disdev.Properties;
import ru.disdev.VkGroupBot;
import ru.disdev.entity.FlowType;
import ru.disdev.service.EventService;
import ru.disdev.util.EventUtils;

public class EventCommand extends BotCommand {

    @Autowired
    private Properties properties;

    @Autowired
    private EventService service;

    public EventCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        VkGroupBot bot = (VkGroupBot) absSender;
        if (arguments.length > 0) {
            String param = arguments[0];
            if (param.equals("new")) {
                if (!properties.botSuperusers.contains(user.getId()))
                    return;
                bot.startFlow(FlowType.EVENT, chat.getId());
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
