package ru.disdev.bot;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.bots.commands.CommandRegistry;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Component
public class CommandHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandHolder.class);

    private final CommandRegistry registry;
    private final ApplicationContext context;
    private final InputMessagesMapper inputMessagesMapper;
    private final Map<String, Method> map = new HashMap<>();

    @Autowired
    public CommandHolder(ApplicationContext context,
                         @Value("${telegram.bot.name}") String name,
                         InputMessagesMapper inputMessagesMapper) {
        this.context = context;
        registry = new CommandRegistry(false, name);
        this.inputMessagesMapper = inputMessagesMapper;
    }

    @PostConstruct
    private void init() {
        context.getBeansOfType(BotCommand.class).forEach((s, botCommand) -> registry.register(botCommand));
        MethodUtils.getMethodsListWithAnnotation(InputMessagesMapper.class, CommandMapping.class)
                .forEach(method -> {
                    method.setAccessible(true);
                    CommandMapping annotation = method.getAnnotation(CommandMapping.class);
                    map.put(annotation.message(), method);
                });

    }

    public boolean resolveTextMessage(String meesgae, User user, Chat chat) {
        if (!map.containsKey(meesgae)) {
            return false;
        }
        Method method = map.get(meesgae);
        try {
            method.invoke(inputMessagesMapper, user, chat);
            return true;
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.warn("Error while calling command", e);
            return false;
        }
    }

    public boolean resolveCommand(AbsSender sender, Message message) {
        return registry.executeCommand(sender, message);
    }

}
