package pcd.ass01;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pcd.ass01.domain.Board;
import pcd.ass01.domain.Boards;
import pcd.ass01.interactors.BoardUpdater;
import pcd.ass01.util.time.Stopwatch;

import java.util.concurrent.TimeUnit;

public final class Launcher {

    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);

    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) {
        final BoardUpdater updater = BoardUpdater.create(AVAILABLE_PROCESSORS);

        Board board = Boards.randomBoard(500, 500);
        final Stopwatch stopwatch = Stopwatch.stopwatch(TimeUnit.MILLISECONDS);
        for (int i = 0; i < 10; i++) {
            stopwatch.start();
            board = updater.update(board);
            logger.info("Update finished time: " + stopwatch.stopAndReset() + " ms");
        }
    }

}
