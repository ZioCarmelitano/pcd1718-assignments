package pcd.ass02.ex3;

import io.reactivex.exceptions.OnErrorNotImplementedException;
import org.reactivestreams.Subscriber;
import pcd.ass02.domain.SearchResult;
import pcd.ass02.domain.SearchStatistics;

import java.util.ArrayList;
import java.util.List;

abstract class SearchResultAccumulator implements Subscriber<SearchResult> {

    private long fileCount;
    private long fileWithOccurrences;
    private long totalOccurrences;
    private double averageMatches;
    private final List<String> files;

    SearchResultAccumulator() {
        this.files = new ArrayList<>();
    }

    @Override
    public final void onNext(SearchResult result) {
        long occurrences = result.getCount();
        String documentName = result.getDocumentName();

        fileCount++;
        if (occurrences > 0) {
            files.add(documentName);
            fileWithOccurrences++;
            totalOccurrences += occurrences;
            averageMatches = ((double) totalOccurrences) / ((double) fileWithOccurrences);
        }
        final double matchingRate = ((double) fileWithOccurrences) / ((double) fileCount);

        onNext(new SearchStatistics(files, matchingRate, averageMatches));
    }

    @Override
    public final void onComplete() {
        onComplete(totalOccurrences);
    }

    @Override
    public void onError(Throwable e) {
        throw new OnErrorNotImplementedException(e);
    }

    protected abstract void onNext(SearchStatistics statistics);

    protected abstract void onComplete(long totalOccurrences);

}
