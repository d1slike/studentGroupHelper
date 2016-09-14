package ru.disdev.model;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.objects.Message;
import ru.disdev.entity.Event;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Component
@Scope("prototype")
public class EventFlow extends Flow<Event> {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.YYYY hh:mm");

    public EventFlow(long chatId) {
        super(chatId);
    }

    @Override
    public Event getResult() {
        return new Event();
    }

    @Override
    public void finish() {
        super.finish();
    }

    private Consumer<Message> getTag() {
        return (message -> {
            if (message.hasText()) {
                String text = message.getText();
                result.setTag(text);
            } else
                sendMessage("Введите текст");
        });
    }

    private Consumer<Message> getInformation() {
        return message -> {
            if (message.hasText()) {
                String text = message.getText();
                result.setInformation(text);
            } else
                sendMessage("Введите текст");
        };
    }


    private Consumer<Message> getDateTime() {
        return message -> {
            if (message.hasText()) {
                try {
                    Date date = DATE_FORMAT.parse(message.getText());
                    LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.of("Europe/Moscow"));
                    result.setDate(localDateTime.toLocalDate());
                    result.setTime(localDateTime.toLocalTime());
                } catch (Exception ex) {
                    sendMessage("Неверный формат даты/времени. Используйте дд.ММ.ГГГГ чч.сс");
                }
            } else
                sendMessage("Введите текст");
        };
    }

    private Consumer<Message> getDateTimeToNotify() {
        return message -> {
            if (message.hasText()) {
                try {
                    Date date = DATE_FORMAT.parse(message.getText());
                    LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.of("Europe/Moscow"));
                    result.setNotificationDateTime(localDateTime);
                    finish();
                } catch (Exception ex) {
                    sendMessage("Неверный формат даты/времени. Используйте дд.ММ.ГГГГ чч.сс");
                }
            } else
                sendMessage("Введите текст");
        };
    }

    @Override
    public Map<Integer, Action> getStateActions() {
        Map<Integer, Action> map = new HashMap<>();
        map.put(0, new Action(getTag(), "Введите тег события"));
        map.put(1, new Action(getInformation(), "Введите текст события"));
        map.put(2, new Action(getDateTime(), "Введите дату/время события в формате: дд.ММ.ГГГГ чч.сс"));
        map.put(3, new Action(getDateTimeToNotify(), "Введите дату/вермя рассылки уведомления в формате: дд.ММ.ГГГГ чч.сс"));
        return map;
    }
}
