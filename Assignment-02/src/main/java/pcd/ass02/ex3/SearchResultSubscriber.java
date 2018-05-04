package pcd.ass02.ex3;

import io.reactivex.exceptions.OnErrorNotImplementedException;
import org.reactivestreams.Subscriber;
import pcd.ass02.domain.SearchResult;
import pcd.ass02.domain.SearchResultAccumulator;
import pcd.ass02.domain.SearchStatistics;

abstract class SearchResultSubscriber implements Subscriber<SearchResult> {

    private final SearchResultAccumulator accumulator;

    protected SearchResultSubscriber() {
        accumulator = new SearchResultAccumulator();
    }

    @Override
    public final void onNext(SearchResult result) {
        onNext(accumulator.updateStatistics(result));
    }

    @Override
    public final void onComplete() {
        onComplete(accumulator.getTotalOccurrences());
    }

    @Override
    public void onError(Throwable e) {
        throw new OnErrorNotImplementedException(e);
    }

    SearchResultAccumulator getAccumulator() {
        return accumulator;
    }

    protected abstract void onNext(SearchStatistics statistics);

    protected abstract void onComplete(long totalOccurrences);

}
