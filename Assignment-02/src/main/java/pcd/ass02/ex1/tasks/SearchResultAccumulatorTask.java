package pcd.ass02.ex1.tasks;

import pcd.ass02.domain.SearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class SearchResultAccumulatorTask implements Runnable {

    private final BlockingQueue<Optional<SearchResult>> resultQueue;
    private long fileCount;
    private long fileWithOccurrences;
    private long totalOccurrences;
    private boolean running;

    private SearchResultUpdateListener listener;
    private List<String> files;

    private double averageMatches;

    public SearchResultAccumulatorTask(SearchResultUpdateListener listener) {
        resultQueue = new LinkedBlockingQueue<>();
        files = new ArrayList<>();
        running = true;
        this.listener = listener;
    }

    public void notifyEvent(SearchResult searchResult) {
        if (!running) {
            throw new IllegalStateException();
        }
        resultQueue.add(Optional.of(searchResult));
    }

    @Override
    public void run() {
        while (running || !resultQueue.isEmpty()) {
            try {
                System.out.println("Im waiting...");
                Optional<SearchResult> event = resultQueue.take();
                if (event.isPresent()) {
                    final SearchResult searchResult = event.get();
                    fileCount++;
                    final long count = searchResult.getCount();
                    if (count > 0) {
                        final String documentName = searchResult.getDocumentName();
                        files.add(documentName);
                        fileWithOccurrences++;
                        totalOccurrences += count;
                        averageMatches = ((double) totalOccurrences) / ((double) fileWithOccurrences);
                    }
                    final double matchingRate = ((double) fileWithOccurrences) / ((double) fileCount);
                    listener.onEvent(files, matchingRate, averageMatches);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void stop() {
        running = false;
        resultQueue.add(Optional.empty());
    }

}
