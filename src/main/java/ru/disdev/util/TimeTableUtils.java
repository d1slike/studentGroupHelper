package ru.disdev.util;

public class TimeTableUtils {
    public static String getTimeForLessonNumber(int lessonNum) {
        switch (lessonNum) {
            case 1:
                return "8:30 - 10:10";
            case 2:
                return "10:20 - 12:00";
            case 3:
                return "12:20 - 14:00";
            case 4:
                return "14:10 - 15:50";
            case 5:
                return "16:00 - 17:40";
            case 6:
                return "18:00 - 19:30";
            case 7:
                return "19:40 - 21:10";
            case 8:
                return "21:20 - 22:50";
            default:
                return "Empty";
        }
    }
}
