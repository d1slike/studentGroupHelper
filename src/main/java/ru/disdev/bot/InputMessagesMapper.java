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
        commandHolder.resolveCommand(telegramBot, chat.getId(), user.getId(), "/tt next");
    }

    @CommandMapping(message = LESSONS_TODAY)
    public void todayLessons(TelegramBot telegramBot, User user, Chat chat) {
        commandHolder.resolveCommand(telegramBot, chat.getId(), user.getId(), "/tt");
    }

    @CommandMapping(message = LESSONS_TOMORROW)
    public void tomorrowLesson(TelegramBot telegramBot, User user, Chat chat) {
        commandHolder.resolveCommand(telegramBot, chat.getId(), user.getId(), "/tt +1");
    }

    @CommandMapping(message = LESSONS_WEEK)
    public void weekLesson(TelegramBot telegramBot, User user, Chat chat) {
        commandHolder.resolveCommand(telegramBot, chat.getId(), user.getId(), "/tt week");
    }

    @CommandMapping(message = EVENTS_LIST)
    public void eventList(TelegramBot telegramBot, User user, Chat chat) {
        commandHolder.resolveCommand(telegramBot, chat.getId(), user.getId(), "/event");
    }
}
