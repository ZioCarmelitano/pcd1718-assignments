package pcd.ass02.interactors;

import pcd.ass02.domain.Folder;
import pcd.ass02.domain.SearchResultAccumulator;

import static com.google.common.base.Preconditions.checkState;

public abstract class AbstractOccurrencesCounter implements OccurrencesCounter {

    private enum State {
        CREATED,
        STARTED,
        FINISHED,
        STOPPED
    }

    private State state;

    private final SearchResultAccumulator accumulator;

    protected AbstractOccurrencesCounter(SearchResultAccumulator accumulator) {
        this.accumulator = accumulator;
        this.state = State.CREATED;
    }

    @Override
    public final void start() {
        checkNotStopped();

        onStart();

        state = State.STARTED;
    }

    @Override
    public void reset() {
        checkFinished();
        checkNotStopped();

        accumulator.resetStatistics();

        onReset();
    }

    @Override
    public final void stop() {
        checkFinished();
        checkNotStopped();

        onStop();

        state = State.STOPPED;
    }

    @Override
    public final long countOccurrences(Folder rootFolder, String regex) {
        checkStarted();
        final long totalOccurrences = doCount(rootFolder, regex);
        state = State.FINISHED;
        return totalOccurrences;
    }

    protected void onStart() {
    }

    protected void onReset() {
    }

    protected void onStop() {
    }

    protected abstract long doCount(Folder rootFolder, String regex);

    protected final long getTotalOccurrences() {
        return accumulator.getTotalOccurrences();
    }

    private void checkStarted() {
        checkState(state == State.STARTED, getClass().getSimpleName() + " not started");
    }

    private void checkNotStopped() {
        checkState(state != State.STOPPED, getClass().getSimpleName() + " stopped");
    }

    private void checkFinished() {
        checkState(state == State.FINISHED, getClass().getSimpleName() + " not finished");
    }

}
