package pcd.ass03.ex1.interactors;

import java.util.concurrent.ThreadFactory;

public interface BoardUpdaterFactory {

    BoardUpdater createBoardUpdater();

    BoardUpdater createBoardUpdater(int numberOfWorkers);

    BoardUpdater createBoardUpdater(int numberOfWorkers, ThreadFactory factory);

}
