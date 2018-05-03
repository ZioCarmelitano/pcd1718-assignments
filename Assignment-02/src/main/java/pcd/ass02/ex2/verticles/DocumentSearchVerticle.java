package pcd.ass02.ex2.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import pcd.ass02.domain.Document;
import pcd.ass02.domain.SearchResult;
import pcd.ass02.util.DocumentHelper;

public class DocumentSearchVerticle extends AbstractVerticle {

    private String regex;
    private EventBus eventBus;

    @Override
    public void start() {
        eventBus = vertx.eventBus();
        eventBus.<String>consumer("documentSearch.regex", m -> onRegex(m.body()));
        eventBus.<Document>consumer("documentSearch", m -> onDocument(m.body()));
    }

    private void onRegex(String regex) {
        this.regex = regex;
    }

    private void onDocument(Document document) {
        final long occurrences = DocumentHelper.countOccurrences(document, regex);
        final SearchResult result = new SearchResult(document.getName(), occurrences);
        eventBus.publish("accumulator", result);
        eventBus.publish("coordinator.documentProcessed", null);
    }

}
