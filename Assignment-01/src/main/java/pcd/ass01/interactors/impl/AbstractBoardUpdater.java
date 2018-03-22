package pcd.ass01.interactors.impl;

import pcd.ass01.interactors.BoardUpdater;

import java.util.concurrent.atomic.AtomicBoolean;

import static pcd.ass01.util.Preconditions.checkState;

public abstract class AbstractBoardUpdater implements BoardUpdater {

    private final AtomicBoolean started;
    private final AtomicBoolean stopped;

    protected AbstractBoardUpdater() {
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
        checkNotStopped();
        stopped.set(true);
    }

    protected final void checkStarted() {
        checkState(isStarted(), "Board updater wasn't started");
    }

    protected final void checkNotStarted() {
        checkState(!isStarted(), "Board updater is already started");
    }

    protected final void checkNotStopped() {
        checkState(!isStopped(), "Board updater is already stopped");
    }

    protected final boolean isStarted() {
        return started.get();
    }

    protected final boolean isStopped() {
        return stopped.get();
    }

}
