package ru.disdev.bot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import ru.disdev.model.flows.Flow;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static ru.disdev.controller.TelegramBotApiController.TELEGRAM_WEBHOOK_PATH;

@Component
public class TelegramBot extends TelegramWebhookBot {

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
    @Value("${spring.profiles.active}")
    private String activeProfile;
    @Value("${server.external.url}")
    private String serverUrl;

    private Map<Long, Flow<?>> activeFlows = new ConcurrentHashMap<>();
    private Map<Long, ScheduledFuture<?>> cancelFlowTasks = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        try {
            setWebhook(serverUrl + TELEGRAM_WEBHOOK_PATH + "/" + botToken, null);
        } catch (TelegramApiRequestException e) {
            LOGGER.error("Error while initializing webhook", e);
        }
    }

    @Override
    public BotApiMethod onWebhookUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                final Message message = update.getMessage();
                boolean resolved = false;
                if (message.isCommand()) {
                    resolved = commandHolder.resolveCommand(this, message.getChatId(), message.getFrom().getId(), message.getText());
                }
                if (!resolved && message.hasText()) {
                    resolved = commandHolder.resolveTextMessage(this, message);
                }
                if (!resolved) {
                    Flow<?> flow = activeFlows.get(message.getChatId());
                    if (flow != null) {
                        flow.consume(message);
                    }
                }
                LOGGER.info(message.toString());
            }
        } catch (Exception ex) {
            LOGGER.error("Error while getting update", ex);
        }
        return null;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotPath() {
        return null;
    }

    public void announceToGroup(String message) {
        if (activeChatId != 0) {
            sendMessage(activeChatId, message);
        }
    }

    public void sendToMaster(String message) {
        if (masterChatId != 0) {
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
            message = MessageConst.OK_EMOJI;
        }

        SendMessage send = new SendMessage()
                .setChatId(chatId.toString())
                .setText(message)
                .enableHtml(withHtml)
                .enableNotification();
        if (keyboard != null) {
            send.setReplyMarkup(keyboard);
        }

        sendFormattedMessage(send);
    }

    public Message sendFormattedMessage(SendMessage sendMessage) {
        try {
            return sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            LOGGER.warn("Error while sending message", e);
        }
        return null;
    }

    public <T extends Flow<?>> T startFlow(Class<T> flowClass, long chatId) {
        if (activeFlows.remove(chatId) != null) {
            ScheduledFuture<?> future = cancelFlowTasks.remove(chatId);
            if (future != null) {
                future.cancel(false);
            }
        }
        Runnable cancel = () -> {
            activeFlows.remove(chatId);
            ScheduledFuture<?> future = cancelFlowTasks.remove(chatId);
            if (future != null && !future.isDone()) {
                future.cancel(false);
            }
        };
        T flow = context.getBean(flowClass, chatId, cancel);
        activeFlows.put(chatId, flow);
        ScheduledFuture<?> removeTask = executorService.schedule(flow::cancel, 5, TimeUnit.MINUTES);
        cancelFlowTasks.put(chatId, removeTask);
        flow.toNextState();
        return flow;
    }

    public long getActiveChatId() {
        return activeChatId;
    }

}
