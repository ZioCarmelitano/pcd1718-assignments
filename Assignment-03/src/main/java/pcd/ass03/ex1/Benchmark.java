package pcd.ass03.ex1;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import ch.qos.logback.classic.Level;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pcd.ass03.ex1.actors.BenchmarkActor;
import pcd.ass03.ex1.actors.msg.Start;
import pcd.ass03.ex1.domain.Board;
import pcd.ass03.ex1.domain.Boards;
import pcd.ass03.ex1.util.LoggingUtils;
import pcd.ass03.ex1.util.time.Stopwatch;
import pcd.ass03.ex1.util.time.TimeUtils;

import java.lang.invoke.MethodHandles;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static java.util.Map.Entry;

final class Benchmark {

    private static final Logger logger;

    private static final int MAX_ITERATIONS = 25;
    private static final int MAX_NUMBER_OF_WORKERS = 10;

    private static final int SIZE = 5_000;

    private static final ActorSystem system = ActorSystem.create("MySystem");

    public static void main(final String... args) {
        final Stopwatch stopwatch = Stopwatch.stopwatch(TimeUnit.MILLISECONDS);
        final Board board = Boards.gosperGliderGun(SIZE, SIZE);

        final Multimap<Integer, Long> results = Multimaps.newListMultimap(new HashMap<>(), ArrayList::new);

        for (int i = 1; i <= MAX_ITERATIONS; i++) {
            System.out.println("Iteration #" + i);
            for (int numberOfWorkers = 1; numberOfWorkers <= MAX_NUMBER_OF_WORKERS; numberOfWorkers++) {
                final ActorRef benchmark = system.actorOf(BenchmarkActor.props(numberOfWorkers), i + "BenchmarkActor" + numberOfWorkers);

                long startTime = System.currentTimeMillis();
                benchmark.tell(new Start(board), ActorRef.noSender());
                while (!benchmark.isTerminated()) ;
                long updateTime = System.currentTimeMillis() - startTime;

                results.put(numberOfWorkers, updateTime);
            }
        }

        system.terminate();

        final Map<Integer, Long> minSpeeds = results.asMap().entrySet().stream()
                .map(e -> new SimpleImmutableEntry<>(
                        e.getKey(),
                        getSpeeds(e)
                                .min()
                                .getAsLong()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        final Map<Integer, Long> maxSpeeds = results.asMap().entrySet().stream()
                .map(e -> new SimpleImmutableEntry<>(
                        e.getKey(),
                        getSpeeds(e)
                                .max()
                                .getAsLong()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        final Map<Integer, Double> avgSpeeds = results.asMap().entrySet().stream()
                .map(e -> new SimpleImmutableEntry<>(
                        e.getKey(),
                        getSpeeds(e)
                                .average()
                                .getAsDouble()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        System.out.println("Iterations: " + MAX_ITERATIONS);
        System.out.println("Size: " + SIZE + "x" + SIZE);
        System.out.println();

        final String si = " " + TimeUtils.toSIString(stopwatch.getTimeUnit());
        System.out.println("Min speeds: " + minSpeeds + si);
        System.out.println("Max speeds: " + maxSpeeds + si);
        System.out.println("Average speeds: " + avgSpeeds + si);
        System.out.println();

        final double singleThreadedMinSpeed = minSpeeds.get(1);
        minSpeeds.remove(1);

        final double singleThreadedMaxSpeed = maxSpeeds.get(1);
        maxSpeeds.remove(1);

        final double singleThreadedAvgSpeed = avgSpeeds.get(1);
        avgSpeeds.remove(1);

        final Map<Integer, Double> minSpeedUps = calculateSpeedUps(minSpeeds, singleThreadedMinSpeed);
        final Map<Integer, Double> maxSpeedUps = calculateSpeedUps(maxSpeeds, singleThreadedMaxSpeed);
        final Map<Integer, Double> avgSpeedUps = calculateSpeedUps(avgSpeeds, singleThreadedAvgSpeed);

        System.out.println("Min speed-ups: " + minSpeedUps);
        System.out.println("Max speed-ups: " + maxSpeedUps);
        System.out.println("Avg speed-ups: " + avgSpeedUps);
    }

    private static Map<Integer, Double> calculateSpeedUps(final Map<? extends Integer, ? extends Number> speeds, final double singleThreadedSpeed) {
        return speeds.entrySet().stream()
                .map(e -> new SimpleImmutableEntry<>(e.getKey(), singleThreadedSpeed / e.getValue().doubleValue()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private static LongStream getSpeeds(final Entry<? extends Integer, ? extends Collection<? extends Long>> e) {
        return e.getValue().stream()
                .mapToLong(Number::longValue);
    }

    static {
        LoggingUtils.setLevel(Level.ERROR);
        logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    }

    private Benchmark() {
    }

}
