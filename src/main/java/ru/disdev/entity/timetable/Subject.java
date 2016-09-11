package ru.disdev.entity.timetable;

import java.util.ArrayList;
import java.util.List;

public class Subject {
    private String name;
    private String teacher;
    private String type;
    private String aud;
    private List<Time> time = new ArrayList<>();

    public List<Time> getTime() {
        return time;
    }

    public void setTime(List<Time> time) {
        this.time = time;
    }

    public String getAud() {
        return aud;
    }

    public void setAud(String aud) {
        this.aud = aud;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("%s\n%s\n%s\n%s", name, teacher, type, aud);
    }
}
