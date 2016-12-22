package ru.disdev.model.flows;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import ru.disdev.bot.TelegramBot;
import ru.disdev.model.Action;
import ru.disdev.model.StateActionMap;

import java.util.function.Consumer;

public abstract class Flow<T> {

    @Autowired
    protected TelegramBot bot;

    protected final T result;
    private final StateActionMap stateActionMap = new StateActionMap();
    private final long chatId;
    private Runnable onDone;
    private int currentState;
    private Consumer<Message> currentConsumer;
    private Consumer<T> onFinish;

    public Flow(long chatId, Runnable onDone) {
        fillStateActions(stateActionMap);
        result = buildResult();
        currentState = -1;
        this.chatId = chatId;
        this.onDone = onDone;
    }

    public final void prevState() {
        currentState--;
        updateState();
    }

    public final void nextState() {
        currentState++;
        updateState();
    }

    private void updateState() {
        Action action = stateActionMap.get(currentState);
        if (action != null) {
            sendMessage(action.getInformationForUser(), action.getKeyBoard());
            currentConsumer = action.getMessageConsumer();
        }
    }

    protected abstract T buildResult();

    public final void consume(Message message) {
        if (currentConsumer != null) {
            currentConsumer.accept(message);
        }
    }

    protected final void sendMessage(String message) {
        bot.sendMessage(chatId, message);
    }

    protected final void sendMessage(String message, ReplyKeyboard keyboard) {
        bot.sendMessage(chatId, message, keyboard);
    }

    protected final void sendKeyboard(ReplyKeyboard keyboard) {
        bot.sendMessage(chatId, null, keyboard);
    }

    public synchronized final void cancel(ReplyKeyboard keyboard) {
        sendMessage("Отменено", keyboard);
        if (onDone != null) {
            onDone.run();
            onDone = null;
        }
    }

    protected synchronized void finish() {
        if (onFinish != null) {
            onFinish.accept(result);
        }
        if (onDone != null) {
            onDone.run();
            onDone = null;
        }
    }

    protected abstract StateActionMap fillStateActions(StateActionMap map);

    public final Flow<T> appendOnFinish(Consumer<T> handler) {
        if (onFinish == null) {
            onFinish = handler;
        } else
            onFinish = onFinish.andThen(handler);

        return this;
    }

}
