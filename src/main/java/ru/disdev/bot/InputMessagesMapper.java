package ru.disdev.bot;

import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;

import static ru.disdev.bot.TelegramKeyBoards.*;

public class InputMessagesMapper {

    private final CommandHolder commandHolder;

    public InputMessagesMapper(CommandHolder commandHolder) {
        this.commandHolder = commandHolder;
    }

    @CommandMapping(message = LESSONS_NEXT)
    public void getNextLesson(TelegramBot telegramBot, User user, Chat chat) {
        timeTableCommand.execute(telegramBot, user, chat, new String[]{"next"});
    }

    @CommandMapping(message = LESSONS_TODAY)
    public void todayLessons(TelegramBot telegramBot, User user, Chat chat) {
        timeTableCommand.execute(telegramBot, user, chat, new String[]{});
    }

    @CommandMapping(message = LESSONS_TOMORROW)
    public void tomorrowLesson(TelegramBot telegramBot, User user, Chat chat) {
        timeTableCommand.execute(telegramBot, user, chat, new String[]{"+1"});
    }

    @CommandMapping(message = LESSONS_WEEK)
    public void weekLesson(TelegramBot telegramBot, User user, Chat chat) {
        timeTableCommand.execute(telegramBot, user, chat, new String[]{"week"});
    }

    @CommandMapping(message = EVENTS_LIST)
    public void eventList(TelegramBot telegramBot, User user, Chat chat) {
        eventCommand.execute(telegramBot, user, chat, new String[]{});
    }
}
