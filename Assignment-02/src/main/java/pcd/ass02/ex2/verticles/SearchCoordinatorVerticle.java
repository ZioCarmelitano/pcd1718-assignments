package pcd.ass02.ex2.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

public class SearchCoordinatorVerticle extends AbstractVerticle {

    private long documentCount;
    private EventBus eventBus;

    @Override
    public void start() throws Exception {
        eventBus = vertx.eventBus();
        eventBus.<Long>consumer("coordinator.documentCount", m -> onDocumentCount(m.body()));
        eventBus.consumer("coordinator.documentProcessed", m -> onDocumentProcessed(m.body()));
    }

    private void onDocumentProcessed(Object ignored) {
        documentCount--;
        if (documentCount == 0) {
            eventBus.publish("coordinator.done", null);
        }
    }

    private void onDocumentCount(long documentCount) {
        this.documentCount = documentCount;
    }

}
