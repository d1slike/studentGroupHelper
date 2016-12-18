package ru.disdev.entity;

import java.util.Date;

public class DropBoxFile {
    private final String name;
    private final String publicLink;
    private final Date updateDate;

    public DropBoxFile(String name, String url, Date updateDate) {
        this.name = name;
        this.publicLink = url;
        this.updateDate = updateDate;
    }

    public String getName() {
        return name;
    }

    public String getPublicLink() {
        return publicLink;
    }

    public Date getUpdateDate() {
        return updateDate;
    }
}
