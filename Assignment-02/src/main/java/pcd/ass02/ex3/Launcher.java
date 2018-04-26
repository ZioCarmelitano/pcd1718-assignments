package pcd.ass02.ex3;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Subscription;
import pcd.ass02.domain.Document;
import pcd.ass02.domain.Folder;
import pcd.ass02.domain.SearchResult;
import pcd.ass02.domain.SearchStatistics;
import pcd.ass02.ex1.OccurrencesCounter;

import java.io.File;
import java.util.List;

final class Launcher {

    public static void main(String... args) {
        File path = new File(args[0]);
        String regex = args[1];
        int maxDepth = Integer.parseInt(args[2]);

        getDocuments(Folder.fromDirectory(path, maxDepth))
                .subscribeOn(Schedulers.computation())
                .map(toSearchResult(regex))
                .blockingSubscribe(new SearchResultAccumulator() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        startTime = System.currentTimeMillis();
                    }

                    private int fileWithOccurrencesCount;
                    private long startTime;

                    @Override
                    public void onNext(SearchStatistics statistics) {
                        final List<String> files = statistics.getMatches();
                        final double averageMatches = statistics.getAverageMatches();
                        final double matchingRate = statistics.getMatchingRate();

                        if (files.size() > fileWithOccurrencesCount) {
                            fileWithOccurrencesCount = files.size();
                            System.out.println(files);
                            System.out.println("Matching rate: " + matchingRate);
                            System.out.println("Average: " + averageMatches);
                            System.out.println("Files with occurrences: " + files.size());
                        }
                    }

                    @Override
                    protected void onComplete(long totalOccurrences) {
                        final long endTime = System.currentTimeMillis();

                        System.out.println("\nTotal occurrences: " + totalOccurrences);
                        System.out.println("Execution time: " + (endTime - startTime) + "ms");
                    }
                });
    }

    private static Function<? super Document, ? extends SearchResult> toSearchResult(String regex) {
        return document -> {
            final long occurrences = OccurrencesCounter.countOccurrences(document, regex);
            return new SearchResult(document.getName(), occurrences);
        };
    }

    private static Flowable<Document> getDocuments(Folder folder) {
        return Flowable.fromIterable(folder.getSubFolders())
                .flatMap(Launcher::getDocuments)
                .mergeWith(Flowable.fromIterable(folder.getDocuments()));
    }

    private Launcher() {
    }

}
