package pcd.ass03.ex1.domain.impl;

import pcd.ass03.ex1.domain.Board;
import pcd.ass03.ex1.domain.BoardFactory;
import pcd.ass03.ex1.domain.Cell;
import pcd.ass03.ex1.util.Preconditions;

import static pcd.ass03.ex1.domain.Cell.DEAD;

public final class SimpleBoardFactory implements BoardFactory {

    public static SimpleBoardFactory defaultInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public Board board(final Cell[][] cells) {
        checkCells(cells);

        return board(cells[0].length, cells.length, cells);
    }

    @Override
    public Board board(final int width, final int height) {
        Preconditions.checkPositive(width, "width");
        Preconditions.checkPositive(height, "height");

        return board(width, height, emptyCells(width, height));
    }

    private static Board board(final int width, final int height, final Cell[][] cells) {
        return new BooleanArrayBoard(width, height, cells);
    }

    private static Cell[][] emptyCells(final int width, final int height) {
        final Cell[][] cells = new Cell[height][];
        for (int x = 0; x < height; x++) {
            cells[x] = new Cell[width];
            for (int y = 0; y < width; y++) {
                cells[x][y] = DEAD;
            }
        }

        return cells;
    }

    private static void checkCells(final Cell[][] cells) {
        Preconditions.checkNotNull(cells, "cells");

        final int width = cells[0].length;
        Preconditions.checkPositive(width, "width");
        final int height = cells.length;
        Preconditions.checkPositive(height, "height");

        for (int x = 0; x < height; x++) {
            final Cell[] row = cells[x];
            final String label = "cells[" + x + "]";

            Preconditions.checkLength(row, width, label);
            Preconditions.checkNotNulls(row, label);
        }
    }

    private static final class Holder {
        static final SimpleBoardFactory INSTANCE = new SimpleBoardFactory();
    }

}
