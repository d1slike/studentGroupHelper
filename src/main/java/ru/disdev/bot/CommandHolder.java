package ru.disdev.bot;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.bots.commands.CommandRegistry;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class CommandHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandHolder.class);

    private CommandRegistry registry;
    private final ApplicationContext context;

    private Map<String, CmdArgPair> map = new HashMap<>();

    @Autowired
    public CommandHolder(ApplicationContext context,
                         @Value("${telegram.bot.name}") String name) {
        this.context = context;
        registry = new CommandRegistry(false, name);
    }

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
                        String message = (String) field.get(null);
                        map.put(message, new CmdArgPair(registeredCommand, mapping.args()));
                    } catch (Exception ex) {
                        LOGGER.error("Error", ex);
                    }
                });
    }

    public CmdArgPair resolveTextMessage(String inputMessage) {
        return map.get(inputMessage);
    }

    public boolean containsTextCommand(String inputMessage) {
        return map.containsKey(inputMessage);
    }

    public void handleCommand(AbsSender sender, Message message) {
        registry.executeCommand(sender, message);
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
