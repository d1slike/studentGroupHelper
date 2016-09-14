package ru.disdev.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.api.objects.Message;
import ru.disdev.VkGroupBot;

import java.util.Map;
import java.util.function.Consumer;

public abstract class Flow<T> {

    @Autowired
    protected VkGroupBot bot;

    protected T result;

    private Map<Integer, Action> stateActionMap;
    private int currentState;
    private long chatId;
    private Consumer<Message> currentConsumer;
    private Runnable onFinish;

    public Flow(long chatId) {
        stateActionMap = getStateActions();
        result = getResult();
        currentState = -1;
        this.chatId = chatId;
    }

    public final void nextState() {
        currentState++;
        Action action = stateActionMap.get(currentState);
        if (action != null) {
            sendMessage(action.getInformationForUser());
            currentConsumer = action.getMessageConsumer();
        }
    }

    public abstract T getResult();

    public final void consume(Message message) {
        if (currentConsumer != null)
            currentConsumer.accept(message);
    }

    public final void sendMessage(String message) {
        bot.sendMessage(chatId, message);
    }

    public void finish() {
        onFinish.run();
    }

    ;

    public abstract Map<Integer, Action> getStateActions();

    public void setOnFinish(Runnable onFinish) {
        this.onFinish = onFinish;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
