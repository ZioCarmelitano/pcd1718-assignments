package pcd.ass02.domain;

import java.util.Collections;
import java.util.List;

public class SearchStatistics {

    private final List<String> documentNames;
    private final double matchingRate;
    private final double averageMatches;

    public SearchStatistics(List<String> documentNames, double matchingRate, double averageMatches) {
        this.documentNames = Collections.unmodifiableList(documentNames);
        this.matchingRate = matchingRate;
        this.averageMatches = averageMatches;
    }

    public List<String> getDocumentNames() {
        return documentNames;
    }

    public double getMatchingRate() {
        return matchingRate;
    }

    public double getAverageMatches() {
        return averageMatches;
    }
}
