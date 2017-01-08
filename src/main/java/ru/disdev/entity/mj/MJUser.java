package ru.disdev.entity.mj;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class MJUser {
    @Id
    private int userId;
    private String login;
    private String password;

    public MJUser() {

    }

    public MJUser(int userId, String login, String password) {
        this.userId = userId;
        this.login = login;
        this.password = password;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
