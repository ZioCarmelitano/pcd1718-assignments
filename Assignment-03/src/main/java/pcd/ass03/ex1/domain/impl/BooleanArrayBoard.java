package pcd.ass03.ex1.domain.impl;

import pcd.ass03.ex1.domain.Cell;
import pcd.ass03.ex1.domain.CellUtils;
import pcd.ass03.ex1.util.Preconditions;

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
        Preconditions.checkNotNull(cell, "cell");
        cells[index(x, y)] = toBoolean(cell);
    }

    @Override
    protected boolean[] getCells() {
        return cells;
    }

    private static Cell toCell(final boolean cell) {
        return cell ? Cell.ALIVE : Cell.DEAD;
    }

    private static boolean toBoolean(final Cell cell) {
        switch (cell) {
            case DEAD:
                return false;
            case ALIVE:
                return true;
            default:
                throw CellUtils.throwUnknownCellState(cell);
        }
    }

}
