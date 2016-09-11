package ru.disdev.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import ru.disdev.VkGroupBot;
import ru.disdev.model.TimeTable;
import ru.disdev.util.TimeTableUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.stream.Stream;

public class TimeTableCommand extends BotCommand {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Autowired
    private TimeTable timeTable;

    private ReplyKeyboardMarkup markup = getKeboard();

    public TimeTableCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        VkGroupBot bot = (VkGroupBot) absSender;
        String answer = "\"Некорректный аргумент.";
        if (arguments.length == 0) {
            LocalDate day = getNow().toLocalDate();
            answer = formatTimeTableRow(timeTable.getTo(day), day);
        } else {
            String arg = arguments[0];
            if (arg.equals("next")) {
                answer = timeTable.getNextLesson(getNow());
            } else if (arg.startsWith("+")) {
                try {
                    String daysToAddInString = arg.substring(1);
                    int daysToAdd = Integer.parseInt(daysToAddInString);
                    LocalDate date = getNow().toLocalDate().plusDays(daysToAdd);
                    answer = formatTimeTableRow(timeTable.getTo(date), date);
                } catch (Exception e) {
                }
            } else {
                try {
                    StringTokenizer tokenizer = new StringTokenizer(arg, ".");
                    int day = Integer.parseInt(tokenizer.nextToken());
                    int mouth = Integer.parseInt(tokenizer.nextToken());
                    LocalDate date = LocalDate.of(2016, mouth, day);
                    answer = formatTimeTableRow(timeTable.getTo(date), date);
                } catch (Exception ex) {
                }
            }
        }

        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());
        message.setText(answer);
        message.setReplyMarkup(markup);
        message.enableMarkdown(true);

        try {
            bot.sendMessage(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private ReplyKeyboardMarkup getKeboard() {
        List<KeyboardRow> rows = new ArrayList<>();
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setOneTimeKeyboad(false);
        markup.setSelective(true);
        markup.setResizeKeyboard(true);
        markup.setKeyboard(rows);

        KeyboardRow nextLessonRow = new KeyboardRow();
        nextLessonRow.add(new KeyboardButton("TT: следующая пара"));

        KeyboardRow todayLessonsRow = new KeyboardRow();
        todayLessonsRow.add(new KeyboardButton("TT: пары сегодня"));

        KeyboardRow tomorrowLessonsRow = new KeyboardRow();
        tomorrowLessonsRow.add(new KeyboardButton("TT: пары на завтра"));

        Stream.of(nextLessonRow, todayLessonsRow, tomorrowLessonsRow).forEach(rows::add);

        markup.setKeyboard(rows);
        return markup;
    }

    private LocalDateTime getNow() {
        return LocalDateTime.ofInstant(Instant.now(), ZoneId.of("Europe/Moscow"));
    }

    private String formatTimeTableRow(Map<Integer, String> row, LocalDate day) {
        StringBuilder stringBuilder = new StringBuilder()
                .append("Пары на ")
                .append(FORMATTER.format(day))
                .append(":\n");
        if (row.isEmpty())
            stringBuilder.append("Нет пар");
        else {
            row.forEach((integer, s) -> stringBuilder
                    .append(integer)
                    .append(" (")
                    .append(TimeTableUtils.getTimeForLessonNumber(integer))
                    .append("): ")
                    .append(s)
                    .append("\n---\n"));
        }
        return stringBuilder.toString();
    }
}
