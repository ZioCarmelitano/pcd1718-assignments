package pcd.ass03.ex1.util.time;

import pcd.ass03.ex1.util.time.impl.StopwatchFactoryImpl;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public interface Stopwatch {

    static Stopwatch stopwatch(final TimeUnit timeUnit) {
        return Objects.requireNonNull(StopwatchFactoryImpl.defaultInstance(), "defaultInstance() is null").unstartedStopwatch(timeUnit);
    }

    static Stopwatch stopwatch(final TimeUnit timeUnit, final boolean started) {
        return Objects.requireNonNull(StopwatchFactoryImpl.defaultInstance(), "defaultInstance() is null").stopwatch(timeUnit, started);
    }

    static Stopwatch startedStopwatch(final TimeUnit timeUnit) {
        return Objects.requireNonNull(StopwatchFactoryImpl.defaultInstance(), "defaultInstance() is null").startedStopwatch(timeUnit);
    }

    static Stopwatch unstartedStopwatch(final TimeUnit timeUnit) {
        return Objects.requireNonNull(StopwatchFactoryImpl.defaultInstance(), "defaultInstance() is null").unstartedStopwatch(timeUnit);
    }

    /**
     * Returns the {@link TimeUnit} used by this stopwatch.
     *
     * @return the {@link TimeUnit} used by this stopwatch.
     */
    TimeUnit getTimeUnit();

    /**
     * Returns the elapsed time from the start stream the stopwatch without stopping it.
     *
     * @return the elapsed time from the start stream the stopwatch.
     * @throws IllegalStateException if the stopwatch was not started or was stopped.
     */
    long partial();

    /**
     * Resets the stopwatch.
     *
     * @return it self.
     * @throws IllegalStateException if the stopwatch was not stopped.
     */
    Stopwatch reset();

    /**
     * Starts the stopwatch after being stopped.
     *
     * @return it self.
     * @throws IllegalStateException if the stopwatch was not stopped.
     */
    Stopwatch restart();

    /**
     * Starts the stopwatch.
     *
     * @return it self.
     * @throws IllegalStateException if the stopwatch was already started.
     */
    Stopwatch start();

    /**
     * Stops the stopwatch and returns the elapsed time from the start stream the stopwatch.
     *
     * @return the elapsed time from the start stream the stopwatch.
     * @throws IllegalStateException if the stopwatch was not started or was already stopped.
     */
    long stop();

    /**
     * Stops and resets the stopwatch, then returns the elapsed time from the start stream the stopwatch.
     *
     * @return the elapsed time from the start stream the stopwatch.
     * @throws IllegalStateException if the stopwatch was not started or was already stopped.
     */
    long stopAndReset();

    boolean isStarted();

    boolean isStopped();

    default boolean isRunning() {
        return isStarted() && !isStopped();
    }

    default long timeIt(Runnable block) {
        Objects.requireNonNull(block, "block is null");
        start();
        block.run();
        return stopAndReset();
    }

}
