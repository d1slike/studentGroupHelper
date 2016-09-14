package ru.disdev.util;

import ru.disdev.entity.Event;

import java.util.Map;

public class EventUtils {
    public static String formatList(Map<Integer, Event> map) {
        StringBuilder builder = new StringBuilder();
        map.forEach((integer, event) -> builder.append(integer).append(" :").append(event.toString()));
        return builder.toString();
    }
}
