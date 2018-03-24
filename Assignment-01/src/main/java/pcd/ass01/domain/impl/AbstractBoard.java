package pcd.ass01.domain.impl;

import pcd.ass01.domain.Board;
import pcd.ass01.domain.Cell;

import java.util.Objects;

import static pcd.ass01.domain.Cell.ALIVE;
import static pcd.ass01.domain.Cell.DEAD;

public abstract class AbstractBoard<C> implements Board {

    private final int width;
    private final int height;

    AbstractBoard(final int width, final int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public final int getWidth() {
        return width;
    }

    @Override
    public final int getHeight() {
        return height;
    }

    protected abstract C getCells();

    @Override
    public final boolean equals(final Object obj) {
        return this == obj || (obj instanceof Board && equals((Board) obj));
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getWidth(), getHeight(), getCells());
    }

    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder("[[");
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

    protected final int index(final int x, final int y) {
        return x * getWidth() + y;
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
