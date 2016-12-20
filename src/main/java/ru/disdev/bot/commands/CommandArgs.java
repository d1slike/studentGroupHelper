package ru.disdev.bot.commands;

import java.util.HashMap;
import java.util.Map;

public final class CommandArgs {
    private final Map<String, String> argMap;

    public CommandArgs(Map<String, String> params) {
        argMap = params;
    }

    public CommandArgs() {
        argMap = new HashMap<>();
    }

    public String getStringOrDefault(String param, String defaultValue) {
        return argMap.getOrDefault(param, defaultValue);
    }

    public int getIntOrDefault(String param, int defaultValue) {
        int toReturn = defaultValue;
        String value = argMap.getOrDefault(param, null);
        if (value == null) {
            return toReturn;
        }
        try {
            toReturn = Integer.parseInt(value);
        } catch (Exception ignored) {

        }

        return toReturn;
    }

    public int getInt(String param) {
        return getIntOrDefault(param, -1);
    }

    public String getString(String param) {
        return argMap.getOrDefault(param, null);
    }

    public int size() {
        return argMap.size();
    }
}
