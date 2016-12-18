package ru.disdev.bot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import ru.disdev.model.flows.Flow;

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
    private ScheduledExecutorService executorService;
    @Autowired
    private ApplicationContext context;
    @Autowired
    private CommandHolder commandHolder;

    @Value("${telegram.bot.channel-chat-id}")
    private long activeChatId;
    @Value("${telegram.bot.token}")
    private String botToken;
    @Value("${telegram.bot.name}")
    private String botName;
    @Value("${telegram.bot.master-chat-id}")
    private long masterChatId;

    private Map<Long, Flow<?>> activeFlows = new ConcurrentHashMap<>();
    private Map<Long, ScheduledFuture<?>> removeFlowTasks = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        TelegramBotsApi botsApi = new TelegramBotsApi();
        try {
            botsApi.registerBot(this);
        } catch (TelegramApiException e) {
            LOGGER.error("Error while registering bot", e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                final Message message = update.getMessage();
                final Chat chat = message.getChat();
                if (message.isCommand()) {
                    commandHolder.handleCommand(this, message);
                } else if (message.hasText()) {
                    String text = message.getText();
                    if (commandHolder.containsTextCommand(text)) {
                        CommandHolder.CmdArgPair command = commandHolder.resolveTextMessage(text);
                        command.getCmd().execute(this, message.getFrom(), chat, command.getArgs());
                    } else {
                        Flow<?> flow = activeFlows.get(chat.getId());
                        if (flow != null) {
                            flow.consume(message);
                        }
                    }
                }
                LOGGER.info(message.toString());
            }
        } catch (Exception ex) {
            LOGGER.error("Error while getting update", ex);
        }

    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    public void announceToGroup(String message) {
        if (activeChatId != 0) {
            sendMessage(activeChatId, message);
        }
    }

    public void sendToMaster(String message) {
        if (masterChatId == 0) {
            sendMessage(masterChatId, message);
        }
    }

    public void sendMessage(Long chatId, String message) {
        sendMessage(chatId, message, null, false);
    }


    public void sendMessage(Long chatId, String message, boolean withHtml) {
        sendMessage(chatId, message, null, withHtml);
    }

    public void sendMessage(Long chatId, String message, ReplyKeyboard keyboard) {
        sendMessage(chatId, message, keyboard, false);
    }

    public void sendMessage(Long chatId, String message, ReplyKeyboard keyboard, boolean withHtml) {
        if (message == null && keyboard == null) {
            return;
        }

        if (keyboard != null && message == null) {
            message = "~";
        }

        SendMessage send = new SendMessage()
                .setChatId(chatId.toString())
                .setText(message)
                .enableHtml(withHtml)
                .enableNotification();
        if (keyboard != null) {
            send.setReplyMarkup(keyboard);
        }
        try {
            sendMessage(send);
        } catch (TelegramApiException e) {
            LOGGER.warn("Error while sending message", e);
        }
    }

    public <T extends Flow<?>> T startFlow(Class<T> flowClass, long chatId) {
        T flow = context.getBean(flowClass, chatId);
        flow.appendOnFinish(o -> {
            activeFlows.remove(chatId);
            ScheduledFuture<?> cancelTask = removeFlowTasks.remove(chatId);
            if (cancelTask != null && !cancelTask.isDone()) {
                cancelTask.cancel(false);
            }
        });
        activeFlows.put(chatId, flow);
        ScheduledFuture<?> removeTask = executorService.schedule(() -> {
            activeFlows.remove(chatId);
            removeFlowTasks.remove(chatId);
            sendMessage(chatId, "Отменено", TelegramKeyBoards.defaultKeyBoard());
        }, 5, TimeUnit.MINUTES);
        removeFlowTasks.put(chatId, removeTask);
        flow.nextState();
        return flow;
    }

    public long getActiveChatId() {
        return activeChatId;
    }

}
