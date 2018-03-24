package pcd.ass01.domain.impl;

import pcd.ass01.domain.Cell;

import static pcd.ass01.domain.Cell.ALIVE;
import static pcd.ass01.domain.Cell.DEAD;
import static pcd.ass01.util.Preconditions.checkNotNull;

final class BooleanArrayBoard extends AbstractBoard<boolean[]> {

    private final boolean[] cells;

    BooleanArrayBoard(final int width, final int height, final Cell[][] cells) {
        super(width, height);
        this.cells = new boolean[width * height];
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                setCell(x, y, cells[x][y]);
            }
        }
    }

    @Override
    public Cell getCell(final int x, final int y) {
        return toCell(cells[index(x, y)]);
    }

    @Override
    public void setCell(final int x, final int y, final Cell cell) {
        checkNotNull(cell, "cell");
        cells[index(x, y)] = toBoolean(cell);
    }

    @Override
    protected boolean[] getCells() {
        return cells;
    }

    private static Cell toCell(final boolean cell) {
        return cell ? ALIVE : DEAD;
    }

    private static boolean toBoolean(final Cell cell) {
        switch (cell) {
            case DEAD:
                return false;
            case ALIVE:
                return true;
            default:
                throw new IllegalStateException("Unknown cell state: " + cell);
        }
    }

}
