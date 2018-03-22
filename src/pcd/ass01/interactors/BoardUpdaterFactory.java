package pcd.ass01.interactors;

public interface BoardUpdaterFactory {

    BoardUpdater createBoardUpdater();

    BoardUpdater createBoardUpdater(int numberOfWorkers);

}
