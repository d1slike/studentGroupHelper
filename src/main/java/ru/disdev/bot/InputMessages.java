package ru.disdev.bot;

public class InputMessages {
    @CommandMapping(command = "tt", arg = "next")
    public static final String LESSONS_NEXT = "Пары: следующая";
    @CommandMapping(command = "tt")
    public static final String LESSONS_TODAY = "Пары: сегодня";
    @CommandMapping(command = "tt", arg = "+1")
    public static final String LESSONS_TOMORROW = "Пары: на завтра";
    @CommandMapping(command = "tt", arg = "week")
    public static final String LESSONS_WEEK = "Пары: на неделю";
    @CommandMapping(command = "event")
    public static final String EVENTS_LIST = "События: список";
}
