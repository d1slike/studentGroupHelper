package ru.disdev.entity;

import ru.disdev.model.Flowable;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "events")
public class Event implements Flowable {

    @Id
    @GeneratedValue
    private int id;
    private String information;
    @OneToMany
    @JoinColumn(name = "eventId")
    private List<Date> dateList;
    private LocalDateTime notificationDateTime;

    Event() {

    }

    public List<Date> getDateList() {
        return dateList;
    }

    public void setDateList(List<Date> dateList) {
        this.dateList = dateList;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getNotificationDateTime() {
        return notificationDateTime;
    }

    public void setNotificationDateTime(LocalDateTime notificationDateTime) {
        this.notificationDateTime = notificationDateTime;
    }
}
