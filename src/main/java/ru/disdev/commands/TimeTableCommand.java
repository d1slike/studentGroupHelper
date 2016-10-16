package ru.disdev.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import ru.disdev.VkGroupBot;
import ru.disdev.entity.Event;
import ru.disdev.model.TimeTable;
import ru.disdev.service.EventService;
import ru.disdev.util.TelegramKeyBoardUtils;
import ru.disdev.util.TimeTableUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class TimeTableCommand extends BotCommand {

    @Autowired
    private TimeTable timeTable;
    @Autowired
    private EventService eventService;

    public TimeTableCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        VkGroupBot bot = (VkGroupBot) absSender;
        String answer = "Некорректный аргумент.";
        if (arguments.length == 0) {
            LocalDate day = getNow().toLocalDate();
            answer = formatTimeTableRow(timeTable.getTo(day), day);
        } else {
            String arg = arguments[0];
            if (arg.equals("next")) {
                answer = formatTimeTableRow(timeTable.getNextLesson(getNow()), null);
            } else if (arg.startsWith("+")) {
                try {
                    String daysToAddInString = arg.substring(1);
                    int daysToAdd = Integer.parseInt(daysToAddInString);
                    LocalDate date = getNow().toLocalDate().plusDays(daysToAdd);
                    answer = formatTimeTableRow(timeTable.getTo(date), date);
                } catch (Exception ignored) {
                }
            } else {
                try {
                    StringTokenizer tokenizer = new StringTokenizer(arg, ".");
                    int day = Integer.parseInt(tokenizer.nextToken());
                    int mouth = Integer.parseInt(tokenizer.nextToken());
                    LocalDate date = LocalDate.of(2016, mouth, day);
                    answer = formatTimeTableRow(timeTable.getTo(date), date);
                } catch (Exception ignored) {
                }
            }
        }

        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());
        message.setText(answer);
        message.setReplyMarkup(TelegramKeyBoardUtils.defaultKeyBoard());
        message.enableMarkdown(true);

        try {
            bot.sendMessage(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private LocalDateTime getNow() {
        return LocalDateTime.ofInstant(Instant.now(), ZoneId.of("Europe/Moscow"));
    }

    private String formatTimeTableRow(Map<Integer, String> row, LocalDate day) {
        StringBuilder stringBuilder = new StringBuilder();
        if (day != null) {
            stringBuilder.append("Пары на ")
                    .append(Event.FORMATTER_DATE.format(day))
                    .append(":\n\n");
        }
        if (row.isEmpty())
            stringBuilder.append("Нет пар");
        else {
            row.forEach((integer, s) -> stringBuilder
                    .append(integer)
                    .append(" (")
                    .append(TimeTableUtils.getTimeForLessonNumber(integer))
                    .append("): ")
                    .append(s)
                    .append("\n----------------------\n"));
        }
        if (day != null) {
            List<Event> additional = eventService.findAllByDate(day);
            if (!additional.isEmpty()) {
                additional.forEach(stringBuilder::append);
            }
        }
        return stringBuilder.toString();
    }
}
