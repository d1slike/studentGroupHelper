package ru.disdev.bot;

public class InputMessages {
    @CommandMapping(command = "tt", args = "next")
    public static final String LESSONS_NEXT = "Пары: следующая";
    @CommandMapping(command = "tt")
    public static final String LESSONS_TODAY = "Пары: сегодня";
    @CommandMapping(command = "tt", args = "+1")
    public static final String LESSONS_TOMORROW = "Пары: на завтра";
    @CommandMapping(command = "tt", args = "week")
    public static final String LESSONS_WEEK = "Пары: на неделю";
    @CommandMapping(command = "event")
    public static final String EVENTS_LIST = "События: список";
}
