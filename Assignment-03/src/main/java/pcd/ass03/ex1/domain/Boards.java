package pcd.ass03.ex1.domain;

import java.util.Arrays;

import static pcd.ass03.ex1.domain.Cell.ALIVE;
import static pcd.ass03.ex1.domain.Cell.DEAD;

public final class Boards {

    public static Board randomBoard(final int width, final int height) {
        final Board board = Board.board(width, height);

        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                board.setCell(x, y, randomCell());
            }
        }

        return board;
    }

    public static Board gosperGliderGun(final int width, final int height) {
        final Cell[][] cells = new Cell[height][];
        for (int i = 0; i < cells.length; i++) {
            cells[i] = new Cell[width];
            Arrays.fill(cells[i], DEAD);
        }

        cells[5][1] = ALIVE;
        cells[6][1] = ALIVE;
        cells[5][2] = ALIVE;
        cells[6][2] = ALIVE;

        cells[5][11] = ALIVE;
        cells[6][11] = ALIVE;
        cells[7][11] = ALIVE;

        cells[4][12] = ALIVE;
        cells[8][12] = ALIVE;

        cells[3][13] = ALIVE;
        cells[3][14] = ALIVE;
        cells[9][13] = ALIVE;
        cells[9][14] = ALIVE;

        cells[6][15] = ALIVE;

        cells[4][16] = ALIVE;
        cells[8][16] = ALIVE;

        cells[5][17] = ALIVE;
        cells[6][17] = ALIVE;
        cells[7][17] = ALIVE;

        cells[6][18] = ALIVE;


        cells[3][21] = ALIVE;
        cells[4][21] = ALIVE;
        cells[5][21] = ALIVE;
        cells[3][22] = ALIVE;
        cells[4][22] = ALIVE;
        cells[5][22] = ALIVE;

        cells[2][23] = ALIVE;
        cells[6][23] = ALIVE;

        cells[1][25] = ALIVE;
        cells[2][25] = ALIVE;
        cells[6][25] = ALIVE;
        cells[7][25] = ALIVE;


        cells[3][35] = ALIVE;
        cells[4][35] = ALIVE;
        cells[3][36] = ALIVE;
        cells[4][36] = ALIVE;

        return Board.board(cells);
    }

    private static Cell randomCell() {
        return Math.random() >= 0.5 ? ALIVE : DEAD;
    }

    private Boards() {
    }

}
