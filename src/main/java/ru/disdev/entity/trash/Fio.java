package ru.disdev.entity.trash;

public class Fio {
    private String firstName;
    private String lastName;
    private String fathersName;

    public Fio(String firstName, String lastName, String fathersName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.fathersName = fathersName;
    }

    public String toString() {
        return lastName + " " + firstName + " " + fathersName;
    }
}
