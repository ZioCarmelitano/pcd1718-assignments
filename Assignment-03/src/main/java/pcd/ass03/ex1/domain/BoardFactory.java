package pcd.ass03.ex1.domain;

public interface BoardFactory {

    Board board(Cell[][] cells);

    Board board(int width, int height);

}
