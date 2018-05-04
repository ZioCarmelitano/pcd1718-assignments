package pcd.ass02.ex1;

import pcd.ass02.domain.Folder;
import pcd.ass02.domain.SearchStatistics;
import pcd.ass02.ex1.tasks.ForkJoinOccurrencesCounter;
import pcd.ass02.interactors.OccurrencesCounter;

import java.io.File;
import java.util.List;
import java.util.Map;


public final class Launcher {

    private static long filesWithOccurrencesCount = 0;

    public static void main(String... args) {
        final File path = new File(args[0]);
        final String regex = args[1];
        final int maxDepth = Integer.parseInt(args[2]);

        final Folder rootFolder = Folder.fromDirectory(path, maxDepth);

        final OccurrencesCounter counter = new ForkJoinOccurrencesCounter(Launcher::accept);

        counter.start();

        final long startTime = System.currentTimeMillis();
        final long totalOccurrences = counter.countOccurrences(rootFolder, regex);
        final long stopTime = System.currentTimeMillis();

        counter.stop();

        System.out.println();
        System.out.println("Total Occurrences: " + totalOccurrences);
        System.out.println("Execution time: " + (stopTime - startTime) + " ms");
    }

    private static void accept(SearchStatistics statistics) {
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

    private Launcher() {
    }

}
