package ru.disdev.entity;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

@MappedSuperclass
public class Post {

    private String tags;
    private String text;
    @Transient
    private List<TelegramAttachment> attachments = new ArrayList<>();

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        StringTokenizer tokenizer = new StringTokenizer(tags, ",");
        while (tokenizer.hasMoreTokens()) {
            builder.append("#").append(tokenizer.nextToken().toLowerCase().trim()).append("@idb1409group\n");
        }
        return builder.append(text).toString();
    }

    public List<TelegramAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<TelegramAttachment> attachments) {
        this.attachments = attachments;
    }
}
