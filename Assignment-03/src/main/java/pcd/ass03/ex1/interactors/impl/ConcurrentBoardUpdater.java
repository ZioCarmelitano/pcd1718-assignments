package pcd.ass03.ex1.interactors.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pcd.ass03.ex1.domain.Board;
import pcd.ass03.ex1.util.Preconditions;
import pcd.ass03.ex1.util.Semaphores;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.stream.IntStream;

final class ConcurrentBoardUpdater extends AbstractBoardUpdater {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final List<Semaphore> startList;
    private final List<Semaphore> finishedList;

    private final List<Worker> workers;
    private final ThreadFactory factory;

    ConcurrentBoardUpdater(final int numberOfWorkers, final ThreadFactory factory) {
        startList = new ArrayList<>(numberOfWorkers);
        finishedList = new ArrayList<>(numberOfWorkers);

        workers = new ArrayList<>(numberOfWorkers);
        this.factory = factory;
        for (int i = 0; i < numberOfWorkers; i++) {
            startList.add(new Semaphore(0, true));
            finishedList.add(new Semaphore(0, true));
            workers.add(new Worker(startList.get(i), finishedList.get(i)));
        }
    }

    @Override
    public void start() {
        super.start();

        workers.parallelStream()
                .map(factory::newThread)
                .forEach(Thread::start);
    }

    @Override
    public void stop() {
        super.stop();

        IntStream.range(0, workers.size())
                .peek(i -> workers.get(i).stop())
                .mapToObj(startList::get)
                .filter(Semaphore::hasQueuedThreads)
                .forEach(Semaphore::release);
    }

    @Override
    public Board update(final Board oldBoard) {
        Preconditions.checkNotNull(oldBoard, "board");

        checkStarted();
        checkNotStopped();

        // Create the new board
        final Board newBoard = Board.board(oldBoard.getWidth(), oldBoard.getHeight());

        // Prepare workers
        prepareWorkers(oldBoard, newBoard);
        logger.debug("Update started");

        // Wait for each worker to finish the update
        if (isNotStopped())
            finishedList.forEach(Semaphores::acquire);
        logger.debug("Finished update");

        return newBoard;
    }

    private void prepareWorkers(final Board oldBoard, final Board newBoard) {
        final int height = oldBoard.getHeight();
        final int offset = height / workers.size();
        int fromRow = 0, toRow = offset;
        for (int i = 1; i < workers.size(); i++) {
            workers.get(i).setBoards(fromRow, toRow, oldBoard, newBoard);
            startList.get(i).release();
            fromRow += offset;
            toRow += offset;
        }
        workers.get(0).setBoards(fromRow, fromRow + (height - fromRow), oldBoard, newBoard);
        startList.get(0).release();
    }

}
