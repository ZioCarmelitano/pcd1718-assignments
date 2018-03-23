package pcd.ass01.domain.impl;

import pcd.ass01.domain.Cell;

import java.util.BitSet;

import static pcd.ass01.domain.Cell.ALIVE;
import static pcd.ass01.domain.Cell.DEAD;
import static pcd.ass01.util.Preconditions.checkNotNull;

public abstract class AbstractBitSetBoard extends AbstractBoard<BitSet> {

    private final BitSet cells;

    protected AbstractBitSetBoard(final int width, final int height, final Cell[][] cells) {
        super(width, height);

        this.cells = new BitSet(width * height);
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                setCell(x, y, cells[x][y]);
            }
        }
    }

    @Override
    public Cell getCell(final int x, final int y) {
        return toCell(cells.get(index(x, y)));
    }

    @Override
    public void setCell(final int x, final int y, final Cell cell) {
        checkNotNull(cell, "cell");
        cells.set(index(x, y), toBoolean(cell));
    }

    @Override
    public BitSet getCells() {
        return cells;
    }

    private static Cell toCell(boolean cell) {
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
