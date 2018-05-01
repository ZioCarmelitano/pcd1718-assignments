package pcd.ass02;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Benchmark {

    private static final int EXERCISE_NUMBER = 3;
    private static final int RUN_NUMBER  = 5;

    public static void main(String[] args) {

        /* Key: exercise number, Value: list of execution times for the selected ex.*/
        Map<Integer, List<Long>> executionTimes = new HashMap<>();

        /*Collect execution times*/
        for (int j = 0; j < EXERCISE_NUMBER; j++) {
            List<Long> exerciseTimes = new ArrayList<>();
            for (int i = 0; i < RUN_NUMBER; i++) {
                long result = 0; //TODO call the correct interactor...
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
            System.out.println("Exercise " + (exKey + 1) + ":"
                    + "\n--------------------------------");
            System.out.println("Average Execution Time: " + averageTime + " ms");
            System.out.println("Max Execution Time: " + maxTime + " ms");
            System.out.println("Min Execution Time: " + minTime + " ms" + "\n");
        });


    }
}
