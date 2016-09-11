package ru.disdev.model;

import ru.disdev.entity.timetable.Subject;
import ru.disdev.entity.timetable.Time;

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
        String nextLesson = "На сегодня пар нет.";
        if (result.isEmpty())
            return nextLesson;
        LocalTime time = now.toLocalTime();
        for (int lessonNum : result.keySet()) {
            int num = getNextLessonNum(time, lessonNum);
            if (num != -1) {
                nextLesson = result.get(num);
                break;
            }
        }

        return nextLesson;

    }

    public int getNextLessonNum(LocalTime time, int lessonNum) {
        final int hour = time.getHour();
        final int minute = time.getMinute();
        if (hour < 8 && minute < 30 && lessonNum == 1)
            return 1;
        else if (hour < 10 && minute < 20 && lessonNum == 2)
            return 2;
        else if (hour < 12 && minute < 20 && lessonNum == 3)
            return 3;
        else if (hour < 14 && minute < 10 && lessonNum == 4)
            return 4;
        else if (hour < 16 && minute < 00 && lessonNum == 5)
            return 5;
        else if (hour < 18 && minute < 00 && lessonNum == 6)
            return 6;
        else if (hour < 19 && minute < 40 && lessonNum == 7)
            return 7;
        else if (hour < 21 && minute < 20 && lessonNum == 8)
            return 8;
        return -1;
    }

}
