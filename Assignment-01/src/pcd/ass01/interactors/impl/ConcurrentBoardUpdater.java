package pcd.ass01.interactors.impl;

import pcd.ass01.domain.Board;
import pcd.ass01.interactors.BoardUpdater;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Logger;

public class ConcurrentBoardUpdater implements BoardUpdater {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().getClass().getSimpleName());

    private final CyclicBarrier updateStarted;
    private final CyclicBarrier finishedUpdate;

    private final Worker[] workers;

    public ConcurrentBoardUpdater(int numberOfWorkers) {
        updateStarted = new CyclicBarrier(numberOfWorkers + 1);
        finishedUpdate = new CyclicBarrier(numberOfWorkers + 1);
        workers = new Worker[numberOfWorkers];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker(updateStarted, finishedUpdate);
        }
    }

    @Override
    public Board update(final Board oldBoard) {
        // Create the new board
        final int width = oldBoard.getWidth();
        final int height = oldBoard.getHeight();
        final Board newBoard = Board.board(width, height);

        // Prepare the workers
        final int offset = height / workers.length;
        int fromRow = 0, toRow = offset;
        for (int i = 1; i < workers.length; i++) {
            workers[i].setBoards(fromRow, toRow, oldBoard, newBoard);
            fromRow += offset;
            toRow += offset;
        }
        workers[0].setBoards(fromRow, fromRow + (height - fromRow), oldBoard, newBoard);
        final Thread ct = Thread.currentThread();
        try {
            updateStarted.await();
        } catch (final InterruptedException | BrokenBarrierException e) {
            ct.interrupt();
        }
        logger.config("Update started");

        try {
            finishedUpdate.await();
        } catch (final InterruptedException | BrokenBarrierException e) {
            ct.interrupt();
        }
        logger.config("Finished update");

        return newBoard;
    }

}
