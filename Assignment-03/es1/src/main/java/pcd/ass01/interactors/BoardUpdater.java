package pcd.ass01.interactors;

import pcd.ass01.domain.Board;

import java.util.concurrent.ThreadFactory;

import static pcd.ass01.interactors.impl.BoardUpdaterFactoryImpl.defaultInstance;

public interface BoardUpdater {

    static BoardUpdater create() {
        return defaultInstance().createBoardUpdater();
    }

    static BoardUpdater create(final int numberOfWorkers) {
        return defaultInstance().createBoardUpdater(numberOfWorkers);
    }

    static BoardUpdater create(final int numberOfWorkers, final ThreadFactory factory) {
        return defaultInstance().createBoardUpdater(numberOfWorkers, factory);
    }

    void start();

    void stop();

    Board update(Board board);

}
