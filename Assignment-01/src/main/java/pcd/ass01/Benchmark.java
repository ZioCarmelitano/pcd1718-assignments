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

import static pcd.ass01.domain.Board.Order.ROW_MAJOR;

class Benchmark {

    private static final Logger logger;

    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
    private static final long MAX_SIZE = 9_000;
    private static final int STEP = 1_000;


    public static void main(String[] args) {
        final Stopwatch stopwatch = Stopwatch.stopwatch(TimeUnit.MILLISECONDS);
        final BoardUpdater sequentialUpdater = BoardUpdater.create();

        sequentialUpdater.start();
        for (int numberOfWorkers = 1; numberOfWorkers <= AVAILABLE_PROCESSORS; numberOfWorkers++) {
            final BoardUpdater updater = BoardUpdater.create(numberOfWorkers);
            updater.start();
            for (int size = STEP; size <= MAX_SIZE; size += STEP) {
                final Board board = Boards.randomBoard(size, size, ROW_MAJOR);

                final long updateTime = timeIt(stopwatch, () -> updater.update(board));

                logger.info("Board {}x{} updated with {} worker{} in {} ms", size, size, numberOfWorkers, numberOfWorkers > 1 ? "s" : "", updateTime);
                // checkState(Objects.equals(newBoard, sequentialUpdater.update(board)), "Updates are not equal");
            }
            sequentialUpdater.stop();
        }
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

}
