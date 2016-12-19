package ru.disdev.util;

import com.fasterxml.jackson.databind.JsonNode;
import ru.disdev.entity.VkPost;
import ru.disdev.service.FileService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VkUtils {

    private static final Pattern TAG_PATTERN = Pattern.compile("#\\S+\\b");

    public static String wallAttachment(int owner, int postId) {
        return "wall" + owner + "_" + postId;
    }

    public static VkPost handleNewPostBody(JsonNode post) {
        StringBuilder message = new StringBuilder("Новая запись в группе:\n")
                .append(post.get("text").asText());
        VkPost vkPost = new VkPost();
        JsonNode attachmentsNode = post.get("attachments");
        if (attachmentsNode != null && attachmentsNode.size() > 0) {
            message.append("\nВложения:\n");
            attachmentsNode.forEach(jsonNode -> {
                String type = jsonNode.get("type").asText();
                if (type.equals("photo") || type.equals("doc") || type.equals("link")) {
                    JsonNode attachment = jsonNode.get(type);
                    if (attachment != null) {
                        String url = null;
                        String description = null;
                        switch (type) {
                            case "photo":
                                if (attachment.has("photo_2560")) {
                                    url = attachment.get("photo_2560").asText();
                                } else if (attachment.has("photo_1280")) {
                                    url = attachment.get("photo_1280").asText();
                                } else if (attachment.has("photo_807")) {
                                    url = attachment.get("photo_807").asText();
                                }
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
                            if (!type.equals("link")) {
                                vkPost.getAttachments().put(url, description);
                            }
                        }

                    }
                }

            });
        }
        String fullMessage = message.toString();
        vkPost.setMessageText(fullMessage);
        vkPost.setTag(getTag(fullMessage));
        return vkPost;
    }

    public static String getTag(String message) {
        Matcher matcher = TAG_PATTERN.matcher(message);
        if (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            return message.substring(start + 1, end);
        }
        return FileService.UNDEFINED_CATEGORY;
    }
}
