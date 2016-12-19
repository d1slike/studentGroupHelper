package ru.disdev.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import ru.disdev.commands.EventCommand;
import ru.disdev.commands.TimeTableCommand;

import static ru.disdev.bot.TelegramKeyBoards.*;

@Component
public class InputMessagesMapper {

    @Autowired
    private TelegramBot telegramBot;
    @Autowired
    private EventCommand eventCommand;
    @Autowired
    private TimeTableCommand timeTableCommand;

    @CommandMapping(message = LESSONS_NEXT)
    public void getNextLesson(User user, Chat chat) {
        timeTableCommand.execute(telegramBot, user, chat, new String[]{"next"});
    }

    @CommandMapping(message = LESSONS_TODAY)
    public void todayLessons(User user, Chat chat) {
        timeTableCommand.execute(telegramBot, user, chat, new String[]{});
    }

    @CommandMapping(message = LESSONS_TOMORROW)
    public void tomorrowLesson(User user, Chat chat) {
        timeTableCommand.execute(telegramBot, user, chat, new String[]{"+1"});
    }

    @CommandMapping(message = LESSONS_WEEK)
    public void weekLesson(User user, Chat chat) {
        timeTableCommand.execute(telegramBot, user, chat, new String[]{"week"});
    }

    @CommandMapping(message = EVENTS_LIST)
    public void eventList(User user, Chat chat) {
        eventCommand.execute(telegramBot, user, chat, new String[]{});
    }
}
