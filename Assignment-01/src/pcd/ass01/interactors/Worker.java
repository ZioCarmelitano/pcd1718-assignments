package pcd.ass01.interactors;

import pcd.ass01.domain.Board;
import pcd.ass01.domain.CellUtils;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

final class Worker {

    private final CyclicBarrier updateStarted;
    private final CyclicBarrier finishedUpdate;
    private final Thread backingThread;

    private int fromRow;
    private int toRow;
    private Board oldBoard;
    private Board newBoard;

    private final AtomicBoolean stopped;

    Worker(final CyclicBarrier updateStarted, final CyclicBarrier finishedUpdate) {
        this.updateStarted = updateStarted;
        this.finishedUpdate = finishedUpdate;
        stopped = new AtomicBoolean();
        backingThread = new Thread(this::updateBoard);
        backingThread.start();
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
        while (!stopped.get())  {
            // Wait for the start of the update
            try {
                updateStarted.await();
            } catch (final InterruptedException | BrokenBarrierException e) {
                backingThread.interrupt();
            }

            // Update the given portion of the board
            System.out.println("Worker from " + fromRow + " to " + toRow + " started on thread " + backingThread.getName());
            for (int x = fromRow; x < toRow; x++) {
                for (int y = 0; y < oldBoard.getWidth(); y++) {
                    newBoard.setCell(x, y, CellUtils.update(oldBoard, x, y));
                }
            }

            System.out.println("Worker from " + fromRow + " to " + toRow + " finished on thread " + backingThread.getName());

            try {
                finishedUpdate.await();
            } catch (final InterruptedException | BrokenBarrierException e) {
                backingThread.interrupt();
            }
        }
    }

}
