package pcd.ass02.ex1.tasks;

import pcd.ass02.domain.SearchResult;
import pcd.ass02.domain.SearchResultAccumulator;
import pcd.ass02.domain.SearchStatistics;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

class SearchResultAccumulatorTask implements Runnable {

    private final BlockingQueue<Optional<SearchResult>> resultQueue;
    private boolean running;

    private final SearchResultAccumulator accumulator;

    private final Consumer<? super SearchStatistics> listener;

    SearchResultAccumulatorTask(SearchResultAccumulator accumulator, Consumer<? super SearchStatistics> listener) {
        resultQueue = new LinkedBlockingQueue<>();
        this.accumulator = accumulator;
        this.listener = listener;
    }

    void notify(SearchResult result) {
        if (!running) {
            throw new IllegalStateException();
        }
        resultQueue.add(Optional.of(result));
    }

    @Override
    public void run() {
        running = true;
        try {
            while (running || !resultQueue.isEmpty()) {
                resultQueue.take()
                        .ifPresent(this::onSearchResult);
            }
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void onSearchResult(final SearchResult result) {
        listener.accept(accumulator.updateStatistics(result));
    }

    public void stop() {
        running = false;
        resultQueue.add(Optional.empty());
    }

}
