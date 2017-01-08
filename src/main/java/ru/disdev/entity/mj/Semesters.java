package ru.disdev.entity.mj;

import java.util.List;

public class Semesters {

    private List<String> semesters;
    private String surname;
    private String initials;
    private String stgroup;

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public String getStgroup() {
        return stgroup;
    }

    public void setStgroup(String stgroup) {
        this.stgroup = stgroup;
    }

    public List<String> getSemesters() {
        return semesters;
    }

    public void setSemesters(List<String> semesters) {
        this.semesters = semesters;
    }
}
