package pcd.ass02;

import org.reactivestreams.Subscription;
import pcd.ass02.domain.Folder;
import pcd.ass02.domain.SearchStatistics;
import pcd.ass02.ex1.tasks.ForkJoinOccurrencesCounter;
import pcd.ass02.ex2.verticles.VertxOccurrencesCounter;
import pcd.ass02.ex3.RxJavaOccurrencesCounter;
import pcd.ass02.ex3.SearchResultSubscriber;
import pcd.ass02.interactors.OccurrencesCounter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Benchmark {

    private static final int EXERCISE_NUMBER = 3;
    private static final int RUN_NUMBER  = 10;

    private static File path;
    private static String regex;
    private static int maxDepth;
    private static Folder rootFolder;

    public static void main(String[] args) {

        /* Argument parsing*/
        path = new File(args[0]);
        regex = args[1];
        maxDepth = Integer.parseInt(args[2]);

        /* Create File System Representation*/
        rootFolder = Folder.fromDirectory(path, maxDepth);

        /* Key: exercise number, Value: list of execution times for the selected ex.*/
        Map<Integer, List<Long>> executionTimes = new HashMap<>();

        /*Collect execution times*/
        for (int j = 0; j < EXERCISE_NUMBER; j++) {
            System.out.println("Searching with Exercise " + j);
            List<Long> exerciseTimes = new ArrayList<>();
            for (int i = 0; i < RUN_NUMBER; i++) {
                System.out.println("Run number:" + i);
                long result = 0;
                /* Recalculate in case of negative result (exercise 2) */
                while(result <= 0) {
                    result = getSearchResult(j);
                }
                exerciseTimes.add(result);
            }
            executionTimes.put(j, exerciseTimes);
        }

        /*Calculate execution statistics*/
        executionTimes.keySet().forEach(exKey -> {
            long averageTime = (long) executionTimes.get(exKey).stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(Double.NaN);

            long maxTime = executionTimes.get(exKey).stream()
                    .max(Long::compareTo)
                    .orElse(0L);

            long minTime = executionTimes.get(exKey).stream()
                    .min(Long::compareTo)
                    .orElse(0L);

            /*Print statistics*/
            System.out.println("\nExercise " + (exKey + 1) + ":"
                    + "\n--------------------------------");
            System.out.println("Avg Execution Time: " + averageTime + " ms");
            System.out.println("Max Execution Time: " + maxTime + " ms");
            System.out.println("Min Execution Time: " + minTime + " ms" + "\n");
        });

    }

    private static long getSearchResult(int exerciseNumber) {
        switch (exerciseNumber){
            case 0:
                return forkJoinSearch();
            case 1:
                return vertxSearch();
            case 2:
                return reactiveStreamSearch();
            default:
                return 0;
        }
    }


    private static long forkJoinSearch(){
        final OccurrencesCounter counter = new ForkJoinOccurrencesCounter(searchStatistics -> {});

        counter.start();

        final long startTime = System.currentTimeMillis();
        counter.countOccurrences(rootFolder, regex);
        final long stopTime = System.currentTimeMillis();

        counter.stop();

        return stopTime - startTime;
    }


    private static long vertxSearch() {
        Long[] executionTime = new Long[1];

        final OccurrencesCounter counter = new VertxOccurrencesCounter(s -> {});

        counter.start();

        final long startTime = System.currentTimeMillis();
        counter.countOccurrences(rootFolder, regex);
        final long stopTime = System.currentTimeMillis();

        counter.stop();

        return executionTime[0];
    }


    private static long reactiveStreamSearch() {
        final long[] executionTime = new long[1];
        final OccurrencesCounter counter = new RxJavaOccurrencesCounter(new SearchResultSubscriber() {
            private long startTime;

            @Override
            protected void onNext(SearchStatistics statistics) {}

            @Override
            protected void onComplete(long totalOccurrences) {
                final long endTime = System.currentTimeMillis();
                executionTime[0] = endTime - startTime;
            }

            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
                startTime = System.currentTimeMillis();
            }
        });

        counter.start();
        counter.countOccurrences(rootFolder, regex);
        counter.stop();

        return executionTime[0];
    }
}
