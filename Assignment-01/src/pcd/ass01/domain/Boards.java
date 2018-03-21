package pcd.ass01.domain;

import static pcd.ass01.domain.Cell.ALIVE;
import static pcd.ass01.domain.Cell.DEAD;

public class Boards {

    public static Board randomBoard(final int width, final int height) {
        final Board board = Board.board(width, height);

        final Board newBoard = Board.board(width, height);
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                board.setCell(x, y, randomCell());
            }
        }

        return board;
    }

    private static Cell randomCell() {
        return Math.random() >= 0.5 ? ALIVE : DEAD;
    }


}
