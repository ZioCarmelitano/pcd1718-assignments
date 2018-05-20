package pcd.ass01.util.time.impl;

import pcd.ass01.util.time.Stopwatch;

import java.util.function.LongSupplier;

import static pcd.ass01.util.Preconditions.checkState;

public abstract class AbstractStopwatch implements Stopwatch {

    private long startTime;
    private long stopTime;
    private long elapsedTime;

    private boolean started;
    private boolean stopped;

    AbstractStopwatch() {
    }

    @Override
    public final long partial() {
        checkRunning();
        return elapsedTime + elapsedTime(getCurrentTime());
    }

    @Override
    public final Stopwatch reset() {
        checkStopped();
        startTime = 0L;
        stopTime = 0L;
        elapsedTime = 0L;
        started = false;
        stopped = false;
        return this;
    }

    @Override
    public final Stopwatch restart() {
        checkStopped();
        stopped = false;
        stopTime = 0L;
        startTime = getCurrentTime();
        return this;
    }

    @Override
    public final Stopwatch start() {
        checkNotStarted();
        checkNotStopped();
        started = true;
        startTime = getCurrentTime();
        return this;
    }

    @Override
    public final long stop() {
        checkNotStopped();
        stopTime = getCurrentTime();
        stopped = true;
        elapsedTime += elapsedTime(stopTime);
        return elapsedTime;
    }

    @Override
    public final long stopAndReset() {
        final long elapsedTime = stop();
        reset();
        return elapsedTime;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    protected abstract LongSupplier currentTime();

    private long getCurrentTime() {
        return currentTime().getAsLong();
    }

    private long elapsedTime(final long currentTime) {
        checkStarted();
        return currentTime - startTime;
    }

    private void checkStarted() {
        checkState(isStarted(), "Stopwatch was not started");
    }

    private void checkNotStarted() {
        checkState(!isStarted(), "Stopwatch was already started");
    }

    private void checkStopped() {
        checkState(isStopped(), "Stopwatch was not stopped");
    }

    private void checkNotStopped() {
        checkState(!isStopped(), "Stopwatch was already stopped");
    }

    private void checkRunning() {
        checkState(isRunning(), "Stopwatch is not running");
    }

}
