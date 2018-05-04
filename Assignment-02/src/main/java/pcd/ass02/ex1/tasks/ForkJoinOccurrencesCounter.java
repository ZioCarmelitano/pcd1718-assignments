package pcd.ass02.ex1.tasks;

import pcd.ass02.domain.Folder;
import pcd.ass02.domain.SearchResultAccumulator;
import pcd.ass02.domain.SearchStatistics;
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
        this(new SearchResultAccumulator(), new ForkJoinPool(), resultHandler);
    }

    public ForkJoinOccurrencesCounter(int parallelism, Consumer<? super SearchStatistics> resultHandler) {
        this(new SearchResultAccumulator(), new ForkJoinPool(parallelism), resultHandler);
    }

    private ForkJoinOccurrencesCounter(SearchResultAccumulator accumulator, ForkJoinPool pool, Consumer<? super SearchStatistics> resultHandler) {
        super(accumulator);
        this.pool = pool;
        this.executor = Executors.newSingleThreadExecutor();
        this.accumulator = new SearchResultAccumulatorTask(accumulator, resultHandler);
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
