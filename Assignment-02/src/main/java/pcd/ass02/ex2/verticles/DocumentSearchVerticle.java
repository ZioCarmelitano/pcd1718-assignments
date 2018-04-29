package pcd.ass02.ex2.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import pcd.ass02.domain.Document;
import pcd.ass02.domain.SearchResult;
import pcd.ass02.util.DocumentHelper;

public class DocumentSearchVerticle extends AbstractVerticle {

    private final String regex;
    private EventBus eventBus;

    public DocumentSearchVerticle(String regex) {
        this.regex = regex;
    }

    @Override
    public void start() {
        eventBus = vertx.eventBus();
        eventBus.<Document>consumer("documentSearch", m -> onDocument(m.body()));
    }

    private void onDocument(Document document) {
        final long occurrences = DocumentHelper.countOccurrences(document, regex);
        final SearchResult result = new SearchResult(document.getName(), occurrences);
        eventBus.publish("accumulator", result);
    }

}
