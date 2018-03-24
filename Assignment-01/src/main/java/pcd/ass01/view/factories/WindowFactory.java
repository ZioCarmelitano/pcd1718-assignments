package pcd.ass01.view.factories;

import pcd.ass01.domain.Board;

import java.io.IOException;

public interface WindowFactory {

    void openStartWindow() throws IOException;

    void openGameWindow(int width, int height, Board board) throws IOException;
}
