package pcd.ass02.ex3;

import org.reactivestreams.Subscription;
import pcd.ass02.domain.Folder;
import pcd.ass02.domain.SearchStatistics;
import pcd.ass02.interactors.OccurrencesCounter;

import java.io.File;
import java.util.Map;

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
                final Map<String, Long> documentResults = statistics.getDocumentResults();
                final double averageMatches = statistics.getAverageMatches();
                final double matchingRate = statistics.getMatchingRate();

                if (documentResults.size() > filesWithOccurrencesCount) {
                    filesWithOccurrencesCount = documentResults.size();
                    System.out.println(documentResults);
                    System.out.println("Matching rate: " + matchingRate);
                    System.out.println("Average: " + averageMatches);
                    System.out.println("Files with occurrences: " + documentResults.size());
                }
            }

            @Override
            protected void onComplete(long totalOccurrences) {
                final long executionTime = System.currentTimeMillis() - startTime;

                System.out.println();
                System.out.println("Total occurrences: " + totalOccurrences);
                System.out.println("Execution time: " + executionTime + "ms");
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
