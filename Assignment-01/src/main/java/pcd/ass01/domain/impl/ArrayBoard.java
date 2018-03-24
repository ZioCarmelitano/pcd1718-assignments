package pcd.ass01.domain.impl;

import pcd.ass01.domain.Cell;

import static pcd.ass01.util.Preconditions.checkNotNull;

final class ArrayBoard extends AbstractBoard<Cell[]> {

    private final Cell[] cells;

    ArrayBoard(final int width, final int height, final Cell[][] cells) {
        super(width, height);
        this.cells = new Cell[width * height];
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                setCell(x, y, cells[x][y]);
            }
        }
    }

    @Override
    public Cell getCell(final int x, final int y) {
        return cells[index(x, y)];
    }

    @Override
    public void setCell(final int x, final int y, final Cell cell) {
        checkNotNull(cell, "cell");
        cells[index(x, y)] = cell;
    }

    @Override
    protected Cell[] getCells() {
        return cells;
    }

}
