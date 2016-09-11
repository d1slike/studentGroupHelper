package ru.disdev.entity.timetable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.disdev.util.converters.LocalDateDeserializer;
import ru.disdev.util.converters.LocalDateSerializer;

import java.time.LocalDate;

public class Time {
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate from;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate to;
    private boolean everyWeek;

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public LocalDate getTo() {
        return to;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }

    public boolean isEveryWeek() {
        return everyWeek;
    }

    public void setEveryWeek(boolean everyWeek) {
        this.everyWeek = everyWeek;
    }
}
