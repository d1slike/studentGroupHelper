package ru.disdev.model.flows;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import ru.disdev.bot.MessageConst;
import ru.disdev.bot.TelegramBot;
import ru.disdev.model.Action;
import ru.disdev.model.StateActionMap;

import javax.annotation.PostConstruct;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;

public abstract class Flow<T> {

    @Autowired
    private TelegramBot telegramBot;
    private final StateActionMap stateActionMap = new StateActionMap();
    private final long chatId;
    private final ScheduledFuture<?> cancelTask;
    private T result;
    private int currentState;
    private Consumer<Message> currentConsumer;
    private Consumer<T> onFinish;

    public Flow(long chatId, ScheduledFuture<?> cancelTask) {
        result = buildResult();
        currentState = -1;
        this.chatId = chatId;
        this.cancelTask = cancelTask;
    }

    @PostConstruct
    private void postConstruct() {
        fillStateActions(stateActionMap);
    }

    protected final void toPreviousState() {
        currentState--;
        onUpdateState();
    }

    protected final void toNextState() {
        currentState++;
        onUpdateState();
    }

    public final void start() {
        if (currentState == -1) {
            toNextState();
        }
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

    public final synchronized void consume(Message message) {
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
        telegramBot.removeFlow(chatId);
        cancelTask.cancel(false);
        sendMessage("Отменено", getKeyboardAfterFinish());
    }

    protected synchronized void finish() {
        telegramBot.removeFlow(chatId);
        cancelTask.cancel(false);
        if (onFinish != null) {
            onFinish.accept(result);
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
