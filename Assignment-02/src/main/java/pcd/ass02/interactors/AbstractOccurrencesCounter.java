package pcd.ass02.interactors;

import pcd.ass02.domain.Folder;
import pcd.ass02.domain.SearchResultAccumulator;

import static com.google.common.base.Preconditions.checkState;

public abstract class AbstractOccurrencesCounter implements OccurrencesCounter {

    private enum State {
        NOT_INITIALIZED,
        INITIALIZED,
        STARTED,
        FINISHED,
        STOPPED
    }

    private State state;

    private SearchResultAccumulator accumulator;

    protected AbstractOccurrencesCounter() {
        state = State.NOT_INITIALIZED;
    }

    @Override
    public final void start() {
        checkNotStopped();
        checkInitialized();

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

    protected final void setAccumulator(SearchResultAccumulator accumulator) {
        checkNotInitialized();

        if (accumulator != null) {
            this.accumulator = accumulator;
            state = State.INITIALIZED;
        }
    }

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

    private void checkInitialized() {
        checkState(state == State.INITIALIZED, getClass().getSimpleName() + " not initialized");
    }

    private void checkNotInitialized() {
        checkState(state == State.NOT_INITIALIZED, getClass().getSimpleName() + " already initialized");
    }

}
