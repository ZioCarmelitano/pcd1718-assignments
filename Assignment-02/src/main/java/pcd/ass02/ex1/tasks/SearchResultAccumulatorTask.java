package pcd.ass02.ex1.tasks;

import pcd.ass02.domain.SearchResult;
import pcd.ass02.domain.SearchStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class SearchResultAccumulatorTask implements Runnable {

    private final BlockingQueue<Optional<SearchResult>> resultQueue;

    private boolean running;

    private long fileCount;
    private long fileWithOccurrences;
    private long totalOccurrences;
    private double averageMatches;
    private final List<String> documentNames;

    private final Consumer<? super SearchStatistics> listener;

    public SearchResultAccumulatorTask(Consumer<? super SearchStatistics> listener) {
        resultQueue = new LinkedBlockingQueue<>();
        documentNames = new ArrayList<>();
        running = true;
        this.listener = listener;
    }

    public void notify(SearchResult result) {
        if (!running) {
            throw new IllegalStateException();
        }
        resultQueue.add(Optional.of(result));
    }

    @Override
    public void run() {
        while (running || !resultQueue.isEmpty()) {
            try {
                final Optional<SearchResult> event = resultQueue.take();
                event.ifPresent(this::onSearchResult);
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void onSearchResult(final SearchResult result) {
        fileCount++;
        final long count = result.getCount();
        if (count > 0) {
            final String documentName = result.getDocumentName();
            documentNames.add(documentName);
            fileWithOccurrences++;
            totalOccurrences += count;
            averageMatches = ((double) totalOccurrences) / ((double) fileWithOccurrences);
        }
        final double matchingRate = ((double) fileWithOccurrences) / ((double) fileCount);

        listener.accept(new SearchStatistics(documentNames, matchingRate, averageMatches));
    }

    public void stop() {
        running = false;
        resultQueue.add(Optional.empty());
    }

}
