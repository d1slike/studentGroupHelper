package ru.disdev.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtils {
    public static String loadFileContentAsString(String fileName) {
        StringBuilder builder = new StringBuilder();
        try {
            Files.lines(Paths.get(fileName)).forEach(builder::append);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }
}
