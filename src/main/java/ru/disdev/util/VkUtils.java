package ru.disdev.util;

public class VkUtils {
    public static String wallAttachment(int owner, int postId) {
        return "wall" + owner + "_" + postId;
    }
}
