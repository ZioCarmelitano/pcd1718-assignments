package pcd.ass01.domain;

import pcd.ass01.domain.impl.BoardFactoryImpl;

public interface Board {

    static Board board(final int height, final int width) {
        return BoardFactoryImpl.defaultInstance().createBoard(height, width);
    }

    static Board board(final Cell[][] cells) {
        return BoardFactoryImpl.defaultInstance().createBoard(cells);
    }

    static Board immutableBoard(final Board board) {
        return BoardFactoryImpl.defaultInstance().createImmutableBoard(board);
    }

    static Board immutableBoard(final Cell[][] cells) {
        return BoardFactoryImpl.defaultInstance().createImmutableBoard(cells);
    }

    int getHeight();

    int getWidth();

    Cell getCell(int x, int y);

    void setCell(int x, int y, Cell cell);

}
