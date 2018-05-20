package pcd.ass03.ex1.interactors;

import pcd.ass03.ex1.domain.Board;
import pcd.ass03.ex1.interactors.impl.BoardUpdaterFactoryImpl;

import java.util.concurrent.ThreadFactory;

public interface BoardUpdater {

    static BoardUpdater create() {
        return BoardUpdaterFactoryImpl.defaultInstance().createBoardUpdater();
    }

    static BoardUpdater create(final int numberOfWorkers) {
        return BoardUpdaterFactoryImpl.defaultInstance().createBoardUpdater(numberOfWorkers);
    }

    static BoardUpdater create(final int numberOfWorkers, final ThreadFactory factory) {
        return BoardUpdaterFactoryImpl.defaultInstance().createBoardUpdater(numberOfWorkers, factory);
    }

    void start();

    void stop();

    Board update(Board board);

}
