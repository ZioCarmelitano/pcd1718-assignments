package pcd.ass01.util.time;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;
import static pcd.ass01.util.Preconditions.checkNotNull;

public final class SystemClock {

    public static long currentTime(TimeUnit unit) {
        checkNotNull(unit, "unit");
        final long currentTime = currentTimeNanos();
        return unit == NANOSECONDS
                ? currentTime
                : unit.convert(currentTime, NANOSECONDS);
    }

    public static long currentTimeNanos() {
        return System.nanoTime();
    }

    public static long currentTimeMicros() {
        return currentTime(MICROSECONDS);
    }

    public static long currentTimeMillis() {
        return currentTime(MILLISECONDS);
    }

    public static long currentTimeSeconds() {
        return currentTime(SECONDS);
    }

    public static long currentTimeMinutes() {
        return currentTime(MINUTES);
    }

    public static long currentTimeHours() {
        return currentTime(HOURS);
    }

    public static long currentTimeDays() {
        return currentTime(DAYS);
    }

    public static void sleep(Duration timeout) {
        final long millis = timeout.toMillis();
        final int nanos = (int) (timeout.toNanos() - NANOSECONDS.convert(millis, MILLISECONDS));
        try {
            Thread.sleep(millis, nanos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void sleep(long millis) {
        sleepMillis(millis);
    }

    public static void sleep(long millis, int nanos) {
        sleepNanos(MILLISECONDS.convert(millis, NANOSECONDS) + nanos);
    }

    public static void sleep(long timeout, TemporalUnit unit) {
        sleep(Duration.of(timeout, unit));
    }

    public static void sleepNanos(final long timeout) {
        sleep(Duration.ofNanos(timeout));
    }

    public static void sleepMicros(final long timeout) {
        sleep(timeout, ChronoUnit.MICROS);
    }

    public static void sleepMillis(final long timeout) {
        sleep(Duration.ofMillis(timeout));
    }

    public static void sleepSeconds(final long timeout) {
        sleep(Duration.ofSeconds(timeout));
    }

    public static void sleepMinutes(final long timeout) {
        sleep(Duration.ofMinutes(timeout));
    }

    public static void sleepHours(final long timeout) {
        sleep(Duration.ofHours(timeout));
    }

    public static void sleepDays(final long timeout) {
        sleep(Duration.ofDays(timeout));
    }

    private SystemClock() {
    }

}
