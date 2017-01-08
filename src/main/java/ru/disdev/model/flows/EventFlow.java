package ru.disdev.model.flows;

import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import ru.disdev.bot.MessageConst;
import ru.disdev.bot.TelegramKeyBoards;
import ru.disdev.entity.post.Event;
import ru.disdev.model.Action;
import ru.disdev.model.Prototype;
import ru.disdev.model.StateActionMap;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static ru.disdev.bot.TelegramKeyBoards.*;
import static ru.disdev.util.TimeTableUtils.*;

@Prototype
public class EventFlow extends AbstractPostFlow<Event> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MM yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH mm");
    private static final DateTimeFormatter NOTIFICATION_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd MM yyyy HH mm");
    private static final String A_DAY_BEFORE = "За день в 20:00";

    private boolean canBeFinished;

    public EventFlow(long chatId, ScheduledFuture<?> cancelTask) {
        super(chatId, cancelTask);
    }


    @Override
    public Event buildResult() {
        return new Event();
    }

    @Override
    public void finish() {
        if (!canBeFinished) {
            jumpToState(getCurrentState() + 2);
            return;
        }
        super.finish();
    }

    private ReplyKeyboard getDateKeyboard() {
        LocalDate now = LocalDate.now();
        List<String> dates = IntStream.range(1, 31)
                .mapToObj(value -> DATE_FORMATTER.format(now.plusDays(value)))
                .collect(Collectors.toList());
        return addLast(MessageConst.CANCEL, makeTableKeyboard(true, dates, 3));
    }

    private ReplyKeyboard getTimeKeyboard() {
        List<String> times = Stream.of(FIRST_LESSON, SECOND_LESSON, THIRD_LESSON,
                FOURTH_LESSON, FIFTH_LESSON, SIXTH_LESSON,
                SEVENTH_LESSON, LAST_LESSON)
                .map(TIME_FORMATTER::format)
                .collect(Collectors.toList());
        times.add(MessageConst.CANCEL);
        return makeOneColumnKeyboard(true, times);
    }

    private ReplyKeyboard getNotificationDateTimeKeyboard() {
        return makeKeyboard(true, rows(row(MessageConst.NEXT, MessageConst.CANCEL), row(A_DAY_BEFORE)));
    }

    @Override
    protected StateActionMap fillStateActions(StateActionMap map) {
        return super.fillStateActions(map)
                .then(Action.of(this::getDate, "Введите дату события в формате: дд ММ ГГГГ", getDateKeyboard()))
                .then(Action.of(this::getTime, "Введите время события в формате: чч мм", getTimeKeyboard()))
                .then(Action.of(this::getDateTimeToNotify,
                        "Введите дату/вермя рассылки уведомления в формате: дд ММ ГГГГ чч мм",
                        getNotificationDateTimeKeyboard()));
    }

    private void getDate(Message message) {
        if (message.hasText()) {
            String text = message.getText();
            try {
                TemporalAccessor temporalAccessor = DATE_FORMATTER.parse(text);
                LocalDate date = LocalDate.from(temporalAccessor);
                if (date.isBefore(LocalDate.now())) {
                    sendMessage("Указанная дата уже прошла");
                    return;
                }
                getResult().setDate(date);
                toNextState();
            } catch (Exception ex) {
                sendMessage("Неверный формат даты!");
            }
        }
    }

    private void getTime(Message message) {
        if (message.hasText()) {
            String text = message.getText();
            try {
                TemporalAccessor temporalAccessor = TIME_FORMATTER.parse(text);
                LocalTime localTime = LocalTime.from(temporalAccessor).withSecond(0);
                LocalDateTime localDateTime = LocalDateTime.of(getResult().getDate(), localTime);
                if (localDateTime.isBefore(LocalDateTime.now())) {
                    sendMessage("Указанная дата/время уже наступила");
                    return;
                }
                getResult().setTime(localTime);
                toNextState();
            } catch (Exception ex) {
                sendMessage("Неверный формат времени!");
            }
        }
    }

    private void getDateTimeToNotify(Message message) {
        if (message.hasText()) {
            String text = message.getText();
            boolean canFinish = false;
            switch (text) {
                case MessageConst.NEXT:
                    canFinish = true;
                    break;
                case A_DAY_BEFORE:
                    LocalDateTime notificationDate = LocalDateTime.of(getResult().getDate(), getResult().getTime())
                            .minusDays(1)
                            .withHour(20)
                            .withMinute(0)
                            .withSecond(0);
                    sendMessage("Время уведомления: " + NOTIFICATION_DATE_TIME_FORMATTER.format(notificationDate));
                    getResult().setNotificationDateTime(notificationDate);
                    canFinish = true;
                    break;
                default:
                    try {
                        TemporalAccessor temporalAccessor = NOTIFICATION_DATE_TIME_FORMATTER.parse(text);
                        LocalDateTime localDateTime = LocalDateTime.from(temporalAccessor);
                        if (localDateTime.isBefore(LocalDateTime.now())) {
                            sendMessage("Указанная дата/время уже наступила");
                            return;
                        }
                        getResult().setNotificationDateTime(localDateTime);
                        canFinish = true;
                    } catch (Exception ex) {
                        sendMessage("Неверный формат времени!");
                    }
                    break;
            }

            if (canFinish) {
                this.canBeFinished = true;
                finish();
            }
        }
    }

    @Override
    protected ReplyKeyboard getKeyboardAfterFinish() {
        return TelegramKeyBoards.eventKeyboard();
    }
}
