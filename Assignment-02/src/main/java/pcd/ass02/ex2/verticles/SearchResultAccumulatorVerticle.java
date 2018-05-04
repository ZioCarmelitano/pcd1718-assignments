package pcd.ass02.ex2.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import pcd.ass02.domain.SearchResult;
import pcd.ass02.domain.SearchResultAccumulator;
import pcd.ass02.domain.SearchStatistics;

import static pcd.ass02.ex2.util.MessageHelper.wrap;

class SearchResultAccumulatorVerticle extends AbstractVerticle {

    private final SearchResultAccumulator accumulator;
    private final Handler<? super SearchStatistics> resultHandler;

    SearchResultAccumulatorVerticle(SearchResultAccumulator accumulator, Handler<? super SearchStatistics> resultHandler) {
        this.accumulator = accumulator;
        this.resultHandler = resultHandler;
    }

    @Override
    public void start() {
        vertx.eventBus().consumer(C.accumulator, wrap(this::onSearchResult));
    }

    private void onSearchResult(SearchResult result) {
        resultHandler.handle(accumulator.updateStatistics(result));
    }

}
