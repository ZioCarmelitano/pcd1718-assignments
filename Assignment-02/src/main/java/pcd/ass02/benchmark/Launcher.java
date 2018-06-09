package pcd.ass02.benchmark;

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

class Launcher {

    private static final int EXERCISE_NUMBER = 3;
    private static final int RUN_NUMBER = 100;

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
        for (int j = 1; j <= EXERCISE_NUMBER; j++) {
            System.out.println("Searching with Exercise " + j);
            final OccurrencesCounter counter = getOccurrencesCounter(j);
            executionTimes.put(j, getExecutionTimes(counter, RUN_NUMBER));
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
            System.out.println("\nExercise " + exKey + ":"
                    + "\n--------------------------------");
            System.out.println("Avg Execution Time: " + averageTime + " ms");
            System.out.println("Max Execution Time: " + maxTime + " ms");
            System.out.println("Min Execution Time: " + minTime + " ms" + "\n");
        });

    }

    private static OccurrencesCounter getOccurrencesCounter(int exerciseNumber) {
        switch (exerciseNumber) {
            case 1:
                return new ForkJoinOccurrencesCounter(searchStatistics -> {
                });
            case 2:
                return new VertxOccurrencesCounter(s -> {
                });
            case 3:
                return new RxJavaOccurrencesCounter(new SearchResultSubscriber() {
                    @Override
                    protected void onNext(SearchStatistics statistics) {
                    }

                    @Override
                    protected void onComplete(long totalOccurrences) {
                    }

                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(Long.MAX_VALUE);
                    }
                });
            default:
                throw new IllegalStateException("Illegal exercise number: " + exerciseNumber);
        }
    }

    private static List<Long> getExecutionTimes(OccurrencesCounter counter, int numberOfRuns) {
        List<Long> executionTimes = new ArrayList<>();
        counter.start();
        for (int i = 0; i < numberOfRuns; i++) {
            System.out.println("Run #" + (i + 1));
            final long startTime = System.currentTimeMillis();
            counter.countOccurrences(rootFolder, regex);
            final long executionTime = System.currentTimeMillis() - startTime;
            counter.reset();
            executionTimes.add(executionTime);
        }
        counter.stop();
        return executionTimes;
    }

}
