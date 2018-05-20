package pcd.ass03.ex1.interactors.impl;

import pcd.ass03.ex1.domain.Board;
import pcd.ass03.ex1.domain.CellUtils;
import pcd.ass03.ex1.util.Preconditions;

final class SequentialBoardUpdater extends AbstractBoardUpdater {

    @Override
    public Board update(final Board oldBoard) {
        Preconditions.checkNotNull(oldBoard, "board");

        checkStarted();
        checkNotStopped();

        final int height = oldBoard.getHeight();
        final int width = oldBoard.getWidth();

        final Board newBoard = Board.board(width, height);
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                newBoard.setCell(x, y, CellUtils.update(oldBoard, x, y));
            }
        }
        return newBoard;
    }

}
