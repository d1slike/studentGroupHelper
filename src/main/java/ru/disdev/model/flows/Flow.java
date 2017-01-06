package ru.disdev.model.flows;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import ru.disdev.bot.MessageConst;
import ru.disdev.bot.TelegramBot;
import ru.disdev.model.Action;
import ru.disdev.model.StateActionMap;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

public abstract class Flow<T> {

    @Autowired
    private TelegramBot telegramBot;
    private final StateActionMap stateActionMap = new StateActionMap();
    private final long chatId;
    private T result;
    private Runnable onDone;
    private int currentState;
    private Consumer<Message> currentConsumer;
    private Consumer<T> onFinish;

    public Flow(long chatId, Runnable onDone) {
        result = buildResult();
        currentState = -1;
        this.chatId = chatId;
        this.onDone = onDone;
    }

    @PostConstruct
    private void postConstruct() {
        fillStateActions(stateActionMap);
    }

    public final void toPreviousState() {
        currentState--;
        onUpdateState();
    }

    public final void toNextState() {
        currentState++;
        onUpdateState();
    }

    protected final void jumpToState(int state) {
        currentState = state;
        onUpdateState();
    }

    protected final int getCurrentState() {
        return currentState;
    }

    private void onUpdateState() {
        Action action = stateActionMap.get(currentState);
        if (action != null) {
            sendMessage(action.getInformationForUser(), action.getKeyBoard());
            currentConsumer = action.getMessageConsumer();
        }
    }

    protected abstract T buildResult();

    public final void consume(Message message) {
        if (message.hasText()) {
            String text = message.getText();
            if (text.equals(MessageConst.CANCEL)) {
                cancel();
                return;
            }
        }
        if (currentConsumer != null) {
            currentConsumer.accept(message);
        }
    }

    protected final T getResult() {
        return result;
    }

    protected final void updateResult(T result) {
        this.result = result;
    }

    protected final void sendMessage(String message) {
        telegramBot.sendMessage(chatId, message);
    }

    protected final void sendMessage(String message, ReplyKeyboard keyboard) {
        telegramBot.sendMessage(chatId, message, keyboard);
    }

    private void sendKeyboard(ReplyKeyboard keyboard) {
        telegramBot.sendMessage(chatId, null, keyboard);
    }

    public synchronized void cancel() {
        if (onDone != null) {
            onDone.run();
            onDone = null;
        }
        sendMessage("Отменено", getKeyboardAfterFinish());
    }

    protected synchronized void finish() {
        if (onFinish != null) {
            onFinish.accept(result);
        }
        if (onDone != null) {
            onDone.run();
            onDone = null;
        }
        sendKeyboard(getKeyboardAfterFinish());
    }

    protected abstract StateActionMap fillStateActions(StateActionMap map);

    protected abstract ReplyKeyboard getKeyboardAfterFinish();

    public final Flow<T> appendOnFinish(Consumer<T> handler) {
        onFinish = onFinish == null ? handler : onFinish.andThen(handler);
        return this;
    }

}
