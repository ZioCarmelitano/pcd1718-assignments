package pcd.ass01.domain.impl;

import pcd.ass01.domain.Cell;

import static pcd.ass01.domain.Board.Order.ROW_MAJOR;

final class SimpleBoard extends AbstractArrayBoard {

    SimpleBoard(final int width, final int height, final Cell[][] cells) {
        super(width, height, cells);
    }

    @Override
    protected int index(final int x, final int y) {
        return x * getWidth() + y;
    }

}
