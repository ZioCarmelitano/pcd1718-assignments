package pcd.ass01.test;

import pcd.ass01.domain.Board;
import pcd.ass01.domain.Boards;
import pcd.ass01.interactors.ConcurrentBoardUpdater;
import pcd.ass01.interactors.SequentialBoardUpdater;

public class Test {

    public static void main(String[] args) {
        ConcurrentBoardUpdater concurrent = new ConcurrentBoardUpdater(Runtime.getRuntime().availableProcessors());
        SequentialBoardUpdater sequential = new SequentialBoardUpdater();
        final Board board = Boards.randomBoard(100, 100);

        if (sequential.update(board).equals(concurrent.update(board)))
            System.out.println("Boards are equal");
        else
            System.out.println("Boards are not equal");
    }

}
