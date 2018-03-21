package pcd.ass01.domain;

public interface BoardFactory {

    Board createBoard(Cell[][] cells);

    Board createBoard(int width, int height);

    Board createImmutableBoard(Cell[][] cells);

    Board createImmutableBoard(Board board);

}

