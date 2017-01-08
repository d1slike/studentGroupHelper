package ru.disdev.entity.vk;

import java.util.HashMap;
import java.util.Map;

public class VkPost {
    private String tag;
    private String messageText;
    private Map<String, String> attachments = new HashMap<>();

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public Map<String, String> getAttachments() {
        return attachments;
    }

    public void setAttachments(Map<String, String> attachments) {
        this.attachments = attachments;
    }
}
