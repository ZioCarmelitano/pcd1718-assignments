package pcd.ass01.domain;

public interface BoardFactory {

    Board createBoard(Cell[][] cells);

    Board createBoard(int height, int width);

    Board createImmutableBoard(Cell[][] cells);

    Board createImmutableBoard(Board board);

}

