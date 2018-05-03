package pcd.ass02.ex2.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import pcd.ass02.domain.SearchResult;
import pcd.ass02.domain.SearchResultAccumulator;
import pcd.ass02.domain.SearchStatistics;

import static pcd.ass02.ex2.util.MessageHelper.handler;
import static pcd.ass02.ex2.verticles.Channels.accumulator;

class SearchResultAccumulatorVerticle extends AbstractVerticle {

    private final SearchResultAccumulator delegate;
    private final Handler<? super SearchStatistics> resultHandler;

    SearchResultAccumulatorVerticle(SearchResultAccumulator accumulator, Handler<? super SearchStatistics> resultHandler) {
        delegate = accumulator;
        this.resultHandler = resultHandler;
    }

    @Override
    public void start() {
        vertx.eventBus().consumer(accumulator, handler(this::onSearchResult));
    }

    private void onSearchResult(SearchResult result) {
        resultHandler.handle(delegate.updateStatistics(result));
    }

}
