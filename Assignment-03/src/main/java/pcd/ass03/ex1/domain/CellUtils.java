package pcd.ass03.ex1.domain;

import pcd.ass03.ex1.util.Preconditions;

public final class CellUtils {

    private static final int MIN_ALIVE_NEIGHBORS = 2;
    private static final int MAX_ALIVE_NEIGHBORS = 3;

    public static IllegalStateException throwUnknownCellState(final Cell cell) {
        throw new IllegalStateException("Unknown cell state: " + cell);
    }

    public static Cell update(final Board board, final int x, final int y) {
        Preconditions.checkNotNull(board, "board");

        final int aliveNeighbors = countAliveNeighbors(board, x, y);
        if (becomesAlive(board, x, y, aliveNeighbors))
            return Cell.ALIVE;
        else if (dies(board, x, y, aliveNeighbors))
            return Cell.DEAD;
        return board.getCell(x, y);
    }

    private static boolean dies(final Board board, final int x, final int y, final int aliveNeighbors) {
        return board.getCell(x, y) == Cell.ALIVE && (aliveNeighbors < MIN_ALIVE_NEIGHBORS || aliveNeighbors > MAX_ALIVE_NEIGHBORS);
    }

    private static boolean becomesAlive(final Board board, final int x, final int y, final int aliveNeighbors) {
        return board.getCell(x, y) == Cell.DEAD && aliveNeighbors == MAX_ALIVE_NEIGHBORS;
    }

    private static int countAliveNeighbors(final Board board, final int x, final int y) {
        int sum = 0;
        for (int i = -1; i <= 1; i++)
            for (int j = -1; j <= 1; j++)
                if (i != 0 || j != 0)
                    sum += value(getCellNormalized(board, x + i, y + j));
        return sum;
    }

    private static Cell getCellNormalized(final Board board, final int x, final int y) {
        if (isOutOfBounds(board, x, y))
            return Cell.DEAD;
        return board.getCell(x, y);
    }

    private static int value(final Cell cell) {
        switch (cell) {
            case DEAD:
                return 0;
            case ALIVE:
                return 1;
            default:
                throw CellUtils.throwUnknownCellState(cell);
        }
    }

    private static boolean isOutOfBounds(Board board, int x, int y) {
        return x < 0 || x >= board.getHeight() || y < 0 || y >= board.getWidth();
    }

    private CellUtils() {
    }

}
