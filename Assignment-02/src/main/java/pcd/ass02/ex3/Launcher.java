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

import javax.swing.*;
import java.io.File;
import java.util.List;

final class Launcher {

    public static void main(String... args) {
        final File path = new File(args[0]);
        final String regex = args[1];
        final int maxDepth = Integer.parseInt(args[2]);

        final OccurrencesCounter counter = new RxJavaOccurrencesCounter(new SearchResultSubscriber() {
            private long startTime;
            private long filesWithOccurrencesCount;

            @Override
            protected void onNext(SearchStatistics statistics) {
                final List<String> documentNames = statistics.getDocumentNames();
                final double averageMatches = statistics.getAverageMatches();
                final double matchingRate = statistics.getMatchingRate();

                if (documentNames.size() > filesWithOccurrencesCount) {
                    filesWithOccurrencesCount = documentNames.size();
                    System.out.println(documentNames);
                    System.out.println("Matching rate: " + matchingRate);
                    System.out.println("Average: " + averageMatches);
                    System.out.println("Files with occurrences: " + documentNames.size());
                }
            }

            @Override
            protected void onComplete(long totalOccurrences) {
                final long endTime = System.currentTimeMillis();

                System.out.println();
                System.out.println("Total occurrences: " + totalOccurrences);
                System.out.println("Execution time: " + (endTime - startTime) + "ms");
            }

            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
                startTime = System.currentTimeMillis();
            }
        });

        final Folder rootFolder = Folder.fromDirectory(path, maxDepth);
        counter.start();
        counter.countOccurrences(rootFolder, regex);
        counter.stop();
    }

    private Launcher() {
    }

}
