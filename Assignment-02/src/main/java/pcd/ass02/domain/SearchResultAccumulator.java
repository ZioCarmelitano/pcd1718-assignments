package pcd.ass02.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SearchResultAccumulator {

    private long fileCount;
    private long totalOccurrences;

    private double averageMatches;

    private final Map<String, Long> documentResults;

    public SearchResultAccumulator() {
        documentResults = new HashMap<>();
    }

    public SearchStatistics updateStatistics(SearchResult result) {
        fileCount++;

        final long occurrences = result.getOccurrences();
        long filesWithOccurrences = documentResults.size();
        if (occurrences > 0) {
            totalOccurrences += occurrences;
            documentResults.put(result.getDocumentName(), occurrences);
            filesWithOccurrences++;
            averageMatches = ((double) totalOccurrences) / ((double) filesWithOccurrences);
        }
        final double matchingRate = ((double) filesWithOccurrences) / ((double) fileCount);

        return new SearchStatistics(documentResults, matchingRate, averageMatches);
    }

    public void resetStatistics() {
        fileCount = 0;
        totalOccurrences = 0;
        averageMatches = 0.0;
        documentResults.clear();
    }

    public long getTotalOccurrences() {
        return totalOccurrences;
    }

}
