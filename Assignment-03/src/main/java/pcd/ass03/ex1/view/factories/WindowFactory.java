package pcd.ass03.ex1.view.factories;

import pcd.ass03.ex1.domain.Board;

import java.io.IOException;

public interface WindowFactory {

    void openStartWindow() throws IOException;

    void openGameWindow(int width, int height, Board board) throws IOException;
}
