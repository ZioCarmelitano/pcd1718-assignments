package pcd.ass01.domain.impl;

import pcd.ass01.domain.Cell;

import static pcd.ass01.domain.Board.Order.COLUMN_MAJOR;

final class ColumnMajorBoard extends AbstractArrayBoard {

    protected ColumnMajorBoard(final int width, final int height, final Cell[][] cells) {
        super(width, height, cells);
    }

    @Override
    public Order getOrder() {
        return COLUMN_MAJOR;
    }

    @Override
    protected int index(int x, int y) {
        return y * getHeight() + x;
    }

}
