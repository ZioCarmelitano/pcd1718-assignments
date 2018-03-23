package pcd.ass01.domain;

import pcd.ass01.domain.impl.SimpleBoardFactory;

public interface Board {

    enum Order {
        ROW_MAJOR,
        COLUMN_MAJOR
    }

    static Board board(final int width, final int height, final Order order) {
        return SimpleBoardFactory.defaultInstance().board(width, height, order);
    }

    static Board board(final Cell[][] cells, final Order order) {
        return SimpleBoardFactory.defaultInstance().board(cells, order);
    }

    int getHeight();

    int getWidth();

    Cell getCell(int x, int y);

    void setCell(int x, int y, Cell cell);

    Order getOrder();

}
