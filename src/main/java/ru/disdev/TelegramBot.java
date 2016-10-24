package ru.disdev;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.bots.commands.CommandRegistry;
import ru.disdev.entity.FlowType;
import ru.disdev.model.Flow;
import ru.disdev.util.TelegramBotUtils;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private static final Logger LOGGER = LogManager.getLogger(TelegramBot.class);

    @Autowired
    private ApplicationContext context;
    @Autowired
    private CommandRegistry registry;
    @Autowired
    private Properties properties;
    @Autowired
    private BotCommand timeTableCommand;
    @Autowired
    private BotCommand eventCommand;
    @Autowired
    private ScheduledExecutorService executorService;
    @Value("${telegram.bot.channel-chat-id}")
    private long activeChatId;

    private Map<Long, Flow<?>> activeFlows = new ConcurrentHashMap<>();
    private Map<Long, ScheduledFuture<?>> removeFlowTasks = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            botsApi.registerBot(this);
            context.getBeansOfType(BotCommand.class).forEach((s, botCommand) -> registry.register(botCommand));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                final Message message = update.getMessage();
                final Chat chat = message.getChat();
                LOGGER.info(message.toString());
                if (message.isCommand())
                    registry.executeCommand(this, message);
                else if (message.hasText()) {
                    String text = message.getText();
                    if (text.startsWith("Пары:")) {
                        String action = TelegramBotUtils.getCommandArg(text, ":");
                        String[] args = new String[0];
                        String arg = null;
                        switch (action) {
                            case "следующая пара":
                                arg = "next";
                                break;
                            case "сегодня":
                                break;
                            case "на завтра":
                                arg = "+1";
                                break;
                            case "на неделю":
                                arg = "week";
                                break;
                        }

                        if (arg != null) {
                            args = new String[1];
                            args[0] = arg;
                        }

                        timeTableCommand.execute(this, message.getFrom(), chat, args);
                    } else if (text.startsWith("События:")) {
                        //String action = TelegramBotUtils.getCommandArg(text, ":");
                        eventCommand.execute(this, message.getFrom(), chat, new String[]{});
                    } else {
                        Flow<?> flow = activeFlows.get(chat.getId());
                        if (flow != null)
                            flow.consume(message);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return properties.botName;
    }

    @Override
    public String getBotToken() {
        return properties.botToken;
    }

    public void announceToGroup(String message) {
        if (activeChatId == -1)
            return;
        sendMessage(activeChatId, message);
    }

    public void sendMessage(Long chatId, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        SendMessage send = new SendMessage();
        send.setChatId(chatId.toString())
                .setText(message)
                .enableNotification();
        try {
            sendMessage(send);
        } catch (TelegramApiException e) {
            LOGGER.warn("Error while sending message", e);
        }
    }

    public void setKeyBoard(Long chatId, ReplyKeyboard keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString())
                .setReplyMarkup(keyboard)
                .setText("-");
        try {
            sendMessage(message);
        } catch (TelegramApiException e) {
            LOGGER.warn("Error while sending message", e);
        }
    }

    public Flow<?> startFlow(FlowType type, long chatId) {
        Flow<?> flow = (Flow<?>) context.getBean(type.name().toLowerCase() + "Flow", chatId);
        flow.appendOnFinish(o -> {
            activeFlows.remove(chatId);
            ScheduledFuture<?> cancelTask = removeFlowTasks.get(chatId);
            if (cancelTask != null && !cancelTask.isDone()) {
                cancelTask.cancel(false);
            }
        });
        activeFlows.put(chatId, flow);
        ScheduledFuture<?> removeTask = executorService.schedule(() -> {
            activeFlows.remove(chatId);
            removeFlowTasks.remove(chatId);
        }, 5, TimeUnit.MINUTES);
        removeFlowTasks.put(chatId, removeTask);
        flow.nextState();
        return flow;
    }

    public long getActiveChatId() {
        return activeChatId;
    }

}
