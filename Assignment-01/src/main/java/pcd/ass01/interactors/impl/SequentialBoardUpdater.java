package pcd.ass01.interactors.impl;

import pcd.ass01.domain.Board;
import pcd.ass01.domain.CellUtils;
import pcd.ass01.interactors.BoardUpdater;

import static pcd.ass01.util.Preconditions.checkNotNull;

final class SequentialBoardUpdater extends AbstractBoardUpdater implements BoardUpdater {

    @Override
    public Board update(final Board oldBoard) {
        checkNotNull(oldBoard, "board");

        checkStarted();
        checkNotStopped();

        final int height = oldBoard.getHeight();
        final int width = oldBoard.getWidth();

        final Board newBoard = Board.board(width, height, oldBoard.getOrder());
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                newBoard.setCell(x, y, CellUtils.update(oldBoard, x, y));
            }
        }
        return newBoard;
    }

}
