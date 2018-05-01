package pcd.ass02.ex2.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import pcd.ass02.domain.SearchResult;
import pcd.ass02.domain.SearchResultAccumulator;
import pcd.ass02.domain.SearchStatistics;

import java.util.Arrays;
import java.util.List;

public class SearchResultAccumulatorVerticle extends AbstractVerticle {

    private static final long COMPLETION_DELAY = 2_000;

    private final Handler<? super SearchStatistics> handler;
    private final Handler<? super List<Long>> completionHandler;

    private final SearchResultAccumulator accumulator;

    private long timerID;

    private long startTime;
    private long endTime;

    public SearchResultAccumulatorVerticle(Handler<? super SearchStatistics> resultHandler, Handler<? super List<Long>> completionHandler) {
        this.handler = resultHandler;
        this.completionHandler = completionHandler;
        accumulator = new SearchResultAccumulator();
    }

    @Override
    public void start() {
        vertx.eventBus().<SearchResult>consumer("accumulator", m -> onSearchResult(m.body()));

        timerID = vertx.setTimer(COMPLETION_DELAY, this::handleCompletion);

        startTime = System.currentTimeMillis();
    }

    private void onSearchResult(SearchResult result) {
        handler.handle(accumulator.updateStatistics(result));

        endTime = System.currentTimeMillis();

        vertx.cancelTimer(timerID);
        timerID = vertx.setTimer(COMPLETION_DELAY, this::handleCompletion);
    }

    private void handleCompletion(long tid) {
        long executionTime = endTime - startTime;
        completionHandler.handle(Arrays.asList(accumulator.getTotalOccurrences(), executionTime));
        //System.out.println("Execution time: " + executionTime + " ms");
    }

}
