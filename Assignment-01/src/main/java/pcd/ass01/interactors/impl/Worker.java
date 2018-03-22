package pcd.ass01.interactors.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pcd.ass01.domain.Board;
import pcd.ass01.domain.CellUtils;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

final class Worker implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Semaphore startUpdate;
    private final Semaphore finishedUpdate;

    private final AtomicBoolean stopped;

    private int fromRow;
    private int toRow;
    private Board oldBoard;
    private Board newBoard;

    Worker(final Semaphore startUpdate, final Semaphore finishedUpdate) {
        this.startUpdate = startUpdate;
        this.finishedUpdate = finishedUpdate;
        stopped = new AtomicBoolean();
    }

    void setBoards(final int fromRow, final int toRow, final Board oldBoard, final Board newBoard) {
        this.fromRow = fromRow;
        this.toRow = toRow;
        this.oldBoard = oldBoard;
        this.newBoard = newBoard;
    }

    void stop() {
        stopped.set(true);
    }

    @Override
    public void run() {
        final Thread ct = Thread.currentThread();
        while (!stopped.get()) {
            // Wait for the start of the update
            logger.debug("Worker waiting to start");
            try {
                startUpdate.acquire();
            } catch (final InterruptedException e) {
                ct.interrupt();
            }

            // Update the given portion of the board
            logger.debug("Worker from row {} to row {} started", fromRow, toRow);
            for (int x = fromRow; x < toRow; x++) {
                for (int y = 0; y < oldBoard.getWidth(); y++) {
                    newBoard.setCell(x, y, CellUtils.update(oldBoard, x, y));
                }
            }

            logger.debug("Worker from row {} to row {} finished", fromRow, toRow);

            finishedUpdate.release();
        }
    }

}
