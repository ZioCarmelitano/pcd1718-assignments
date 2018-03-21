package pcd.ass01.interactors.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pcd.ass01.domain.Board;
import pcd.ass01.interactors.BoardUpdater;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

final class ConcurrentBoardUpdater implements BoardUpdater {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrentBoardUpdater.class);

    private final Semaphore[] updateStarted;
    private final CyclicBarrier finishedUpdate;

    private final Worker[] workers;

    ConcurrentBoardUpdater(int numberOfWorkers) {
        updateStarted = new Semaphore[numberOfWorkers];
        finishedUpdate = new CyclicBarrier(numberOfWorkers + 1);
        workers = new Worker[numberOfWorkers];
        for (int i = 0; i < workers.length; i++) {
            updateStarted[i] = new Semaphore(0);
            workers[i] = new Worker(updateStarted[i], finishedUpdate);
        }
    }

    @Override
    public Board update(final Board oldBoard) {
        // Create the new board
        final int width = oldBoard.getWidth();
        final int height = oldBoard.getHeight();
        final Board newBoard = Board.board(width, height);

        // Prepare workers
        prepareWorkers(oldBoard, newBoard);
        logger.debug("Update started");

        Thread ct = Thread.currentThread();
        try {
            finishedUpdate.await();
        } catch (final InterruptedException | BrokenBarrierException e) {
            ct.interrupt();
        }
        logger.debug("Finished update");

        return newBoard;
    }

    private void prepareWorkers(Board oldBoard, Board newBoard) {
        final int height = oldBoard.getHeight();
        final int offset = height / workers.length;
        int fromRow = 0, toRow = offset;
        for (int i = 1; i < workers.length; i++) {
            workers[i].setBoards(fromRow, toRow, oldBoard, newBoard);
            updateStarted[i].release();
            fromRow += offset;
            toRow += offset;
        }
        workers[0].setBoards(fromRow, fromRow + (height - fromRow), oldBoard, newBoard);
        updateStarted[0].release();
    }

}
