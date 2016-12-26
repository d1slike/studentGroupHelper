package ru.disdev.model.flows;

import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import ru.disdev.bot.MessageConst;
import ru.disdev.bot.TelegramKeyBoards;
import ru.disdev.entity.Prototype;
import ru.disdev.model.Action;
import ru.disdev.model.StateActionMap;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ru.disdev.bot.TelegramKeyBoards.addLast;
import static ru.disdev.bot.TelegramKeyBoards.makeTableKeyboard;
import static ru.disdev.bot.commands.impl.TimeTableCommand.DATE_REQUEST_FORMATTER;

@Prototype
public class TimeTableDateRequestFlow extends Flow<String> {
    public TimeTableDateRequestFlow(long chatId, Runnable onDone) {
        super(chatId, onDone);
    }

    @Override
    protected String buildResult() {
        return "";
    }

    private void getDate(Message message) {
        if (message.hasText()) {
            String text = message.getText();
            updateResult(text);
            finish();
        }
    }

    private ReplyKeyboard makeKeyboard() {
        LocalDate now = LocalDate.now();
        List<String> dates = IntStream.range(1, 31)
                .mapToObj(value -> DATE_REQUEST_FORMATTER.format(now.plusDays(value)))
                .collect(Collectors.toList());
        return addLast(MessageConst.CANCEL, makeTableKeyboard(true, dates, 3));
    }

    @Override
    protected StateActionMap fillStateActions(StateActionMap map) {
        return map.then(Action.of(this::getDate, "Введите дату в формате дд.ММ.гг", makeKeyboard()));
    }

    @Override
    protected ReplyKeyboard getKeyboardAfterFinish() {
        return TelegramKeyBoards.timeTableKeyboard();
    }
}
