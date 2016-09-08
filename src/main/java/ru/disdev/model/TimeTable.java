package ru.disdev.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeTable {
    private List<Map<Integer, List<Subject>>> timeTable = new ArrayList<>();

    public List<Map<Integer, List<Subject>>> getTimeTable() {
        return timeTable;
    }

    public Map<Integer, String> getTo(LocalDate date) {
        Map<Integer, String> result = new HashMap<>();
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (!dayOfWeek.equals(DayOfWeek.SUNDAY)) {
            Map<Integer, List<Subject>> lessons = timeTable.get(dayOfWeek.getValue() - 1);
            lessons.forEach((lesson, subjects) -> {
                for (Subject subject : subjects) {
                    boolean exist = false;
                    List<Time> times = subject.getTime();
                    for (Time time : times) {
                        if (time.isEveryWeek()) {
                            LocalDate from = time.getFrom();
                            LocalDate to = time.getTo();
                            if (from.equals(date) || to.equals(date) || (from.isBefore(date) && to.isAfter(date))) {
                                exist = true;
                            }
                        } else {
                            LocalDate copyFrom = LocalDate.from(time.getFrom());
                            while (copyFrom.isBefore(time.getTo())) {
                                if (copyFrom.equals(date)) {
                                    exist = true;
                                    break;
                                }

                                copyFrom = copyFrom.plusWeeks(2);
                            }

                        }
                    }

                    if (exist) {
                        result.put(lesson, subject.toString());
                        break;
                    }
                }
            });
        }
        return result;
    }

    public String getNextLesson(LocalDateTime now) {
        Map<Integer, String> result = getTo(now.toLocalDate());
        if (result.isEmpty())
            return "На сегодня пар нет.";
        String lesson = result.get(getNextLessonNum(now.toLocalTime()));
        if (lesson == null)
            return "На сегодня пар нет.";
        return lesson;

    }

    public int getNextLessonNum(LocalTime time) {
        final int hour = time.getHour();
        final int minute = time.getMinute();
        if (hour < 8 && minute < 30)
            return 1;
        else if (hour < 10 && minute < 20)
            return 2;
        else if (hour < 12 && minute < 20)
            return 3;
        else if (hour < 14 && minute < 10)
            return 4;
        else if (hour < 16 && minute < 00)
            return 5;
        else if (hour < 18 && minute < 00)
            return 6;
        else if (hour < 19 && minute < 40)
            return 7;
        else if (hour < 21 && minute < 20)
            return 8;
        return -1;
    }

}
