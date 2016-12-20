package ru.disdev.bot.commands.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import ru.disdev.bot.TelegramBot;
import ru.disdev.bot.commands.AbstractRequest;
import ru.disdev.bot.commands.CommandArgs;
import ru.disdev.bot.commands.Request;
import ru.disdev.entity.Answer;
import ru.disdev.entity.Event;
import ru.disdev.model.TimeTable;
import ru.disdev.service.EventService;
import ru.disdev.util.TimeTableUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import static java.time.LocalDateTime.now;
import static ru.disdev.util.IOUtils.resourceAsStream;

@Request(command = "/tt")
public class TimeTableCommand extends AbstractRequest {

    @Autowired
    private EventService eventService;
    @Autowired
    private ObjectMapper mapper;
    private TimeTable timeTable;

    @PostConstruct
    private void init() throws IOException {
        timeTable = mapper.readValue(resourceAsStream("/time_table.json"), TimeTable.class);
    }

    @Override
    public Answer execute(CommandArgs commandArgs) {
        TelegramBot bot = (TelegramBot) absSender;
        String answer = "Некорректный аргумент.";
        if (arguments.length == 0) {
            LocalDate day = now().toLocalDate();
            answer = formatTimeTableRow(timeTable.getFor(day), day);
        } else {
            String arg = arguments[0];
            if (arg.equals("next")) {
                answer = formatTimeTableRow(timeTable.getNextLesson(now()), null);
            } else if (arg.startsWith("+")) {
                try {
                    String daysToAddInString = arg.substring(1);
                    int daysToAdd = Integer.parseInt(daysToAddInString);
                    LocalDate date = now().toLocalDate().plusDays(daysToAdd);
                    answer = formatTimeTableRow(timeTable.getFor(date), date);
                } catch (Exception ignored) {
                }
            } else if (arg.equals("week")) {
                StringBuilder builder = new StringBuilder("Расписание на неделю:\n");
                LocalDate now = now().toLocalDate();
                if (now.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    now = now.plusDays(1);
                }
                while (now.getDayOfWeek() != DayOfWeek.SUNDAY) {
                    Map<Integer, String> forToDay = timeTable.getFor(now);
                    builder.append(formatTimeTableRow(forToDay, now))
                            .append("\n++++++++++++++++++++++++++++\n\n");
                    now = now.plusDays(1);
                }
                answer = builder.toString();
            } else {
                try {
                    StringTokenizer tokenizer = new StringTokenizer(arg, ".");
                    int day = Integer.parseInt(tokenizer.nextToken());
                    int mouth = Integer.parseInt(tokenizer.nextToken());
                    LocalDate date = LocalDate.of(2016, mouth, day);
                    answer = formatTimeTableRow(timeTable.getFor(date), date);
                } catch (Exception ignored) {
                }
            }
        }

        bot.sendMessage(chat.getId(), answer, true);

    }

    private String formatTimeTableRow(Map<Integer, String> row, LocalDate day) {
        StringBuilder stringBuilder = new StringBuilder();
        if (day != null) {
            stringBuilder.append("<b>Пары на ")
                    .append(Event.FORMATTER_DATE.format(day))
                    .append(":</b>\n\n");
        }
        if (row.isEmpty())
            stringBuilder.append("<b>Нет пар\n</b>");
        else {
            row.forEach((integer, s) -> stringBuilder
                    .append("<b>")
                    .append(integer)
                    .append("</b>")
                    .append(" (<i>")
                    .append(TimeTableUtils.getTimeForLessonNumber(integer))
                    .append("</i>): ")
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
