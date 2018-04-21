package fj;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Statistic implements Runnable{
    private BlockingQueue<Optional<SearchResult>> resultQueue;
    private long fileCount;
    private long fileWithOccurrences;
    private long totalOccurrences;
    private volatile AtomicBoolean running;

    private SearchResultUpdateListener listener;
    private List<String> files;
    private double matchingRate;
    private double averageMatches;

    public Statistic(SearchResultUpdateListener listener) {
        this.resultQueue = new LinkedBlockingQueue<>();
        this.files = new ArrayList<>();
        this.running = new AtomicBoolean(true);
        this.listener = listener;
    }

    public void putSearchResult(SearchResult searchResult) {
        if (!running.get()) {
            throw new IllegalStateException();
        }
        this.resultQueue.add(Optional.of(searchResult));
    }

    @Override
    public void run() {
        while (running.get() || !resultQueue.isEmpty()) {
            try {
                System.out.println("Im waiting .....");
                Optional<SearchResult> searchResult = resultQueue.take();
                if (searchResult.isPresent()) {
                    this.fileCount++;
                    if (searchResult.get().getCount() > 0) {
                        this.files.add(searchResult.get().getDocumentName());
                        this.fileWithOccurrences++;
                        this.totalOccurrences += searchResult.get().getCount();
                        this.averageMatches = (double) this.totalOccurrences / (double) this.fileWithOccurrences;
                    }
                    matchingRate = (double) fileWithOccurrences / (double) fileCount;
                    listener.onEvent(this.files, this.matchingRate, this.averageMatches);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Done! ByeBye");
    }

    public void stop() {
        this.running.set(false);
        resultQueue.add(Optional.empty());
    }
}
