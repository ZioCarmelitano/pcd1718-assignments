package pcd.ass02.ex3;

import io.reactivex.Observer;
import pcd.ass02.domain.SearchResult;
import pcd.ass02.domain.SearchResultStatistics;

import java.util.ArrayList;
import java.util.List;

abstract class SearchResultAccumulator implements Observer<SearchResult> {

    private long fileCount;
    private long fileWithOccurrences;
    long totalOccurrences;
    private double averageMatches;
    private final List<String> files;

    public SearchResultAccumulator() {
        this.files = new ArrayList<>();
    }

    @Override
    public void onNext(SearchResult searchResult) {
        long occurrences = searchResult.getCount();
        String documentName = searchResult.getDocumentName();

        fileCount++;
        if (occurrences > 0) {
            files.add(documentName);
            fileWithOccurrences++;
            totalOccurrences += occurrences;
            averageMatches = ((double) totalOccurrences) / ((double) fileWithOccurrences);
        }
        final double matchingRate = ((double) fileWithOccurrences) / ((double) fileCount);

        SearchResultStatistics statistics = new SearchResultStatistics(files, matchingRate, averageMatches);
        onNext(statistics);
    }

    protected abstract void onNext(SearchResultStatistics statistics);

}
