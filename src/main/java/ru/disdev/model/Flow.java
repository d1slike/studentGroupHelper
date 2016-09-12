package ru.disdev.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.api.objects.Message;
import ru.disdev.VkGroupBot;

public abstract class Flow<T extends Flowable> {

    public static final int POST_INIT_STATE = -1;

    @Autowired
    protected VkGroupBot bot;

    protected int currentState;
    protected long currentChat;
    protected T object;

    public Flow(long chatId) {
        currentChat = chatId;
        currentState = POST_INIT_STATE;
    }

    protected final void next() {
        String nextStateMessage = nextStateMessage();
        if (nextStateMessage != null) {
            bot.sendMessage(currentChat, nextStateMessage);
        }
        currentState++;
        if (currentState == lastState())
            finish();
    }

    public abstract String nextStateMessage();

    public abstract void consume(Message message);

    public abstract void finish();

    public abstract int lastState();
}
