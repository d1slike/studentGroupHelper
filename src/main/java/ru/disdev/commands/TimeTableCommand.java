package ru.disdev.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import ru.disdev.VkGroupBot;
import ru.disdev.model.TimeTable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimeTableCommand extends BotCommand {

    @Autowired
    private TimeTable timeTable;

    public TimeTableCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        VkGroupBot bot = (VkGroupBot) absSender;
        if (arguments.length == 0) {
            bot.sendMessgae(chat.getId(), "Формат команды: /tt {дата} || next");
            return;
        }
        String arg = arguments[0];
        if (arg.equals("next")) {
            String lesson = timeTable
                    .getNextLesson(LocalDateTime.ofInstant(Instant.now(), ZoneId.of("Europe/Moscow")));
            bot.sendMessgae(chat.getId(), lesson);
        } else {

        }

    }
}
