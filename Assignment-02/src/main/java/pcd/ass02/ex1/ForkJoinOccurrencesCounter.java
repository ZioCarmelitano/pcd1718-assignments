package pcd.ass02.ex1;

import pcd.ass02.domain.Folder;
import pcd.ass02.domain.SearchResultAccumulator;
import pcd.ass02.domain.SearchStatistics;
import pcd.ass02.ex1.tasks.FolderSearchTask;
import pcd.ass02.ex1.tasks.SearchResultAccumulatorTask;
import pcd.ass02.interactors.AbstractOccurrencesCounter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

public class ForkJoinOccurrencesCounter extends AbstractOccurrencesCounter {

    private final ForkJoinPool pool;
    private final ExecutorService executor;

    private final SearchResultAccumulatorTask accumulator;

    public ForkJoinOccurrencesCounter(Consumer<? super SearchStatistics> resultHandler) {
        pool = new ForkJoinPool();
        this.executor = Executors.newSingleThreadExecutor();
        final SearchResultAccumulator accumulator = new SearchResultAccumulator();
        this.accumulator = new SearchResultAccumulatorTask(accumulator, resultHandler);
        setAccumulator(accumulator);
    }

    @Override
    protected void onStart() {
        executor.execute(accumulator);
    }

    @Override
    protected void onStop() {
        accumulator.stop();
        executor.shutdown();
    }

    @Override
    protected long doCount(Folder rootFolder, String regex) {
        final FolderSearchTask task = new FolderSearchTask(rootFolder, regex, accumulator::notify);
        return pool.invoke(task);
    }

}
