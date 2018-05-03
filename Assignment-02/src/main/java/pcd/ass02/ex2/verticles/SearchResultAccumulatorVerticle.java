package pcd.ass02.ex2.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import pcd.ass02.domain.SearchResult;
import pcd.ass02.domain.SearchResultAccumulator;
import pcd.ass02.domain.SearchStatistics;

public class SearchResultAccumulatorVerticle extends AbstractVerticle {

    private static final long COMPLETION_DELAY = 2_000;

    private final Handler<? super SearchStatistics> handler;

    private final SearchResultAccumulator accumulator;

    public SearchResultAccumulatorVerticle(SearchResultAccumulator accumulator, Handler<? super SearchStatistics> resultHandler) {
        this.handler = resultHandler;
        this.accumulator = accumulator;
    }

    @Override
    public void start() {
        vertx.eventBus().<SearchResult>consumer("accumulator", m -> onSearchResult(m.body()));
    }

    private void onSearchResult(SearchResult result) {
        handler.handle(accumulator.updateStatistics(result));
    }

}
