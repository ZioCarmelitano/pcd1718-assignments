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
    private final List<String> files;

    private final Consumer<SearchStatistics> listener;

    public SearchResultAccumulatorTask(Consumer<SearchStatistics> listener) {
        resultQueue = new LinkedBlockingQueue<>();
        files = new ArrayList<>();
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
                System.out.println("Im waiting...");
                Optional<SearchResult> event = resultQueue.take();
                if (event.isPresent()) {
                    final SearchResult result = event.get();
                    fileCount++;
                    final long count = result.getCount();
                    if (count > 0) {
                        final String documentName = result.getDocumentName();
                        files.add(documentName);
                        fileWithOccurrences++;
                        totalOccurrences += count;
                        averageMatches = ((double) totalOccurrences) / ((double) fileWithOccurrences);
                    }
                    final double matchingRate = ((double) fileWithOccurrences) / ((double) fileCount);

                    listener.accept(new SearchStatistics(files, matchingRate, averageMatches));
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
