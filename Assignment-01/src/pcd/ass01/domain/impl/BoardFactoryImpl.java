package pcd.ass01.domain.impl;

import pcd.ass01.domain.Board;
import pcd.ass01.domain.BoardFactory;
import pcd.ass01.domain.Cell;
import pcd.ass01.util.ArrayUtils;

import static pcd.ass01.domain.Cell.DEAD;
import static pcd.ass01.util.Preconditions.*;

public final class BoardFactoryImpl implements BoardFactory {

    private static final class Holder {
        static final BoardFactory INSTANCE = new BoardFactoryImpl();
    }

    public static BoardFactory defaultInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public Board createBoard(final Cell[][] cells) {
        checkCells(cells);

        return new SimpleBoard(cells[0].length, cells.length, ArrayUtils.flatten(cells));
    }

    @Override
    public Board createBoard(final int width, final int height) {
        checkPositive(width, "width");
        checkPositive(height, "height");

        return new SimpleBoard(width, height, emptyCells(width, height));
    }

    @Override
    public Board createImmutableBoard(final Cell[][] cells) {
        checkCells(cells);

        return new SimpleImmutableBoard(cells[0].length, cells.length, ArrayUtils.flatten(cells));
    }

    @Override
    public Board createImmutableBoard(final Board board) {
        checkNotNull(board, "board");

        return board instanceof SimpleImmutableBoard
                ? board
                : new SimpleImmutableBoard(board.getWidth(), board.getHeight(), cells(board));
    }

    private static Cell[] cells(final Board board) {
        final int height = board.getHeight();
        final int width = board.getWidth();
        final Cell[] cells = new Cell[height * width];
        for (int x = 0; x < height; x++)
            for (int y = 0; y < width; y++)
                cells[SimpleBoard.index(x, y, width)] = board.getCell(x, y);
        return cells;
    }

    private static Cell[] emptyCells(final int width, final int height) {
        final Cell[] cells = new Cell[width * height];
        for (int i = 0; i < width * height; i++)
            cells[i] = DEAD;

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

}
