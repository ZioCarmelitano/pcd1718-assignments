package pcd.ass02.ex3;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Subscription;
import pcd.ass02.domain.Document;
import pcd.ass02.domain.Folder;
import pcd.ass02.domain.SearchResult;
import pcd.ass02.domain.SearchStatistics;
import pcd.ass02.interactors.OccurrencesCounter;
import pcd.ass02.util.DocumentHelper;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

public class RxJavaOccurrencesCounter implements OccurrencesCounter {

    private final SearchResultSubscriber subscriber;

    public RxJavaOccurrencesCounter(SearchResultSubscriber subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public long countOccurrences(Folder rootFolder, String regex) {
        final AtomicLong result = new AtomicLong();
        getDocuments(rootFolder)
                .subscribeOn(Schedulers.computation())
                .map(toSearchResult(regex))
                .blockingSubscribe(subscriber);
        return result.get();
    }

    private static Function<? super Document, ? extends SearchResult> toSearchResult(String regex) {
        return document -> {
            final long occurrences = DocumentHelper.countOccurrences(document, regex);
            return new SearchResult(document.getName(), occurrences);
        };
    }

    private static Flowable<Document> getDocuments(Folder folder) {
        return Flowable.fromIterable(folder.getSubFolders())
                .flatMap(RxJavaOccurrencesCounter::getDocuments)
                .mergeWith(Flowable.fromIterable(folder.getDocuments()));
    }

}
