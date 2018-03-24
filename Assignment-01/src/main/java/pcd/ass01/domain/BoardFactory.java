package pcd.ass01.domain;

import static pcd.ass01.domain.Board.Order;

public interface BoardFactory {

    Board board(Cell[][] cells);

    Board board(int width, int height);

}
