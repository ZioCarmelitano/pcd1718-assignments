package pcd.ass02.ex1;

import pcd.ass02.domain.Folder;
import pcd.ass02.domain.SearchStatistics;
import pcd.ass02.ex1.tasks.FolderSearchTask;
import pcd.ass02.ex1.tasks.SearchResultAccumulatorTask;
import pcd.ass02.interactors.OccurrencesCounter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

public class ForkJoinOccurrencesCounter implements OccurrencesCounter {

    private final ForkJoinPool pool;
    private final ExecutorService executor;
    private final SearchResultAccumulatorTask accumulator;

    public ForkJoinOccurrencesCounter(Consumer<? super SearchStatistics> resultHandler) {
        pool = new ForkJoinPool();
        executor = Executors.newSingleThreadExecutor();
        accumulator = new SearchResultAccumulatorTask(resultHandler);
    }

    @Override
    public void start() {
        executor.execute(accumulator);
    }

    @Override
    public void stop() {
        accumulator.stop();
        executor.shutdown();
    }

    @Override
    public long countOccurrences(Folder rootFolder, String regex) {
        final FolderSearchTask task = new FolderSearchTask(rootFolder, regex, accumulator::notify);
        return pool.invoke(task);
    }

}
