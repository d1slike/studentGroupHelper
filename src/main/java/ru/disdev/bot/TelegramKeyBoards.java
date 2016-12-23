package ru.disdev.bot;

import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.disdev.bot.MessageConst.*;

public class TelegramKeyBoards {

    private static ReplyKeyboardMarkup mainKeyboard;
    private static ReplyKeyboardMarkup storageKeyboard;
    private static ReplyKeyboardMarkup eventKeyboard;
    private static ReplyKeyboardMarkup timeTableKeyboard;
    private static ReplyKeyboardMarkup cancelRow = makeKeyBoard(true, rows(row(CANCEL)));
    private static ReplyKeyboard hideKeyBoard = new ReplyKeyboardRemove();

    static {
        loadMainKeyBoard();
        loadStorageKeyboard();
        loadEventKeyboard();
        loadTimeTableKeyboard();
    }

    private static void loadTimeTableKeyboard() {
        timeTableKeyboard = makeKeyBoard(false, rows(row(LESSONS_NEXT, LESSONS_TODAY),
                row(LESSONS_TOMORROW, LESSONS_WEEK),
                row(HOME)));
    }

    private static void loadEventKeyboard() {
        eventKeyboard = makeKeyBoard(false, rows(row(EVENT_LIST),
                row(ADD_EVENT, DELETE_EVENT),
                row(HOME)));
    }

    private static void loadStorageKeyboard() {
        storageKeyboard = makeKeyBoard(false, rows(row(ALL_FILES),
                row(FILE_TAG_SEARCH, FILE_NAME_SEARCH),
                row(HOME)));
    }

    private static void loadMainKeyBoard() {
        mainKeyboard = makeKeyBoard(false, rows(
                row(TIME_TABLE, STORAGE),
                row(EVENTS, TEACHERS),
                row(NEW_POST)));
    }

    public static KeyboardRow row(String... buttons) {
        List<KeyboardButton> list =
                Stream.of(buttons).map(KeyboardButton::new).collect(Collectors.toList());
        KeyboardRow row = new KeyboardRow();
        row.addAll(list);
        return row;
    }


    public static List<KeyboardRow> rows(KeyboardRow... rows) {
        return Stream.of(rows).collect(Collectors.toList());
    }

    public static ReplyKeyboardMarkup makeKeyBoard(boolean oneSelectiveTime, List<KeyboardRow> rows) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(rows);
        markup.setOneTimeKeyboad(oneSelectiveTime);
        markup.setResizeKeyboard(true);
        markup.setSelective(true);
        return markup;
    }

    public static ReplyKeyboardMarkup makeColumnKeyBoard(boolean oneSelectiveTime, Collection<String> rows) {
        List<KeyboardRow> rowList = rows.stream().map(row -> {
            KeyboardButton button = new KeyboardButton(row);
            KeyboardRow keyboardRow = new KeyboardRow();
            keyboardRow.add(button);
            return keyboardRow;
        }).collect(Collectors.toList());
        return makeKeyBoard(oneSelectiveTime, rowList);
    }

    public static ReplyKeyboardMarkup addFirst(String button, ReplyKeyboardMarkup markup) {
        return addButton(0, button, markup);
    }

    public static ReplyKeyboardMarkup addLast(String button, ReplyKeyboardMarkup markup) {
        return addButton(markup.getKeyboard().size(), button, markup);
    }

    private static ReplyKeyboardMarkup addButton(int index, String button, ReplyKeyboardMarkup markup) {
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton(button));
        markup.getKeyboard().add(index, row);
        return markup;
    }

    public static ReplyKeyboardMarkup mainKeyBoard() {
        return mainKeyboard;
    }

    public static ReplyKeyboard hideKeyBoard() {
        return hideKeyBoard;
    }

    public static ReplyKeyboardMarkup storageKeyboard() {
        return storageKeyboard;
    }

    public static ReplyKeyboardMarkup eventKeyboard() {
        return eventKeyboard;
    }

    public static ReplyKeyboardMarkup timeTableKeyboard() {
        return timeTableKeyboard;
    }

    public static ReplyKeyboardMarkup cancelButton() {
        return cancelRow;
    }
}
