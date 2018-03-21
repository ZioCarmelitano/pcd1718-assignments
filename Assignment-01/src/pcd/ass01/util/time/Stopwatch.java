package pcd.ass01.util.time;

import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;
import static pcd.ass01.util.time.impl.StopwatchFactoryImpl.defaultInstance;

public interface Stopwatch {

    public static Stopwatch stopwatch(final TimeUnit timeUnit) {
        return requireNonNull(defaultInstance(), "defaultInstance() is null").unstartedStopwatch(timeUnit);
    }

    public static Stopwatch stopwatch(final TimeUnit timeUnit, final boolean started) {
        return requireNonNull(defaultInstance(), "defaultInstance() is null").stopwatch(timeUnit, started);
    }

    public static Stopwatch startedStopwatch(final TimeUnit timeUnit) {
        return requireNonNull(defaultInstance(), "defaultInstance() is null").startedStopwatch(timeUnit);
    }

    public static Stopwatch unstartedStopwatch(final TimeUnit timeUnit) {
        return requireNonNull(defaultInstance(), "defaultInstance() is null").unstartedStopwatch(timeUnit);
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

}
