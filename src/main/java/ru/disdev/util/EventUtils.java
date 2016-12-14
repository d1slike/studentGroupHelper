package ru.disdev.util;

import ru.disdev.entity.Event;

import java.util.Collection;

public class EventUtils {
    public static String formatList(Collection<Event> events) {
        StringBuilder builder = new StringBuilder();
        if (events.isEmpty()) {
            builder.append("Событий нет");
        } else {
            events.forEach(event -> builder.append(event.getId()).append(": ").append(event.toString()));
        }
        return builder.toString();
    }
}
