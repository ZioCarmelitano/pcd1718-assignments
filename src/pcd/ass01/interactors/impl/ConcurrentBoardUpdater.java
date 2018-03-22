package pcd.ass01.interactors.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pcd.ass01.domain.Board;
import pcd.ass01.interactors.BoardUpdater;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

final class ConcurrentBoardUpdater implements BoardUpdater {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final List<Semaphore> startUpdateList;
    private final List<Semaphore> finishedUpdateList;
    // private final CyclicBarrier finishedUpdateList;

    private final Worker[] workers;

    ConcurrentBoardUpdater(final int numberOfWorkers) {
        startUpdateList = new ArrayList<>(numberOfWorkers);
        finishedUpdateList = new ArrayList<>(numberOfWorkers);
        workers = new Worker[numberOfWorkers];
        for (int i = 0; i < workers.length; i++) {
            startUpdateList.add(new Semaphore(0, true));
            finishedUpdateList.add(new Semaphore(0, true));
            workers[i] = new Worker(startUpdateList.get(i), finishedUpdateList.get(i));
            new Thread(workers[i]).start();
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

        finishedUpdateList.forEach(ConcurrentBoardUpdater::acquire);
        logger.debug("Finished update");

        return newBoard;
    }

    private static void acquire(final Semaphore s) {
        final Thread ct = Thread.currentThread();
        try {
            s.acquire();
        } catch (InterruptedException e) {
            ct.interrupt();
        }
    }

    private void prepareWorkers(final Board oldBoard, final Board newBoard) {
        final int height = oldBoard.getHeight();
        final int offset = height / workers.length;
        int fromRow = 0, toRow = offset;
        for (int i = 1; i < workers.length; i++) {
            workers[i].setBoards(fromRow, toRow, oldBoard, newBoard);
            startUpdateList.get(i).release();
            fromRow += offset;
            toRow += offset;
        }
        workers[0].setBoards(fromRow, fromRow + (height - fromRow), oldBoard, newBoard);
        startUpdateList.get(0).release();
    }

}
