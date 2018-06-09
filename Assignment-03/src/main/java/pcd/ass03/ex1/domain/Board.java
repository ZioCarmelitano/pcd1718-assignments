package pcd.ass03.ex1.domain;

import pcd.ass03.ex1.domain.impl.SimpleBoardFactory;

public interface Board {

    static Board board(final int width, final int height) {
        return SimpleBoardFactory.defaultInstance().board(width, height);
    }

    static Board board(final Cell[][] cells) {
        return SimpleBoardFactory.defaultInstance().board(cells);
    }

    int getHeight();

    int getWidth();

    Cell getCell(int x, int y);

    void setCell(int x, int y, Cell cell);

}
