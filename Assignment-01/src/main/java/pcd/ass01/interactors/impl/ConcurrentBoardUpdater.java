package pcd.ass01.interactors.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pcd.ass01.domain.Board;
import pcd.ass01.interactors.BoardUpdater;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.IntStream;

import static pcd.ass01.util.Preconditions.checkNotNull;

final class ConcurrentBoardUpdater extends AbstractBoardUpdater implements BoardUpdater {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final List<Semaphore> startUpdateList;
    private final List<Semaphore> finishedUpdateList;

    private final List<Worker> workers;

    ConcurrentBoardUpdater(final int numberOfWorkers) {
        startUpdateList = new ArrayList<>(numberOfWorkers);
        finishedUpdateList = new ArrayList<>(numberOfWorkers);
        workers = new ArrayList<>(numberOfWorkers);
        for (int i = 0; i < numberOfWorkers; i++) {
            startUpdateList.add(new Semaphore(0, true));
            finishedUpdateList.add(new Semaphore(0, true));
            workers.add(new Worker(startUpdateList.get(i), finishedUpdateList.get(i)));
        }
    }

    @Override
    public void start() {
        super.start();
        workers.parallelStream()
                .map(Thread::new)
                .forEach(Thread::start);
    }

    @Override
    public void stop() {
        super.stop();

        for (int i = 0; i < workers.size(); i++) {
            workers.get(i).stop();
            final Semaphore startUpdate = startUpdateList.get(i);
            if (startUpdate.hasQueuedThreads()) {
                logger.debug("Releasing semaphore");
                startUpdate.release();
            }
        }
    }

    @Override
    public Board update(final Board oldBoard) {
        checkNotNull(oldBoard, "board");

        checkStarted();
        checkNotStopped();

        // Create the new board
        final int height = oldBoard.getHeight();
        final int width = oldBoard.getWidth();
        final Board newBoard = Board.board(height, width);

        // Prepare workers
        prepareWorkers(oldBoard, newBoard);
        logger.debug("Update started");

        if (!isStopped())
            finishedUpdateList.forEach(Semaphore::acquireUninterruptibly);
        logger.debug("Finished update");

        return newBoard;
    }

    private void prepareWorkers(final Board oldBoard, final Board newBoard) {
        final int height = oldBoard.getHeight();
        final int offset = height / workers.size();
        int fromRow = 0, toRow = offset;
        for (int i = 1; i < workers.size(); i++) {
            workers.get(i).setBoards(fromRow, toRow, oldBoard, newBoard);
            startUpdateList.get(i).release();
            fromRow += offset;
            toRow += offset;
        }
        workers.get(0).setBoards(fromRow, fromRow + (height - fromRow), oldBoard, newBoard);
        startUpdateList.get(0).release();
    }

}
