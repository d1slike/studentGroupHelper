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

import static ru.disdev.util.TimeTableUtils.*;

public class TimeTable {
    private List<Map<Integer, List<Subject>>> timeTable = new ArrayList<>();

    public List<Map<Integer, List<Subject>>> getTimeTable() {
        return timeTable;
    }

    public Map<Integer, String> getTo(LocalDate date) {
        Map<Integer, String> result = new HashMap<>();
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek.equals(DayOfWeek.SUNDAY))
            return result;

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
                            break;
                        }
                    } else {
                        LocalDate copyFrom = LocalDate.from(time.getFrom());
                        while (!copyFrom.isAfter(time.getTo())) {
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

        return result;
    }

    public Map<Integer, String> getNextLesson(LocalDateTime now) {
        Map<Integer, String> result = getTo(now.toLocalDate());
        Map<Integer, String> nextLesson = new HashMap<>();
        LocalTime time = now.toLocalTime();
        for (int lessonNum : result.keySet()) {
            int num = getNextLessonNum(time, lessonNum);
            if (num != -1) {
                nextLesson.put(num, result.get(num));
                break;
            }
        }

        return nextLesson;
    }

    private int getNextLessonNum(LocalTime time, int lessonNum) {
        if (time.isBefore(FIRST_LESSON) && lessonNum == 1)
            return 1;
        else if (time.isBefore(SECOND_LESSON) && lessonNum == 2)
            return 2;
        else if (time.isBefore(THIRD_LESSON) && lessonNum == 3)
            return 3;
        else if (time.isBefore(FOURTH_LESSON) && lessonNum == 4)
            return 4;
        else if (time.isBefore(FIFTH_LESSON) && lessonNum == 5)
            return 5;
        else if (time.isBefore(SIXTH_LESSON) && lessonNum == 6)
            return 6;
        else if (time.isBefore(SEVENTH_LESSON) && lessonNum == 7)
            return 7;
        else if (time.isBefore(LAST_LESSON) && lessonNum == 8)
            return 8;
        return -1;
    }

}
