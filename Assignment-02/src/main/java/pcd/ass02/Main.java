package pcd.ass02;

import javafx.application.Application;
import javafx.stage.Stage;
import pcd.ass02.view.factories.FxWindowFactory;
import pcd.ass02.view.factories.WindowFactory;

public class Main extends Application {

    public static void main(final String... args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        WindowFactory windowFactory = FxWindowFactory.defaultInstance();
        windowFactory.openStartWindow();
    }

}
