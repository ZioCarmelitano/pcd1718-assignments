package pcd.ass02.ex2.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import pcd.ass02.domain.SearchResult;
import pcd.ass02.domain.SearchStatistics;

import java.util.ArrayList;
import java.util.List;

public class SearchResultAccumulatorVerticle extends AbstractVerticle {

    private static final long CLOSE_DELAY = 2_000;

    private final Handler<SearchStatistics> handler;

    private long fileCount;
    private long fileWithOccurrences;
    private long totalOccurrences;
    private double averageMatches;
    private final List<String> files;

    private long timerID;

    private long startTime;

    private long endTime;

    public SearchResultAccumulatorVerticle(Handler<SearchStatistics> handler) {
        this.handler = handler;
        files = new ArrayList<>();
    }

    @Override
    public void start() {
        startTime = System.currentTimeMillis();

        vertx.eventBus().<SearchResult>consumer("accumulator",
                m -> onSearchResult(m.body()));

        timerID = vertx.setTimer(CLOSE_DELAY, completionHandler());
    }

    private void onSearchResult(SearchResult result) {
        vertx.cancelTimer(timerID);

        final long occurrences = result.getCount();
        final String documentName = result.getDocumentName();

        fileCount++;
        if (occurrences > 0) {
            files.add(documentName);
            fileWithOccurrences++;
            totalOccurrences += occurrences;
            averageMatches = ((double) totalOccurrences) / ((double) fileWithOccurrences);
        }
        final double matchingRate = ((double) fileWithOccurrences) / ((double) fileCount);

        handler.handle(new SearchStatistics(files, matchingRate, averageMatches));

        endTime = System.currentTimeMillis();

        timerID = vertx.setTimer(2000, completionHandler());
    }

    private Handler<Long> completionHandler() {
        return event -> {
            System.out.println("Total occurrences: " + totalOccurrences);
            System.out.println("Execution time: " + (endTime - startTime) + " ms");
            vertx.close();
        };
    }

}
