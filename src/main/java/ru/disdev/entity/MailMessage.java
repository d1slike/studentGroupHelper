package ru.disdev.entity;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MailMessage {
    private String tag;
    private String message;
    private List<File> attachments = new ArrayList<>();

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<File> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<File> attachments) {
        this.attachments = attachments;
    }
}
