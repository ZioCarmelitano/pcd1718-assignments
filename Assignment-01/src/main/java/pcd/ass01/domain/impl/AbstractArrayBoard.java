package pcd.ass01.domain.impl;

import pcd.ass01.domain.Cell;

import static pcd.ass01.util.Preconditions.checkNotNull;

public abstract class AbstractArrayBoard extends AbstractBoard<Cell[]> {

    private final Cell[] cells;

    AbstractArrayBoard(final int width, final int height, final Cell[][] cells) {
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
    Cell[] getCells() {
        return cells;
    }

}
