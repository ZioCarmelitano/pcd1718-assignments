package fj;

import java.util.List;

public interface SearchResultUpdateListener {
    void onEvent(List<String> list, Double matchingRate, Double averageMatches);
}
