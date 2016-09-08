package ru.disdev.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import ru.disdev.VkGroupBot;
import ru.disdev.model.TimeTable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.StringTokenizer;

public class TimeTableCommand extends BotCommand {

    @Autowired
    private TimeTable timeTable;

    public TimeTableCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        VkGroupBot bot = (VkGroupBot) absSender;
        String answer;
        if (arguments.length == 0) {
            answer = formatTimeTableRow(timeTable.getTo(getNow().toLocalDate()));
        } else {
            String arg = arguments[0];
            if (arg.equals("next")) {
                answer = timeTable.getNextLesson(getNow());
            } else {
                try {
                    StringTokenizer tokenizer = new StringTokenizer(arg, ".");
                    int day = Integer.parseInt(tokenizer.nextToken());
                    int mouth = Integer.parseInt(tokenizer.nextToken());
                    answer = formatTimeTableRow(timeTable.getTo(LocalDate.of(2016, mouth, day)));
                } catch (Exception ex) {
                    answer = "Некорректный аргумент.";
                }
            }
        }

        bot.sendMessgae(chat.getId(), answer);

    }

    private LocalDateTime getNow() {
        return LocalDateTime.ofInstant(Instant.now(), ZoneId.of("Europe/Moscow"));
    }

    private String formatTimeTableRow(Map<Integer, String> row) {
        StringBuilder stringBuilder = new StringBuilder();
        row.forEach((integer, s) -> {
            stringBuilder.append(integer).append(": ").append(s).append("\n---\n");
        });
        return stringBuilder.toString();
    }
}
