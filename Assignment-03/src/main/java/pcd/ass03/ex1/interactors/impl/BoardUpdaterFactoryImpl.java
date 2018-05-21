package pcd.ass03.ex1.interactors.impl;

import pcd.ass03.ex1.interactors.BoardUpdater;
import pcd.ass03.ex1.interactors.BoardUpdaterFactory;
import pcd.ass03.ex1.util.Preconditions;

import java.util.concurrent.ThreadFactory;

public class BoardUpdaterFactoryImpl implements BoardUpdaterFactory {

    public static BoardUpdaterFactoryImpl defaultInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public BoardUpdater createBoardUpdater() {
        return new SequentialBoardUpdater();
    }

    @Override
    public BoardUpdater createBoardUpdater(int numberOfWorkers) {
        return createBoardUpdater(numberOfWorkers, Thread::new);
    }

    @Override
    public BoardUpdater createBoardUpdater(final int numberOfWorkers, final ThreadFactory factory) {
        Preconditions.checkNonNegative(numberOfWorkers, "numberOfWorkers");
        Preconditions.checkNotNull(factory, "factory");
        return numberOfWorkers == 1
                ? new SequentialBoardUpdater()
                : new ConcurrentBoardUpdater(numberOfWorkers, factory);
    }

    private static final class Holder {
        static final BoardUpdaterFactoryImpl INSTANCE = new BoardUpdaterFactoryImpl();
    }

}
