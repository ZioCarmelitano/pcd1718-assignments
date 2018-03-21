package pcd.ass01.interactors.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pcd.ass01.domain.Board;
import pcd.ass01.domain.CellUtils;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

final class Worker {

    private static final Logger logger = LoggerFactory.getLogger(Worker.class);

    private final Semaphore updateStarted;
    private final CyclicBarrier finishedUpdate;
    private final AtomicBoolean stopped;

    private final Thread backgroundThread;

    private int fromRow;
    private int toRow;
    private Board oldBoard;
    private Board newBoard;

    Worker(final Semaphore updateStarted, final CyclicBarrier finishedUpdate) {
        this.updateStarted = updateStarted;
        this.finishedUpdate = finishedUpdate;
        stopped = new AtomicBoolean();
        backgroundThread = new Thread(this::updateBoard);
        backgroundThread.start();
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

    private void updateBoard() {
        while (!stopped.get()) {
            // Wait for the start of the update
            logger.debug("Worker on thread " + backgroundThread.getName() + " waiting for start");
            try {
                updateStarted.acquire();
            } catch (final InterruptedException e) {
                backgroundThread.interrupt();
            }

            // Update the given portion of the board
            logger.debug("Worker from " + fromRow + " to " + toRow + " started on thread " + backgroundThread.getName());
            for (int x = fromRow; x < toRow; x++) {
                for (int y = 0; y < oldBoard.getWidth(); y++) {
                    newBoard.setCell(x, y, CellUtils.update(oldBoard, x, y));
                }
            }

            logger.debug("Worker from " + fromRow + " to " + toRow + " finished on thread " + backgroundThread.getName());

            try {
                finishedUpdate.await();
            } catch (final InterruptedException | BrokenBarrierException e) {
                backgroundThread.interrupt();
            }
        }
    }

}
