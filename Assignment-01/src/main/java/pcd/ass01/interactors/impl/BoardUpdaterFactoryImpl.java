package pcd.ass01.interactors.impl;

import pcd.ass01.interactors.BoardUpdater;
import pcd.ass01.interactors.BoardUpdaterFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static pcd.ass01.util.Preconditions.checkNonNegative;
import static pcd.ass01.util.Preconditions.checkNotNull;

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
        return createBoardUpdater(numberOfWorkers, Executors.defaultThreadFactory());
    }

    @Override
    public BoardUpdater createBoardUpdater(final int numberOfWorkers, final ThreadFactory factory) {
        checkNonNegative(numberOfWorkers, "numberOfWorkers");
        checkNotNull(factory, "factory");
        return numberOfWorkers == 1
                ? new SequentialBoardUpdater()
                : new ConcurrentBoardUpdater(numberOfWorkers, factory);
    }

    private static final class Holder {
        static final BoardUpdaterFactoryImpl INSTANCE = new BoardUpdaterFactoryImpl();
    }

}
