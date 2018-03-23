package pcd.ass01;

import javafx.application.Application;
import javafx.stage.Stage;
import pcd.ass01.view.factories.FxWindowFactory;
import pcd.ass01.view.factories.WindowFactory;

import java.io.IOException;

public final class Launcher extends Application{

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        WindowFactory windowFactory = FxWindowFactory.defaultInstance();
        windowFactory.openStartWindow();
    }

}
