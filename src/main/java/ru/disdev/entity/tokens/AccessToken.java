package ru.disdev.entity.tokens;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tokens")
public class AccessToken {

    public static final int USER_TOKEN_ID = 1;
    public static final int GROUP_TOKEN_ID = 2;

    @Id
    private int id;
    private String token;
    private int userId;

    public AccessToken() {

    }

    public int getId() {
        return id;
    }

    ;

    public void setId(int id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
