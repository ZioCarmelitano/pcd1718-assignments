package pcd.ass02.ex1.tasks;

import pcd.ass02.ex1.OccurrencesCounter;

import java.util.List;
import java.util.concurrent.RecursiveTask;

class LinesSearchTask extends RecursiveTask<Long> {

    private final List<String> lines;
    private final String regex;
    private final OccurrencesCounter oc;

    public LinesSearchTask(List<String> lines, String regex, OccurrencesCounter oc) {
        this.lines = lines;
        this.regex = regex;
        this.oc = oc;
    }

    @Override
    protected Long compute() {
        return null;
    }

}
