package pcd.ass02.ex1.tasks;

import pcd.ass02.domain.Document;
import pcd.ass02.ex1.OccurrencesCounter;

import java.util.concurrent.RecursiveTask;
import java.util.function.BiConsumer;

public class DocumentSearchTask extends RecursiveTask<Long> {

    private final Document document;
    private final String regex;
    private final OccurrencesCounter oc;
    private final BiConsumer<Document, Long> callback;

    public DocumentSearchTask(OccurrencesCounter oc, Document document, String regex, BiConsumer<Document, Long> callback) {
        super();
        this.document = document;
        this.regex = regex;
        this.oc = oc;
        this.callback = callback;
    }

    @Override
    protected Long compute() {
        final long result = oc.occurrencesCount(document, regex);
        callback.accept(document, result);
        return result;
    }

}

