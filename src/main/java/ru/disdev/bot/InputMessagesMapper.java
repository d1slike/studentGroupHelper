package ru.disdev.bot;

import ru.disdev.model.flows.DeleteEventFlow;
import ru.disdev.model.flows.MJUserFlow;
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
    public void getNextLesson(TelegramBot telegramBot, long chatId, int userId) {
        commandHolder.resolveCommand(telegramBot, chatId, userId, "/tt next");
    }

    @CommandMapping(message = LESSONS_TODAY)
    public void todayLessons(TelegramBot telegramBot, long chatId, int userId) {
        commandHolder.resolveCommand(telegramBot, chatId, userId, "/tt");
    }

    @CommandMapping(message = LESSONS_TOMORROW)
    public void tomorrowLesson(TelegramBot telegramBot, long chatId, int userId) {
        commandHolder.resolveCommand(telegramBot, chatId, userId, "/tt +1");
    }

    @CommandMapping(message = LESSONS_WEEK)
    public void weekLesson(TelegramBot telegramBot, long chatId, int userId) {
        commandHolder.resolveCommand(telegramBot, chatId, userId, "/tt week");
    }

    @CommandMapping(message = EVENT_LIST)
    public void eventList(TelegramBot telegramBot, long chatId, int userId) {
        commandHolder.resolveCommand(telegramBot, chatId, userId, "/event");
    }

    @CommandMapping(message = HOME)
    public void main(TelegramBot telegramBot, long chatId, int userId) {
        commandHolder.resolveCommand(telegramBot, chatId, userId, "/nav main");
    }

    @CommandMapping(message = EVENTS)
    public void events(TelegramBot telegramBot, long chatId, int userId) {
        commandHolder.resolveCommand(telegramBot, chatId, userId, "/nav events");
    }

    @CommandMapping(message = STORAGE)
    public void storage(TelegramBot telegramBot, long chatId, int userId) {
        commandHolder.resolveCommand(telegramBot, chatId, userId, "/nav storage");
    }

    @CommandMapping(message = TIME_TABLE)
    public void timeTable(TelegramBot telegramBot, long chatId, int userId) {
        commandHolder.resolveCommand(telegramBot, chatId, userId, "/nav tt");
    }

    @CommandMapping(message = TEACHERS)
    public void teachers(TelegramBot telegramBot, long chatId, int userId) {
        commandHolder.resolveCommand(telegramBot, chatId, userId, "/teach");
    }

    @CommandMapping(message = NEW_POST)
    public void newPost(TelegramBot telegramBot, long chatId, int userId) {
        commandHolder.resolveCommand(telegramBot, chatId, userId, "/post");
    }

    @CommandMapping(message = ALL_FILES)
    public void allFiles(TelegramBot telegramBot, long chatId, int userId) {
        commandHolder.resolveCommand(telegramBot, chatId, userId, "/file");
    }

    @CommandMapping(message = FILE_NAME_SEARCH)
    public void fileNameSearch(TelegramBot telegramBot, long chatId, int userId) {
        telegramBot.newFlow(NameSearchFlow.class, chatId)
                .appendOnFinish(result ->
                        commandHolder.resolveCommand(telegramBot, chatId, userId, "/file name " + result))
                .start();

    }

    @CommandMapping(message = FILE_TAG_SEARCH)
    public void tagNameSearch(TelegramBot telegramBot, long chatId, int userId) {
        telegramBot.newFlow(TagSearchFlow.class, chatId)
                .appendOnFinish(result ->
                        commandHolder.resolveCommand(telegramBot, chatId, userId, "/file tag " + result))
                .start();

    }

    @CommandMapping(message = ADD_EVENT)
    public void eventNew(TelegramBot telegramBot, long chatId, int userId) {
        commandHolder.resolveCommand(telegramBot, chatId, userId, "/event new");
    }

    @CommandMapping(message = DELETE_EVENT)
    public void deleteEvent(TelegramBot telegramBot, long chatId, int userId) {
        telegramBot.newFlow(DeleteEventFlow.class, chatId)
                .appendOnFinish(result -> commandHolder.resolveCommand(telegramBot, chatId, userId, "/event del " + result))
                .start();

    }

    @CommandMapping(message = LESSONS_FOR_DAY)
    public void timeTableRequestDate(TelegramBot telegramBot, long chatId, int userId) {
        telegramBot.newFlow(TimeTableDateRequestFlow.class, chatId)
                .appendOnFinish(result -> commandHolder.resolveCommand(telegramBot, chatId, userId, "/tt " + result))
                .start();

    }

    @CommandMapping(message = MODULES)
    public void modules(TelegramBot telegramBot, long chatId, int userId) {
        commandHolder.resolveCommand(telegramBot, chatId, userId, "/nav mj");
    }

    @CommandMapping(message = MODULES_SHOW)
    public void modulesShow(TelegramBot telegramBot, long chatId, int userId) {
        commandHolder.resolveCommand(telegramBot, chatId, userId, "/mj get");
    }

    @CommandMapping(message = MODULES_ADD_ACC)
    public void modulesAddAcc(TelegramBot telegramBot, long chatId, int userId) {
        telegramBot.newFlow(MJUserFlow.class, chatId)
                .appendOnFinish(mjUser -> {
                    String command = "/mj new " + mjUser.getLogin() + " " + mjUser.getPassword();
                    commandHolder.resolveCommand(telegramBot, chatId, userId, command);
                }).start();
    }
}
