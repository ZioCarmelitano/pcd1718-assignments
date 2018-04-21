package fj;

import java.util.concurrent.RecursiveTask;
import java.util.function.BiConsumer;

public class DocumentSearchTask extends RecursiveTask<Long> {
    
	private final Document document;
    private final String regex;
    private final OccurrencesCounter wc;
    private final BiConsumer<Document,Long> callback;
    
    public DocumentSearchTask(OccurrencesCounter wc, Document document, String regex, BiConsumer<Document, Long> callback) {
        super();
        this.document = document;
        this.regex = regex;
        this.wc = wc;
        this.callback = callback;
    }
    
    @Override
    protected Long compute() {
        Long result = wc.occurrencesCount(document, regex);
        callback.accept(document,result);
        return result;
    }
}

