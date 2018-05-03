package pcd.ass02.ex2.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import pcd.ass02.domain.Document;
import pcd.ass02.domain.SearchResult;
import pcd.ass02.util.DocumentHelper;

import static pcd.ass02.ex2.util.MessageHelper.handler;
import static pcd.ass02.ex2.verticles.Channels.*;

class DocumentSearchVerticle extends AbstractVerticle {

    private String regex;
    private EventBus eventBus;

    @Override
    public void start() {
        eventBus = vertx.eventBus();
        eventBus.consumer(documentSearch.regex, handler(this::onRegex));
        eventBus.consumer(documentSearch.analyze, handler(this::onDocument));
    }

    private void onRegex(String regex) {
        this.regex = regex;
    }

    private void onDocument(Document document) {
        final long occurrences = DocumentHelper.countOccurrences(document, regex);
        final SearchResult result = new SearchResult(document.getName(), occurrences);
        eventBus.publish(accumulator, result);
        eventBus.publish(coordinator.documentAnalyzed, null);
    }

}
