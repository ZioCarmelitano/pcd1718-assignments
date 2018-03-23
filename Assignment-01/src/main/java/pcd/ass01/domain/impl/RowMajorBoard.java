package pcd.ass01.domain.impl;

import pcd.ass01.domain.Cell;

import static pcd.ass01.domain.Board.Order.ROW_MAJOR;

final class RowMajorBoard extends AbstractArrayBoard {

    RowMajorBoard(final int width, final int height, final Cell[][] cells) {
        super(width, height, cells);
    }

    @Override
    public Order getOrder() {
        return ROW_MAJOR;
    }

    @Override
    protected int index(final int x, final int y) {
        return x * getWidth() + y;
    }

}
