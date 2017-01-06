package ru.disdev.entity;

public class TelegramAttachment {
    private final String id;
    private final String fileExtension;
    private String name;

    public TelegramAttachment(String id, String fileExtension) {
        this.id = id;
        this.fileExtension = fileExtension;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileExtension() {
        return fileExtension;
    }

}
