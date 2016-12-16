package ru.disdev.util;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class VkUtils {
    public static String wallAttachment(int owner, int postId) {
        return "wall" + owner + "_" + postId;
    }

    public static String handleNewPostBody(JsonNode post, List<String> attachmentsUrlList) {
        StringBuilder message = new StringBuilder("Новая запись в группе:\n")
                .append(post.get("text").asText());
        JsonNode attachments = post.get("attachments");
        if (attachments != null && attachments.size() > 0) {
            message.append("\nВложения:\n");
            attachments.forEach(jsonNode -> {
                String type = jsonNode.get("type").asText();
                if (type.equals("photo") || type.equals("doc") || type.equals("link")) {
                    JsonNode attachment = jsonNode.get(type);
                    if (attachment != null) {
                        String url = null;
                        String description = null;
                        switch (type) {
                            case "photo":
                                url = attachment.get("photo_2560").asText();
                                description = attachment.get("text").asText();
                                break;
                            case "doc":
                            case "link":
                                url = attachment.get("url").asText();
                                description = attachment.get("title").asText();
                                break;
                        }
                        if (url != null && url.isEmpty()) {
                            message.append(url).append(" - ").append(description).append("\n");
                            attachmentsUrlList.add(url);
                        }

                    }
                }

            });
        }
        return message.toString();
    }
}
