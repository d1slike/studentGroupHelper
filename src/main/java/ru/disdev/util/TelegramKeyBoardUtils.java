package ru.disdev.util;

import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TelegramKeyBoardUtils {

    private static ReplyKeyboardMarkup defaultKeyboard;

    static {
        loadDefaultKeyBoard();
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

    public static ReplyKeyboardMarkup defaultKeyBoard() {
        return defaultKeyboard;
    }
}
