package pcd.ass02.ex1.tasks;

import java.util.List;

@FunctionalInterface
public interface SearchResultUpdateListener {

    void onEvent(List<String> list, Double matchingRate, Double averageMatches);

}
