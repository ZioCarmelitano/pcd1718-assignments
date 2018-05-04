package pcd.ass02.ex3;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import pcd.ass02.domain.Document;
import pcd.ass02.domain.Folder;
import pcd.ass02.domain.SearchResult;
import pcd.ass02.interactors.AbstractOccurrencesCounter;
import pcd.ass02.util.DocumentHelper;

public class RxJavaOccurrencesCounter extends AbstractOccurrencesCounter {

    private final SearchResultSubscriber subscriber;

    public RxJavaOccurrencesCounter(SearchResultSubscriber subscriber) {
        super(subscriber.getAccumulator());
        this.subscriber = subscriber;
    }

    @Override
    protected long doCount(Folder rootFolder, String regex) {
            getDocuments(rootFolder)
                    .subscribeOn(Schedulers.computation())
                    .map(toSearchResult(regex))
                    .blockingSubscribe(subscriber);
            return getTotalOccurrences();
        }

        private static Function<? super Document, ? extends SearchResult> toSearchResult (String regex){
            return document -> {
                final long occurrences = DocumentHelper.countOccurrences(document, regex);
                return new SearchResult(document.getName(), occurrences);
            };
        }

        private static Flowable<Document> getDocuments (Folder folder){
            return Flowable.fromIterable(folder.getSubFolders())
                    .flatMap(RxJavaOccurrencesCounter::getDocuments)
                    .mergeWith(Flowable.fromIterable(folder.getDocuments()));
        }

    }
