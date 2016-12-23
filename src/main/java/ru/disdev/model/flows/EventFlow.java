package ru.disdev.model.flows;

import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import ru.disdev.bot.TelegramKeyBoards;
import ru.disdev.entity.Event;
import ru.disdev.entity.Prototype;
import ru.disdev.model.Action;
import ru.disdev.model.StateActionMap;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Consumer;

@Prototype
public class EventFlow extends AbstractPostFlow<Event> {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd MM yyyy HH mm");

    private boolean canBeFinished;

    public EventFlow(long chatId, Runnable onDone) {
        super(chatId, onDone);
        canBeFinished = false;
    }

    @Override
    public Event buildResult() {
        return new Event();
    }

    @Override
    public void finish() {
        if (!canBeFinished) {
            nextState();
            return;
        }
        super.finish();
    }

    @Override
    protected StateActionMap fillStateActions(StateActionMap map) {
        return super.fillStateActions(map)
                .next(new Action(getDateTime(), "Введите дату/время события в формате: дд ММ ГГГГ чч мм"))
                .next(new Action(getDateTimeToNotify(), "Введите дату/вермя рассылки уведомления в формате: дд ММ ГГГГ чч мм"));
    }

    private Consumer<Message> getDateTime() {
        return message -> {
            if (message.hasText()) {
                try {
                    Date date = DATE_FORMAT.parse(message.getText());
                    LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                    if (localDateTime.isBefore(LocalDateTime.now())) {
                        sendMessage("Указанное время уже наступило");
                        return;
                    }
                    result.setDate(localDateTime.toLocalDate());
                    result.setTime(localDateTime.toLocalTime());
                    nextState();
                } catch (Exception ex) {
                    sendMessage("Неверный формат даты/времени. Используйте дд ММ ГГГГ чч мм");
                }
            } else
                sendMessage("Введите текст");
        };
    }

    private Consumer<Message> getDateTimeToNotify() {
        return message -> {
            if (message.hasText()) {
                try {
                    String text = message.getText();
                    if (!text.equals("-")) {
                        Date date = DATE_FORMAT.parse(message.getText());
                        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                        if (localDateTime.isBefore(LocalDateTime.now())) {
                            sendMessage("Указанное время уже наступило");
                            return;
                        }
                        result.setNotificationDateTime(localDateTime);
                    }
                    canBeFinished = true;
                    finish();
                } catch (Exception ex) {
                    sendMessage("Неверный формат даты/времени. Используйте дд ММ ГГГГ чч мм");
                }
            } else
                sendMessage("Введите текст");
        };
    }

    @Override
    protected ReplyKeyboard getKeyboardAfterFinish() {
        return TelegramKeyBoards.eventKeyboard();
    }
}
