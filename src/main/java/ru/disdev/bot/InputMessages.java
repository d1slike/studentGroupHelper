package ru.disdev.bot;

public class InputMessages {
    @CommandMapping(command = "tt", arg = "next")
    public static String LESSONS_NEXT = "Пары: следующая";
    @CommandMapping(command = "tt")
    public static String LESSONS_TODAY = "Пары: сегодня";
    @CommandMapping(command = "tt", arg = "+1")
    public static String LESSONS_TOMORROW = "Пары: на завтра";
    @CommandMapping(command = "tt", arg = "week")
    public static String LESSONS_WEEK = "Пары: на неделю";
    @CommandMapping(command = "event")
    public static String EVENTS_LIST = "События: список";
}
