/*
 * Fork-Join example, adapted from
 * http://www.oracle.com/technetwork/articles/java/fork-join-422606.html
 *
 */
package pcd.ass02.ex1;

import pcd.ass02.domain.Folder;
import pcd.ass02.domain.SearchResult;
import pcd.ass02.domain.SearchResultStatistics;
import pcd.ass02.ex1.tasks.SearchResultAccumulatorTask;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

class Launcher {

    private static long fileWithOccurrencesCount = 0;

    public static void main(String[] args) {
        OccurrencesCounter occurrencesCounter = new OccurrencesCounter();
        Folder folder = Folder.fromDirectory(new File(args[0]), Integer.parseInt(args[2]));

        long counts;
        long startTime;
        long stopTime;

        Consumer<SearchResultStatistics> listener = Launcher::accept;
        SearchResultAccumulatorTask accumulator = new SearchResultAccumulatorTask(listener);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(accumulator);

        startTime = System.currentTimeMillis();
        counts = occurrencesCounter.countOccurrencesInParallel(folder, args[1], (document, count) -> accumulator.notifyEvent(new SearchResult(document.getName(), count)));
        accumulator.stop();
        executor.shutdown();
        stopTime = System.currentTimeMillis();

        System.out.println();
        System.out.println("Total Occurrences: " + counts);
        System.out.println("Execution time: " + (stopTime - startTime) + "ms");
    }

    private static void accept(SearchResultStatistics statistics) {
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
}
