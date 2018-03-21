package pcd.ass01.interactors;

import pcd.ass01.domain.Board;
import pcd.ass01.domain.CellUtils;

import static pcd.ass01.util.Preconditions.checkNotNull;

public class SequentialBoardUpdater implements BoardUpdater {

    @Override
    public Board update(final Board board) {
        checkNotNull(board, "board");

        final int width = board.getWidth();
        final int height = board.getHeight();

        final Board newBoard = Board.board(width, height);
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                newBoard.setCell(x, y, CellUtils.update(board, x, y));
            }
        }
        return newBoard;
    }

}
