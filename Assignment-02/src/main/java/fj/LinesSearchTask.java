package fj;

import java.util.List;
import java.util.concurrent.RecursiveTask;

public class LinesSearchTask extends RecursiveTask<Long> {
    private List<String> lines;
    private final String regex;
    private final OccurrencesCounter wc;

    public LinesSearchTask(List<String> lines, String regex, OccurrencesCounter wc) {
        this.lines = lines;
        this.regex = regex;
        this.wc = wc;
    }

    @Override
    protected Long compute() {
        return null;
    }
}
