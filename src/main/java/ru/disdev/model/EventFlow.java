package ru.disdev.model;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.objects.Message;
import ru.disdev.entity.Event;

@Component
@Scope("prototype")
public class EventFlow extends Flow<Event> {

    public EventFlow(long chatId) {
        super(chatId);
    }

    @Override
    public String nextStateMessage() {
        switch (currentState) {
            case POST_INIT_STATE:
                return "Содержание события:\n";
            case 1:
                return "Введите дату и время события в формате dd.MM.YYYY";
            default:
                return null;
        }
    }

    @Override
    public void consume(Message message) {

    }

    @Override
    public void finish() {

    }

    @Override
    public int lastState() {
        return 0;
    }
}
