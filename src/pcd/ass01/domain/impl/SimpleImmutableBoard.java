package pcd.ass01.domain.impl;

import pcd.ass01.domain.Cell;

class SimpleImmutableBoard extends SimpleBoard {

    SimpleImmutableBoard(final int width, final int height, final Cell[] cells) {
        super(width, height, cells);
    }

    @Override
    public void setCell(final int x, final int y, final Cell cell) {
        throw new UnsupportedOperationException("void setCell(int, int, Cell) not supported");
    }

}
