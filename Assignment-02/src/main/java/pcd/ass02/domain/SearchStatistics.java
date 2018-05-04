package pcd.ass02.domain;

import java.util.Map;
import java.util.Set;

public class SearchStatistics {

    private final Map<String, Long> documentResults;
    private final double matchingRate;
    private final double averageMatches;

    public SearchStatistics(Map<String, Long> documentResults, double matchingRate, double averageMatches) {
        this.documentResults = documentResults;
        this.matchingRate = matchingRate;
        this.averageMatches = averageMatches;
    }

    public Set<String> getDocumentNames() {
        return documentResults.keySet();
    }

    public Map<String, Long> getDocumentResults(){
        return documentResults;
    }

    public double getMatchingRate() {
        return matchingRate;
    }

    public double getAverageMatches() {
        return averageMatches;
    }

}
