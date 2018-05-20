package pcd.ass01.domain;

public interface BoardFactory {

    Board board(Cell[][] cells);

    Board board(int width, int height);

}
