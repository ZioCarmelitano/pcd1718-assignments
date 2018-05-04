package pcd.ass02.ex2.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import pcd.ass02.domain.Document;
import pcd.ass02.domain.SearchResult;
import pcd.ass02.util.DocumentHelper;

import static pcd.ass02.ex2.util.MessageHelper.wrap;

class DocumentSearchVerticle extends AbstractVerticle {

    private String regex;
    private EventBus eventBus;

    @Override
    public void start() {
        eventBus = vertx.eventBus();
        eventBus.consumer(C.documentSearch.regex, wrap(this::onRegex));
        eventBus.consumer(C.documentSearch.analyze, wrap(this::onDocument));
    }

    private void onRegex(String regex) {
        this.regex = regex;
    }

    private void onDocument(Document document) {
        final long occurrences = DocumentHelper.countOccurrences(document, regex);
        final SearchResult result = new SearchResult(document.getName(), occurrences);
        eventBus.publish(C.accumulator, result);
        eventBus.publish(C.coordinator.documentAnalyzed, null);
    }

}
