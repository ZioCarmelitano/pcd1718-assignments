package pcd.ass01.interactors;

import pcd.ass01.domain.Board;

import static pcd.ass01.interactors.impl.BoardUpdaterFactoryImpl.defaultInstance;

public interface BoardUpdater {

    static BoardUpdater create() {
        return defaultInstance().createBoardUpdater();
    }

    static BoardUpdater create(final int numberOfWorkers) {
        return defaultInstance().createBoardUpdater(numberOfWorkers);
    }

    Board update(Board board);

}
