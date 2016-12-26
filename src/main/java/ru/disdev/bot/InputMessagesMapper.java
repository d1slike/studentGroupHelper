package ru.disdev.bot;

import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import ru.disdev.model.flows.DeleteEventFlow;
import ru.disdev.model.flows.TimeTableDateRequestFlow;
import ru.disdev.model.flows.files.NameSearchFlow;
import ru.disdev.model.flows.files.TagSearchFlow;

import static ru.disdev.bot.MessageConst.*;

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

    @CommandMapping(message = EVENT_LIST)
    public void eventList(TelegramBot telegramBot, User user, Chat chat) {
        commandHolder.resolveCommand(telegramBot, chat.getId(), user.getId(), "/event");
    }

    @CommandMapping(message = HOME)
    public void main(TelegramBot telegramBot, User user, Chat chat) {
        commandHolder.resolveCommand(telegramBot, chat.getId(), user.getId(), "/nav main");
    }

    @CommandMapping(message = EVENTS)
    public void events(TelegramBot telegramBot, User user, Chat chat) {
        commandHolder.resolveCommand(telegramBot, chat.getId(), user.getId(), "/nav events");
    }

    @CommandMapping(message = STORAGE)
    public void storage(TelegramBot telegramBot, User user, Chat chat) {
        commandHolder.resolveCommand(telegramBot, chat.getId(), user.getId(), "/nav storage");
    }

    @CommandMapping(message = TIME_TABLE)
    public void timeTable(TelegramBot telegramBot, User user, Chat chat) {
        commandHolder.resolveCommand(telegramBot, chat.getId(), user.getId(), "/nav tt");
    }

    @CommandMapping(message = TEACHERS)
    public void teachers(TelegramBot telegramBot, User user, Chat chat) {
        commandHolder.resolveCommand(telegramBot, chat.getId(), user.getId(), "/teach");
    }

    @CommandMapping(message = NEW_POST)
    public void newPost(TelegramBot telegramBot, User user, Chat chat) {
        commandHolder.resolveCommand(telegramBot, chat.getId(), user.getId(), "/post");
    }

    @CommandMapping(message = ALL_FILES)
    public void allFiles(TelegramBot telegramBot, User user, Chat chat) {
        commandHolder.resolveCommand(telegramBot, chat.getId(), user.getId(), "/file");
    }

    @CommandMapping(message = FILE_NAME_SEARCH)
    public void fileNameSearch(TelegramBot telegramBot, User user, Chat chat) {
        telegramBot.startFlow(NameSearchFlow.class, chat.getId()).appendOnFinish(result ->
                commandHolder.resolveCommand(telegramBot, chat.getId(), user.getId(), "/file name " + result));

    }

    @CommandMapping(message = FILE_TAG_SEARCH)
    public void tagNameSearch(TelegramBot telegramBot, User user, Chat chat) {
        telegramBot.startFlow(TagSearchFlow.class, chat.getId()).appendOnFinish(result ->
                commandHolder.resolveCommand(telegramBot, chat.getId(), user.getId(), "/file tag " + result));

    }

    @CommandMapping(message = ADD_EVENT)
    public void eventNew(TelegramBot telegramBot, User user, Chat chat) {
        commandHolder.resolveCommand(telegramBot, chat.getId(), user.getId(), "/event new");
    }

    @CommandMapping(message = DELETE_EVENT)
    public void deleteEvent(TelegramBot telegramBot, User user, Chat chat) {
        telegramBot.startFlow(DeleteEventFlow.class, chat.getId()).appendOnFinish(result ->
                commandHolder.resolveCommand(telegramBot, chat.getId(), user.getId(), "/event del " + result));

    }

    @CommandMapping(message = LESSONS_FOR_DAY)
    public void timeTableRequestDate(TelegramBot telegramBot, User user, Chat chat) {
        telegramBot.startFlow(TimeTableDateRequestFlow.class, chat.getId()).appendOnFinish(result ->
                commandHolder.resolveCommand(telegramBot, chat.getId(), user.getId(), "/tt " + result));

    }
}
