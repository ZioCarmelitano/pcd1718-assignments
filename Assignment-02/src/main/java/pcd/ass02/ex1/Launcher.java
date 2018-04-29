package pcd.ass02.ex1;

import pcd.ass02.domain.Folder;
import pcd.ass02.domain.SearchResult;
import pcd.ass02.domain.SearchStatistics;
import pcd.ass02.ex1.tasks.FolderSearchTask;
import pcd.ass02.ex1.tasks.SearchResultAccumulatorTask;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

final class Launcher {

    private static long fileWithOccurrencesCount = 0;

    public static void main(String... args) {
        final File path = new File(args[0]);
        final String regex = args[1];
        final int maxDepth = Integer.parseInt(args[2]);

        final Folder rootFolder = Folder.fromDirectory(path, maxDepth);

        long counts;
        long startTime;
        long stopTime;

        final SearchResultAccumulatorTask accumulator = new SearchResultAccumulatorTask(Launcher::accept);
        final Consumer<SearchResult> resultCallback = accumulator::notify;
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(accumulator);

        final ForkJoinPool pool = new ForkJoinPool();

        final FolderSearchTask task = new FolderSearchTask(rootFolder, regex, resultCallback);
        startTime = System.currentTimeMillis();
        counts = pool.invoke(task);
        accumulator.stop();
        executor.shutdown();
        stopTime = System.currentTimeMillis();

        System.out.println();
        System.out.println("Total Occurrences: " + counts);
        System.out.println("Execution time: " + (stopTime - startTime) + " ms");
    }

    private static void accept(SearchStatistics statistics) {
        List<String> files = statistics.getDocumentNames();
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

    private Launcher() {
    }

}
