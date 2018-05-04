package pcd.ass02.ex2.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

import static pcd.ass02.ex2.util.MessageHelper.wrap;

class SearchCoordinatorVerticle extends AbstractVerticle {

    private long documentCount;
    private EventBus eventBus;

    @Override
    public void start() {
        eventBus = vertx.eventBus();
        eventBus.consumer(C.coordinator.documentCount, wrap(this::onDocumentCount));
        eventBus.consumer(C.coordinator.documentAnalyzed, wrap(this::onDocumentAnalyzed));
    }

    private void onDocumentAnalyzed() {
        documentCount--;
        if (documentCount == 0) {
            eventBus.publish(C.coordinator.done, null);
        }
    }

    private void onDocumentCount(long documentCount) {
        this.documentCount = documentCount;
    }

}
