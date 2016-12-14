package ru.disdev.util;

import ru.disdev.StudentHelperApplication;

import java.io.InputStream;

public class IOUtils {
    public static InputStream resourceAsStream(String fileName) {
        return StudentHelperApplication.class.getResourceAsStream(fileName);
    }
}
