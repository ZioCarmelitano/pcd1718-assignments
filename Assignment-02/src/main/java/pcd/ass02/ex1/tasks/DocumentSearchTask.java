package pcd.ass02.ex1.tasks;

import pcd.ass02.domain.Document;
import pcd.ass02.domain.SearchResult;
import pcd.ass02.util.DocumentHelper;

import java.util.concurrent.RecursiveTask;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

class DocumentSearchTask extends RecursiveTask<Long> {

    private final Document document;
    private final String regex;
    private final Consumer<? super SearchResult> callback;

    public DocumentSearchTask(Document document, String regex, Consumer<? super SearchResult> callback) {
        super();
        this.document = document;
        this.regex = regex;
        this.callback = callback;
    }

    @Override
    protected Long compute() {
        final long occurrences = DocumentHelper.countOccurrences(document, regex);
        callback.accept(new SearchResult(document.getName(), occurrences));
        return occurrences;
    }

}

