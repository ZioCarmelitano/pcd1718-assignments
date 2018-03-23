package pcd.ass01.domain;

import static pcd.ass01.domain.Board.Order;

public interface BoardFactory {

    Board board(Cell[][] cells, Order order);

    Board board(int width, int height, Order order);

}
