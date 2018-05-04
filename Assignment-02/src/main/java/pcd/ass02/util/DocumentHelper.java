package pcd.ass02.util;

import pcd.ass02.domain.Document;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DocumentHelper {

    public static long countOccurrences(Document document, String regex) {
        final Pattern pattern = Pattern.compile(regex);

        long count = 0;
        for (String line : document.getLines()) {
            count += countOccurrences(line, pattern);
        }
        return count;
    }

    private static long countOccurrences(String input, Pattern pattern) {
        final Matcher matcher = pattern.matcher(input);

        long count = 0;
        while (matcher.find())
            count++;
        return count;
    }

    private DocumentHelper() {
    }

}
