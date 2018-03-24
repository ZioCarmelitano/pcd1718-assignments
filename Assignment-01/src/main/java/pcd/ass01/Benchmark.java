package pcd.ass01;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pcd.ass01.domain.Board;
import pcd.ass01.domain.Boards;
import pcd.ass01.interactors.BoardUpdater;
import pcd.ass01.util.LoggingUtils;
import pcd.ass01.util.time.Stopwatch;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

final class Benchmark {

    private static final Logger logger;

    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    private static final int STEP = 1_000;
    private static final int MAX_SIZE = 10_000;

    public static void main(final String... args) {
        final Stopwatch stopwatch = Stopwatch.stopwatch(TimeUnit.MILLISECONDS);
        final BoardUpdater sequentialUpdater = BoardUpdater.create();

        sequentialUpdater.start();
        for (int numberOfWorkers = 1; numberOfWorkers <= AVAILABLE_PROCESSORS + 1; numberOfWorkers++) {
            final BoardUpdater updater = BoardUpdater.create(numberOfWorkers);
            updater.start();
            for (int size = STEP; size <= MAX_SIZE; size += STEP) {
                final Board board = Boards.gosperGliderGun(size, size);

                final long updateTime = timeIt(stopwatch, () -> updater.update(board));

                logger.info("{} {}x{} updated with {} worker{} in {} ms", board.getClass().getSimpleName(), size, size, numberOfWorkers, numberOfWorkers > 1 ? "s" : "", updateTime);
                // checkState(Objects.equals(newBoard, sequentialUpdater.update(board)), "Updates are not equal");
            }
            updater.stop();
        }
        sequentialUpdater.stop();
    }

    private static long timeIt(final Stopwatch stopwatch, final Runnable action) {
        stopwatch.start();
        action.run();
        return stopwatch.stopAndReset();
    }

    static {
        LoggingUtils.setLevel(Level.INFO);
        logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    }

    private Benchmark() {
    }

}
