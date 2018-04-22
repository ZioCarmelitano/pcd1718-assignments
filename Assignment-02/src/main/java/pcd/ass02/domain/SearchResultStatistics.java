package pcd.ass02.domain;

import java.util.Collections;
import java.util.List;

public class SearchResultStatistics {

    private final List<String> matches;
    private final double matchingRate;
    private final double averageMatches;

    public SearchResultStatistics(List<String> matches, double matchingRate, double averageMatches) {
        this.matches = Collections.unmodifiableList(matches);
        this.matchingRate = matchingRate;
        this.averageMatches = averageMatches;
    }

    public List<String> getMatches() {
        return matches;
    }

    public double getMatchingRate() {
        return matchingRate;
    }

    public double getAverageMatches() {
        return averageMatches;
    }
}
