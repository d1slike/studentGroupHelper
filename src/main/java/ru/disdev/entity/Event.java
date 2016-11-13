package ru.disdev.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Entity
@Table(name = "events")
public class Event extends Post {

    public static final DateTimeFormatter FORMATTER_DATE =
            DateTimeFormatter.ofPattern("dd.MM.yyyy (EEEE)").withLocale(Locale.forLanguageTag("ru"));
    public static final DateTimeFormatter FORMATTER_TIME =
            DateTimeFormatter.ofPattern("HH:mm");

    @Id
    @GeneratedValue
    private int id;
    private LocalDate date;
    private LocalTime time;
    private LocalDateTime notificationDateTime;

    public Event() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDateTime getNotificationDateTime() {
        return notificationDateTime;
    }

    public void setNotificationDateTime(LocalDateTime notificationDateTime) {
        this.notificationDateTime = notificationDateTime;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(super.toString());
        builder.append("\nДата/Время события:\n")
                .append(FORMATTER_DATE.format(date))
                .append(" ")
                .append(FORMATTER_TIME.format(time));
        return builder.append("\n--------------\n\n").toString();
    }
}
