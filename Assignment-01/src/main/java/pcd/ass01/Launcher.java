package pcd.ass01;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pcd.ass01.domain.Board;
import pcd.ass01.domain.Boards;
import pcd.ass01.interactors.BoardUpdater;
import pcd.ass01.util.logging.LogbackUtils;
import pcd.ass01.util.time.Stopwatch;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static pcd.ass01.util.Preconditions.checkState;

public final class Launcher {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
    private static final long CYCLES = 10_000;
    private static final int STEP = 1_000;

    public static void main(String[] args) {
        final Stopwatch stopwatch = Stopwatch.stopwatch(TimeUnit.MILLISECONDS);
        final BoardUpdater sequentialUpdater = BoardUpdater.create();

        for (int numberOfWorkers = 5; numberOfWorkers <= AVAILABLE_PROCESSORS; numberOfWorkers++) {
            final BoardUpdater updater = BoardUpdater.create(numberOfWorkers);
            for (int size = STEP; size <= CYCLES; size += STEP) {
                final Board board = Boards.randomBoard(size, size);

                stopwatch.start();
                final Board newBoard = updater.update(board);
                final long updateTime = stopwatch.stopAndReset();

                logger.info("Board {}x{} updated with {} worker{} in {} ms", size, size, numberOfWorkers, numberOfWorkers > 1 ? "s" : "", updateTime);
                checkState(Objects.equals(newBoard, sequentialUpdater.update(board)), "Updates are not equal");
            }
        }
    }

    private static long timeIt(final Stopwatch stopwatch, final Runnable action) {
        stopwatch.start();
        action.run();
        return stopwatch.stopAndReset();
    }

    static {
        LogbackUtils.setLevel(Level.DEBUG);
    }

}
