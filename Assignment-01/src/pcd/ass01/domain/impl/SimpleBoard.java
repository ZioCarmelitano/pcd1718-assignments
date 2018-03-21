package pcd.ass01.domain.impl;

import pcd.ass01.domain.Board;
import pcd.ass01.domain.Cell;
import pcd.ass01.domain.CellUtils;

import java.util.Objects;

import static pcd.ass01.util.Preconditions.checkNotNull;

class SimpleBoard implements Board {

    private final Cell[] cells;
    private final int width;
    private final int height;

    SimpleBoard(final int width, final int height, final Cell[] cells) {
        this.width = width;
        this.height = height;
        this.cells = cells;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
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
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof Board && equals((Board) obj));
    }

    @Override
    public int hashCode() {
        return Objects.hash(height, width, cells);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder('[');
        sb.append("[[");
        sb.append(getCell(0, 0));
        for (int y = 1; y < width; y++) {
            sb.append(", ").append(getCell(0, y));
        }
        sb.append(']');
        for (int x = 1; x < height; x++) {
            sb.append(", [");
            sb.append(getCell(x, 0));
            for (int y = 1; y < width; y++) {
                sb.append(", ").append(getCell(x, y));
            }
            sb.append(']');
        }
        return sb.append(']').toString();
    }

    static  int index(final int x, final int y, final int width) {
        return x * width + y;
    }

    private int index(final int x, final int y) {
        return index(x, y, width);
    }

    private boolean equals(final Board other) {
        if (getWidth() != other.getWidth() || getHeight() != other.getHeight())
            return false;
        for (int x = 0; x < getHeight(); x++)
            for (int y = 0; y < getWidth(); y++)
                if (getCell(x, y) != other.getCell(x, y))
                    return false;
        return true;
    }

}
