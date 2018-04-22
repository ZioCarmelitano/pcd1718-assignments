package pcd.ass02.ex1.tasks;

import pcd.ass02.domain.Document;
import pcd.ass02.ex1.OccurrencesCounter;

import java.util.concurrent.RecursiveTask;
import java.util.function.BiConsumer;

class DocumentSearchTask extends RecursiveTask<Long> {

    private final Document document;
    private final String regex;
    private final BiConsumer<Document, Long> callback;

    public DocumentSearchTask(Document document, String regex, BiConsumer<Document, Long> callback) {
        super();
        this.document = document;
        this.regex = regex;
        this.callback = callback;
    }

    @Override
    protected Long compute() {
        final long result = OccurrencesCounter.occurrencesCount(document, regex);
        callback.accept(document, result);
        return result;
    }

}

