package ru.disdev.util;

import java.util.StringTokenizer;

public class TelegramBotUtils {

    public static String getCommandArg(String text, String delim) {
        StringTokenizer tokenizer = new StringTokenizer(text, delim);
        tokenizer.nextToken();
        String action = tokenizer.nextToken().trim();
        return action;
    }
}
