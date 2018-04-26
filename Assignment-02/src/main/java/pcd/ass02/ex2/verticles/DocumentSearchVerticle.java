package pcd.ass02.ex2.verticles;

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
    public void start() {
        vertx.<Long>executeBlocking(future -> {
            long occurrences = OccurrencesCounter.countOccurrences(document, regex);
            future.complete(occurrences);
        }, ar -> {
            if (ar.succeeded()) {
                final long occurrences = ar.result();
                vertx.eventBus().publish("accumulator",
                        new JsonObject()
                                .put("occurrences", occurrences)
                                .put("documentName", document.getName()));
            } else {
                System.err.println("Oops, something went wrong: " + ar.cause().getMessage());
            }
        });
    }
}
