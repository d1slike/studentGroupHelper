package ru.disdev.entity;

import ru.disdev.model.Flowable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "events")
public class Event implements Flowable {

    public static final DateTimeFormatter FORMATTER_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final DateTimeFormatter FORMATTER_TIME = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter FULL_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Id
    @GeneratedValue
    private int id;
    private String tag;
    private String information;
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

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
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

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(tag != null ? ("#" + tag) : "")
                .append("\n")
                .append(information)
                .append("\n")
                .append(FORMATTER_DATE.format(date))
                .append(" ")
                .append(FORMATTER_TIME.format(time));
        if (notificationDateTime != null) {
            builder
                    .append("\nДата/Время уведомления:\n")
                    .append(FULL_FORMATTER.format(notificationDateTime));
        }
        return builder.append("\n---\n").toString();
    }
}
