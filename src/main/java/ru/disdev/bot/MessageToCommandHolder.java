package ru.disdev.bot;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.bots.commands.CommandRegistry;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class MessageToCommandHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageToCommandHolder.class);

    @Autowired
    private CommandRegistry registry;
    @Autowired
    private ApplicationContext context;

    private Map<String, CmdArgPair> map = new HashMap<>();

    @PostConstruct
    private void init() {
        context.getBeansOfType(BotCommand.class).forEach((s, botCommand) -> registry.register(botCommand));
        FieldUtils.getFieldsListWithAnnotation(InputMessages.class, CommandMapping.class)
                .stream()
                .filter(field -> registry.getRegisteredCommand(field.getAnnotation(CommandMapping.class).command()) != null)
                .forEach(field -> {
                    CommandMapping mapping = field.getAnnotation(CommandMapping.class);
                    String command = mapping.command();
                    BotCommand registeredCommand = registry.getRegisteredCommand(command);
                    try {
                        String message = (String) FieldUtils.readDeclaredStaticField(InputMessages.class, field.getName());
                        map.put(message, new CmdArgPair(registeredCommand, mapping.arg()));
                    } catch (Exception ex) {
                        LOGGER.error("Error", ex);
                    }
                });
    }

    public CmdArgPair getCommand(String inputMessage) {
        return map.get(inputMessage);
    }

    public boolean contains(String inputMessage) {
        return map.containsKey(inputMessage);
    }

    public static class CmdArgPair {
        private final BotCommand cmd;
        private final String[] args;

        public CmdArgPair(BotCommand cmd, String[] args) {
            this.cmd = cmd;
            this.args = args;
        }

        public BotCommand getCmd() {
            return cmd;
        }

        public String[] getArgs() {
            return args;
        }
    }
}
