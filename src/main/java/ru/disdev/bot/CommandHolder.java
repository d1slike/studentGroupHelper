package ru.disdev.bot;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.objects.Message;
import ru.disdev.bot.commands.AbstractRequest;
import ru.disdev.bot.commands.Request;
import ru.disdev.model.Answer;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

@Component
public class CommandHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandHolder.class);

    private final ApplicationContext context;
    private final InputMessagesMapper inputMessagesMapper;
    private final Map<String, Method> textMessageMap = new HashMap<>();
    private final Map<String, AbstractRequest> commandMap = new HashMap<>();

    @Autowired
    public CommandHolder(ApplicationContext context) {
        this.context = context;
        this.inputMessagesMapper = new InputMessagesMapper(this);
    }

    @PostConstruct
    private void init() {
        context.getBeansOfType(AbstractRequest.class).forEach((name, abstractRequest) -> {
            Request request = abstractRequest.getClass().getAnnotation(Request.class);
            commandMap.put(request.command(), abstractRequest);
        });
        MethodUtils.getMethodsListWithAnnotation(InputMessagesMapper.class, CommandMapping.class)
                .forEach(method -> {
                    method.setAccessible(true);
                    CommandMapping annotation = method.getAnnotation(CommandMapping.class);
                    textMessageMap.put(annotation.message(), method);
                });

    }

    public boolean resolveTextMessage(TelegramBot sender, Message message) {
        if (!textMessageMap.containsKey(message.getText())) {
            return false;
        }
        Method method = textMessageMap.get(message.getText());
        try {
            method.invoke(inputMessagesMapper, sender, message.getFrom(), message.getChat());
            return true;
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.warn("Error while calling command", e);
            return false;
        }
    }

    public boolean resolveCommand(TelegramBot telegramBot, long chatId, int userId, String command) {
        StringTokenizer tokenizer = new StringTokenizer(command, " ");
        String cmd = tokenizer.nextToken();
        if (commandMap.containsKey(cmd)) {
            Answer answer = commandMap.get(cmd).execute(command, chatId, userId);
            if (answer != null && answer != Answer.empty()) {
                telegramBot.sendMessage(chatId, answer.getText(), answer.getKeyboard(), answer.isWithHtml());
            }
            return true;
        }
        return false;
    }

}
