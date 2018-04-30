package pcd.ass02.ex1;

import pcd.ass02.domain.Folder;
import pcd.ass02.domain.SearchStatistics;
import pcd.ass02.ex1.tasks.FolderSearchTask;
import pcd.ass02.ex1.tasks.SearchResultAccumulatorTask;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public final class Launcher {

    private static long filesWithOccurrencesCount = 0;

    public static void main(String... args) {
        final File path = new File(args[0]);
        final String regex = args[1];
        final int maxDepth = Integer.parseInt(args[2]);

        final Folder rootFolder = Folder.fromDirectory(path, maxDepth);

        final SearchResultAccumulatorTask accumulator = new SearchResultAccumulatorTask(Launcher::accept);
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(accumulator);

        final ForkJoinPool pool = new ForkJoinPool();
        final FolderSearchTask task = new FolderSearchTask(rootFolder, regex, accumulator::notify);

        final long startTime = System.currentTimeMillis();
        final long totalOccurrences = pool.invoke(task);
        final long stopTime = System.currentTimeMillis();

        accumulator.stop();
        executor.shutdown();

        System.out.println("Total Occurrences: " + totalOccurrences);
        System.out.println("Execution time: " + (stopTime - startTime) + " ms");
    }

    private static void accept(SearchStatistics statistics) {
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

    private Launcher() {
    }

}
