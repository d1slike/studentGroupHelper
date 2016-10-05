package ru.disdev.entity;

import java.util.StringTokenizer;

public class Post {

    private String tags;
    private String text;

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
            builder.append("#").append(tokenizer.nextToken()).append("@idb1409group\n");
        }
        return builder.append(text).toString();
    }
}
