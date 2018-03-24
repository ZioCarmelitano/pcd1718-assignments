package pcd.ass01.interactors.impl;

import pcd.ass01.interactors.BoardUpdater;

import java.util.concurrent.atomic.AtomicBoolean;

import static pcd.ass01.util.Preconditions.checkState;

public abstract class AbstractBoardUpdater implements BoardUpdater {

    private final AtomicBoolean started;
    private final AtomicBoolean stopped;

    AbstractBoardUpdater() {
        started = new AtomicBoolean();
        stopped = new AtomicBoolean();
    }

    @Override
    public void start() {
        checkNotStarted();
        checkNotStopped();
        started.set(true);
    }

    @Override
    public void stop() {
        checkStarted();
        // comment if launch Benchmark
        checkNotStopped();
        stopped.set(true);
    }

    final void checkStarted() {
        checkState(isStarted(), "Board updater wasn't started");
    }

    private void checkNotStarted() {
        checkState(!isStarted(), "Board updater is already started");
    }

    final void checkNotStopped() {
        checkState(isNotStopped(), "Board updater is already stopped");
    }

    private boolean isStarted() {
        return started.get();
    }

    final boolean isNotStopped() {
        return !stopped.get();
    }

}
