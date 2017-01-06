package ru.disdev.util;

import ru.disdev.StudentHelperApplication;

import java.io.File;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IOUtils {
    public static InputStream resourceAsStream(String fileName) {
        return StudentHelperApplication.class.getResourceAsStream(fileName);
    }

    public static File changeFileExtension(String source, String newExtension) {
        String target;
        String currentExtension = getFileExtension(source);

        if (currentExtension.equals("")) {
            target = source + "." + newExtension;
        } else {
            target = source.replaceFirst(Pattern.quote("." +
                    currentExtension) + "$", Matcher.quoteReplacement("." + newExtension));

        }
        File destination = new File(target);
        new File(source).renameTo(destination);
        return destination;
    }

    private static String getFileExtension(String f) {
        String ext = "";
        int i = f.lastIndexOf('.');
        if (i > 0 && i < f.length() - 1) {
            ext = f.substring(i + 1);
        }
        return ext;
    }
}
