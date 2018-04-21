/*
 * Fork-Join example, adapted from
 * http://www.oracle.com/technetwork/articles/java/fork-join-422606.html
 * 
 */
package fj;

import java.io.*;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestWordCounter {    

    private static long fileWithOccurrencesCount = 0;

    public static void main(String[] args) throws IOException {
        OccurrencesCounter occurrencesCounter = new OccurrencesCounter();
        Folder folder = Folder.fromDirectory(new File(args[0]), Integer.parseInt(args[3]));
        
        final int repeatCount = Integer.parseInt(args[2]);
        long counts = 0;
        long startTime;
        long stopTime;
        
        long[] forkedThreadTimes = new long[repeatCount];

        SearchResultUpdateListener listener = (files, matching, average) -> {
            if (files.size() > fileWithOccurrencesCount) {
                fileWithOccurrencesCount = files.size();
                System.out.println(files);
                System.out.println("Matching rate: " + matching);
                System.out.println("Average: " + average);
                System.out.println("Files with occurences: " + files.size());
            }
        };
        Statistic statistic = new Statistic(listener);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(statistic);

        for (int i = 0; i < repeatCount; i++) {
            startTime = System.currentTimeMillis();
            counts = occurrencesCounter.countOccurrencesInParallel(folder, args[1], (document, count) -> statistic.putSearchResult(new SearchResult(document.getName(), count)));
            statistic.stop();
            executor.shutdown();
            stopTime = System.currentTimeMillis();
            forkedThreadTimes[i] = (stopTime - startTime);
        }

        for (int i = 0; i < repeatCount; i++) {
            System.out.println();
            System.out.println("Total Occurrences: " + counts);
            System.out.println("Execution times: " + forkedThreadTimes[i] + "ms");
        }

        System.out.println();
    }
}
