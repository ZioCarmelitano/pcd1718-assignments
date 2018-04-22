package pcd.ass02.ex1.tasks;

import pcd.ass02.domain.SearchResult;
import pcd.ass02.domain.SearchResultStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class SearchResultAccumulatorTask implements Runnable {

    private final BlockingQueue<Optional<SearchResult>> resultQueue;

    private boolean running;
    private final Consumer<SearchResultStatistics> listener;

    private long fileCount;
    private long fileWithOccurrences;
    private long totalOccurrences;
    private double averageMatches;
    private final List<String> files;

    public SearchResultAccumulatorTask(Consumer<SearchResultStatistics> listener) {
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
                    listener.accept(new SearchResultStatistics(files, matchingRate, averageMatches));
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
