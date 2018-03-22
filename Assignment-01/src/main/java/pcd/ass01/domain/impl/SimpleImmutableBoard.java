package pcd.ass01.domain.impl;

import pcd.ass01.domain.Cell;

class SimpleImmutableBoard extends SimpleBoard {

    SimpleImmutableBoard(final int height, final int width, final Cell[] cells) {
        super(height, width, cells);
    }

    @Override
    public void setCell(final int x, final int y, final Cell cell) {
        throw new UnsupportedOperationException("void setCell(int, int, Cell) not supported");
    }

}
