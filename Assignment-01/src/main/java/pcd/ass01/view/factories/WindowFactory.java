package pcd.ass01.view.factories;

import java.io.IOException;

public interface WindowFactory {

    void openStartWindow() throws IOException;

    void openGameWindow(int width, int height) throws IOException;
}
