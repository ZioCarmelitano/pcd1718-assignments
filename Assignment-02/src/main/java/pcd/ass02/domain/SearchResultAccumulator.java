package pcd.ass02.domain;

import java.util.ArrayList;
import java.util.List;

public class SearchResultAccumulator {

    private long fileCount;
    private long totalOccurrences;

    private double averageMatches;

    private final List<String> documentNames;

    public SearchResultAccumulator() {
        documentNames = new ArrayList<>();
    }

    public SearchStatistics updateStatistics(SearchResult result) {
        fileCount++;

        final long occurrences = result.getOccurrences();
        long filesWithOccurrences = documentNames.size();
        if (occurrences > 0) {
            totalOccurrences += occurrences;
            documentNames.add(result.getDocumentName());
            filesWithOccurrences++;
            averageMatches = ((double) totalOccurrences) / ((double) filesWithOccurrences);
        }
        final double matchingRate = ((double) filesWithOccurrences) / ((double) fileCount);

        return new SearchStatistics(documentNames, matchingRate, averageMatches);
    }

    public void resetStatistics() {
        fileCount = 0;
        totalOccurrences = 0;
        averageMatches = 0.0;
        documentNames.clear();
    }

    public long getTotalOccurrences() {
        return totalOccurrences;
    }

}
