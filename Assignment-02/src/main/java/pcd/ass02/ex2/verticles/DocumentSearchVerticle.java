package pcd.ass02.ex2.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import pcd.ass02.domain.Document;
import pcd.ass02.domain.SearchResult;
import pcd.ass02.ex1.OccurrencesCounter;

public class DocumentSearchVerticle extends AbstractVerticle {

    private final String regex;

    public DocumentSearchVerticle(String regex) {
        this.regex = regex;
    }

    @Override
    public void start() {
        vertx.eventBus().<Document>consumer("documentSearch", m -> onDocument(m.body()));
    }

    private void onDocument(Document document) {
        final long occurrences = OccurrencesCounter.countOccurrences(document, regex);
        final SearchResult result = new SearchResult(document.getName(), occurrences);
        vertx.eventBus().publish("accumulator", result);
    }

}
