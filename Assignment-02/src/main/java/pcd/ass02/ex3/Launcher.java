package pcd.ass02.ex3;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import pcd.ass02.domain.Document;
import pcd.ass02.domain.Folder;
import pcd.ass02.domain.SearchResult;
import pcd.ass02.domain.SearchResultStatistics;
import pcd.ass02.ex1.OccurrencesCounter;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static pcd.ass02.domain.Folder.fromDirectory;

class Launcher {

    public static void main(String[] args) {
        File path = new File(args[0]);
        String regex = args[1];
        int maxDepth = Integer.parseInt(args[2]);

        getDocuments(fromDirectory(path, maxDepth))
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.trampoline())
                .map(toSearchResult(regex))
                .blockingSubscribe(new SearchResultAccumulator() {
                    private int fileWithOccurrencesCount;
                    private long startTime;

                    @Override
                    public void onSubscribe(Disposable d) {
                        startTime = System.currentTimeMillis();
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                        final long endTime = System.currentTimeMillis();
                        System.out.println("\nTotal occurrences: " + totalOccurrences);
                        System.out.println("Execution time: " + (endTime - startTime) + "ms");
                    }

                    @Override
                    public void onNext(SearchResultStatistics statistics) {
                        List<String> files = statistics.getMatches();
                        double averageMatches = statistics.getAverageMatches();
                        double matchingRate = statistics.getMatchingRate();

                        if (files.size() > fileWithOccurrencesCount) {
                            fileWithOccurrencesCount = files.size();
                            System.out.println(files);
                            System.out.println("Matching rate: " + matchingRate);
                            System.out.println("Average: " + averageMatches);
                            System.out.println("Files with occurrences: " + files.size());
                        }
                    }
                });
    }

    private static Function<? super Document, ? extends SearchResult> toSearchResult(String regex) {
        return document -> {
            long occurrences = OccurrencesCounter.occurrencesCount(document, regex);
            return new SearchResult(document.getName(), occurrences);
        };
    }

    private static Observable<Document> getDocuments(Folder folder) {
        Observable<Document> documents = Observable.fromIterable(folder.getDocuments());

        return Observable.merge(folder.getSubFolders().parallelStream()
                .map(Launcher::getDocuments)
                .collect(Collectors.toList()))
                .mergeWith(documents);
    }

}
