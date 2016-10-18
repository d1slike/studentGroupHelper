package ru.disdev.util;

import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardHide;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TelegramKeyBoardUtils {

    private static ReplyKeyboardMarkup defaultKeyboard;
    private static ReplyKeyboardMarkup tagListKeyboard;
    private static ReplyKeyboard hideKeyBoard = new ReplyKeyboardHide();

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

        KeyboardRow nextLessonRow = new KeyboardRow();
        nextLessonRow.add(new KeyboardButton("Пары: следующая пара"));

        KeyboardRow lessons = new KeyboardRow();
        lessons.add(new KeyboardButton("Пары: сегодня"));
        lessons.add(new KeyboardButton("Пары: на завтра"));

        KeyboardRow events = new KeyboardRow();
        events.add("События: список");

        Stream.of(nextLessonRow, lessons, events).forEach(rows::add);

        defaultKeyboard.setKeyboard(rows);
    }

    private static void loadTagListKeyBoard() {
        List<KeyboardRow> rows = new ArrayList<>();
        tagListKeyboard = new ReplyKeyboardMarkup();
        tagListKeyboard.setOneTimeKeyboad(false);
        tagListKeyboard.setSelective(true);
        tagListKeyboard.setResizeKeyboard(true);
        tagListKeyboard.setKeyboard(rows);

        Stream.of("Далее", "web", "бд", "комграф", "элтех", "чмв", "тка", "эконом", "оуп")
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
