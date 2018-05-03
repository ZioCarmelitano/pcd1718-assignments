package pcd.ass02.ex2.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

import static pcd.ass02.ex2.util.MessageHelper.handler;
import static pcd.ass02.ex2.verticles.Channels.*;

class SearchCoordinatorVerticle extends AbstractVerticle {

    private long documentCount;
    private EventBus eventBus;

    @Override
    public void start() {
        eventBus = vertx.eventBus();
        eventBus.consumer(coordinator.documentCount, handler(this::onDocumentCount));
        eventBus.consumer(coordinator.documentAnalyzed, handler(this::onDocumentAnalyzed));
    }

    private void onDocumentAnalyzed(Object ignored) {
        documentCount--;
        if (documentCount == 0) {
            eventBus.publish(coordinator.done, null);
        }
    }

    private void onDocumentCount(long documentCount) {
        this.documentCount = documentCount;
    }

}
