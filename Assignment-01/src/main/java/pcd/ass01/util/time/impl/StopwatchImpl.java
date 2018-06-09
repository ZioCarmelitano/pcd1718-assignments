package pcd.ass01.util.time.impl;

import pcd.ass01.util.time.SystemClock;

import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

import static java.util.Objects.requireNonNull;

final class StopwatchImpl extends AbstractStopwatch {

    private final TimeUnit tu;

    public StopwatchImpl(final TimeUnit timeUnit) {
        tu = requireNonNull(timeUnit, "Time unit cannot be null");
    }

    @Override
    public TimeUnit getTimeUnit() {
        return tu;
    }

    @Override
    protected LongSupplier currentTime() {
        return () -> SystemClock.currentTime(tu);
    }

}
