package ru.disdev.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import ru.disdev.TelegramBot;

import java.util.Map;
import java.util.function.Consumer;

public abstract class Flow<T> {

    @Autowired
    protected TelegramBot bot;

    protected T result;

    private Map<Integer, Action> stateActionMap;
    private int currentState;
    private long chatId;
    private Consumer<Message> currentConsumer;
    private Consumer<T> onFinish;

    public Flow(long chatId) {
        stateActionMap = getStateActions();
        result = buildResult();
        currentState = -1;
        this.chatId = chatId;
    }

    public final void nextState() {
        currentState++;
        Action action = stateActionMap.get(currentState);
        if (action != null) {
            sendMessage(action.getInformationForUser(), action.getKeyBoard());
            currentConsumer = action.getMessageConsumer();
        }
    }

    public abstract T buildResult();

    public final void consume(Message message) {
        if (currentConsumer != null)
            currentConsumer.accept(message);
    }

    public final void sendMessage(String message) {
        bot.sendMessage(chatId, message);
    }

    public final void sendMessage(String message, ReplyKeyboard keyboard) {
        bot.sendMessage(chatId, message, keyboard);
    }

    public final void sendKeyboard(ReplyKeyboard keyboard) {
        bot.sendMessage(chatId, null, keyboard);
    }

    public void finish() {
        if (onFinish != null) {
            onFinish.accept(result);
        }
    }

    public abstract Map<Integer, Action> getStateActions();

    public void appendOnFinish(Consumer<T> handler) {
        if (onFinish == null) {
            onFinish = handler;
        } else
            onFinish = onFinish.andThen(handler);
    }

    public void setResult(T result) {
        this.result = result;
    }
}
