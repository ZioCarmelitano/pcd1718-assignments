package pcd.ass02.ex2;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import pcd.ass02.domain.Document;
import pcd.ass02.ex1.OccurrencesCounter;

class DocumentSearchVerticle extends AbstractVerticle {

    private final Document document;
    private final String regex;

    public DocumentSearchVerticle(Document document, String regex) {
        this.document = document;
        this.regex = regex;
    }

    @Override
    public void start() throws Exception {
        super.start();
        long occurrences = OccurrencesCounter.occurrencesCount(document, regex);
        vertx.eventBus().publish("accumulator",
                new JsonObject()
                        .put("occurrences", occurrences)
                        .put("documentName", document.getName())
        );
    }
}
