package ru.disdev.bot;

import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TelegramKeyBoards {

    private static final String[] TAGS = {"Далее", "web", "бд", "комграф", "элтех", "чмв", "тка", "эконом", "оуп"};
    public static final String LESSONS_NEXT = "Пары: следующая";
    public static final String LESSONS_TODAY = "Пары: сегодня";
    public static final String LESSONS_TOMORROW = "Пары: на завтра";
    public static final String LESSONS_WEEK = "Пары: на неделю";
    public static final String EVENTS_LIST = "События: список";

    private static ReplyKeyboardMarkup defaultKeyboard;
    private static ReplyKeyboardMarkup tagListKeyboard;
    private static ReplyKeyboard hideKeyBoard = new ReplyKeyboardRemove();

    static {
        loadDefaultKeyBoard();
        loadTagListKeyBoard();
    }

    private static void loadDefaultKeyBoard() {
        List<KeyboardRow> rows = new ArrayList<>();
        defaultKeyboard = new ReplyKeyboardMarkup();
        defaultKeyboard.setOneTimeKeyboad(false);
        defaultKeyboard.setSelective(true);
        defaultKeyboard.setResizeKeyboard(true);
        defaultKeyboard.setKeyboard(rows);

        KeyboardRow firstRow = new KeyboardRow();
        firstRow.add(new KeyboardButton(LESSONS_NEXT));
        firstRow.add(new KeyboardButton(LESSONS_TODAY));

        KeyboardRow secondRow = new KeyboardRow();
        secondRow.add(new KeyboardButton(LESSONS_TOMORROW));
        secondRow.add(new KeyboardButton(LESSONS_WEEK));

        KeyboardRow events = new KeyboardRow();
        events.add(EVENTS_LIST);

        Stream.of(firstRow, secondRow, events).forEach(rows::add);

        defaultKeyboard.setKeyboard(rows);
    }

    private static void loadTagListKeyBoard() {
        List<KeyboardRow> rows = new ArrayList<>();
        tagListKeyboard = new ReplyKeyboardMarkup();
        tagListKeyboard.setOneTimeKeyboad(false);
        tagListKeyboard.setSelective(true);
        tagListKeyboard.setResizeKeyboard(true);
        tagListKeyboard.setKeyboard(rows);

        Stream.of(TAGS)
                .forEach(tag -> {
                    KeyboardRow row = new KeyboardRow();
                    row.add(new KeyboardButton(tag));
                    rows.add(row);
                });
        tagListKeyboard.setKeyboard(rows);
    }

    public static ReplyKeyboardMarkup defaultKeyBoard() {
        return defaultKeyboard;
    }

    public static ReplyKeyboard getHideKeyBoard() {
        return hideKeyBoard;
    }

    public static ReplyKeyboardMarkup getTagListKeyboard() {
        return tagListKeyboard;
    }
}
