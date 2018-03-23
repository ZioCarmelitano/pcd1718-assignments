package pcd.ass01.domain.impl;

import pcd.ass01.domain.Board;
import pcd.ass01.domain.BoardFactory;
import pcd.ass01.domain.Cell;

import static pcd.ass01.domain.Board.Order;
import static pcd.ass01.domain.Cell.DEAD;
import static pcd.ass01.util.Preconditions.*;

public final class SimpleBoardFactory implements BoardFactory {

    public static SimpleBoardFactory defaultInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public Board board(final Cell[][] cells, final Order order) {
        checkCells(cells);

        return createBoard(cells[0].length, cells.length, cells, order);
    }

    @Override
    public Board board(final int width, final int height, final Order order) {
        checkPositive(width, "width");
        checkPositive(height, "height");

        return createBoard(width, height, emptyCells(width, height), order);
    }

    private static Board createBoard(final int width, final int height, final Cell[][] cells, final Order order) {
        switch (order) {
            case ROW_MAJOR:
                return new RowMajorBoard(width, height, cells);
            case COLUMN_MAJOR:
                return new ColumnMajorBoard(width, height, cells);
            default:
                throw new IllegalStateException("Unknown order mode: " + order);
        }
    }

    private static Cell[][] cells(final Board board) {
        final int height = board.getHeight();
        final int width = board.getWidth();
        final Cell[][] cells = new Cell[height][];
        for (int x = 0; x < height; x++) {
            cells[x] = new Cell[width];
            for (int y = 0; y < width; y++)
                cells[x][y] = board.getCell(x, y);
        }
        return cells;
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
        checkNotNull(cells, "cells");

        final int width = cells[0].length;
        checkPositive(width, "width");
        final int height = cells.length;
        checkPositive(height, "height");

        for (int x = 0; x < height; x++) {
            final Cell[] row = cells[x];
            final String label = "cells[" + x + "]";

            checkLength(row, width, label);
            checkNotNulls(row, label);
        }
    }

    private static final class Holder {
        static final SimpleBoardFactory INSTANCE = new SimpleBoardFactory();
    }

}
