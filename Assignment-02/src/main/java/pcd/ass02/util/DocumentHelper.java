package pcd.ass02.util;

import pcd.ass02.domain.Document;

public final class DocumentHelper {

    public static long countOccurrences(Document document, String regex) {
        long count = 0;
        for (String line : document.getLines()) {
            count += MatcherHelper.countMatches(regex, line);
        }
        return count;
    }

    private DocumentHelper() {
    }

}
