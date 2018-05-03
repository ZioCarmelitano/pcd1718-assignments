package pcd.ass02.ex2;

import pcd.ass02.domain.Folder;
import pcd.ass02.domain.SearchStatistics;
import pcd.ass02.interactors.OccurrencesCounter;

import java.io.File;
import java.util.List;

final class Launcher {

    private static int filesWithOccurrencesCount;

    public static void main(String... args) {
        final File path = new File(args[0]);
        final String regex = args[1];
        final int maxDepth = Integer.parseInt(args[2]);

        final Folder rootFolder = Folder.fromDirectory(path, maxDepth);

        final OccurrencesCounter counter = new VertxOccurrencesCounter(Launcher::handleResult);

        counter.start();

        final long startTime = System.currentTimeMillis();
        final long totalOccurrences = counter.countOccurrences(rootFolder, regex);
        final long executionTime = System.currentTimeMillis() - startTime;

        counter.stop();
        System.out.println("Total occurrences: " + totalOccurrences);
        System.out.println("Execution time: " + executionTime + " ms");
    }

    private static void handleResult(SearchStatistics statistics) {
        final List<String> files = statistics.getDocumentNames();
        final double averageMatches = statistics.getAverageMatches();
        final double matchingRate = statistics.getMatchingRate();

        if (files.size() > filesWithOccurrencesCount) {
            filesWithOccurrencesCount = files.size();
            System.out.println(files);
            System.out.println("Matching rate: " + matchingRate);
            System.out.println("Average: " + averageMatches);
            System.out.println("Files with occurrences: " + files.size());
        }
    }

    private Launcher() {
    }

}
