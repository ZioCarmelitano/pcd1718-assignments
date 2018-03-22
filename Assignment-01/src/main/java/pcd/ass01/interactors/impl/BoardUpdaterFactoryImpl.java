package pcd.ass01.interactors.impl;

import pcd.ass01.interactors.BoardUpdater;
import pcd.ass01.interactors.BoardUpdaterFactory;

import static pcd.ass01.util.Preconditions.checkNonNegative;

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
        checkNonNegative(numberOfWorkers, "numberOfWorkers");
        return numberOfWorkers == 1
                ? new SequentialBoardUpdater()
                : new ConcurrentBoardUpdater(numberOfWorkers);
    }

    private static final class Holder {
        static final BoardUpdaterFactoryImpl INSTANCE = new BoardUpdaterFactoryImpl();
    }

}
