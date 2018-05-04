package pcd.ass02.view.factories;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.Optional;

/**
 * Utility class to create JavaFx windows using pattern Static Factory.
 */
public final class FxWindowFactory implements WindowFactory {

    private static final String APP_TITLE = "Regex Search Tool";
    private static final String APP_ICON_PATH = "/regex_search_tool.png";
    private static final String BUTTON_ICON_PATH = "/search_button_icon.png";
    private static final String FXML_PATH = "/main.fxml";
    private static final String CSS_PATH = "/main_style.css";

    private static FXMLLoader loader;


    public static FxWindowFactory defaultInstance() {
        return Holder.INSTANCE;
    }

    /**
     * @param <T> type of the presenter
     * @return reference to view presenter.
     */
    public static <T> T getPresenter() {
        return loader == null ? null : loader.getController();
    }

    /**
     * Load a new window. If it is contained in a menu, the method return the
     * root of the new scene.
     *
     * @param fxmlPath path of the GUI structure file FXML.
     * @return root.
     */
    private static Node openWindow(final String fxmlPath, final String cssPath, final boolean resizable) throws IOException {
        loader = new FXMLLoader(
                FxWindowFactory.class.getResource(fxmlPath));
        final BorderPane root = loader.load();
        final Stage stage = new Stage();
        stage.setResizable(resizable);
        final Scene scene = new Scene(root);
        scene.getStylesheets().add(FxWindowFactory.class
                .getResource(cssPath).toExternalForm());
        stage.setTitle(APP_TITLE);
        stage.getIcons().add(new Image(APP_ICON_PATH));
        stage.setScene(scene);
        stage.show();
        return root;
    }

    /**
     * Close a JavaFx window.
     *
     * @param sceneToClose link to the window to close.
     */
    private static void closeWindow(final Scene sceneToClose) {
        final Stage sceneStage = (Stage) sceneToClose.getWindow();
        sceneStage.close();
    }

    /**
     * Replace a old window with a new one.
     *
     * @param fxmlPath     path of the GUI structure file FXML to open.
     * @param sceneToClose link to the window to close.
     */
    public static void replaceWindow(final String fxmlPath, final Scene sceneToClose) throws IOException {
        FxWindowFactory.openWindow(fxmlPath, CSS_PATH, true);
        FxWindowFactory.closeWindow(sceneToClose);
    }

    /**
     * Show a simple info dialog with a optional image.
     *
     * @param title     header of the show dialog.
     * @param message   content of the dialog.
     * @param alertType to select the type of dialog.
     */
    public static void showDialog(final String title, final String message,
                                  final AlertType alertType) {
        final Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(APP_ICON_PATH));
        alert.showAndWait();
    }


    /**
     * @param title     of dialog window.
     * @param message   to user.
     * @param inputText to show in input text field.
     * @return input string written by user.
     */
    public static String createInputDialog(final String title, final String message, final String inputText) {
        final TextInputDialog dialog = new TextInputDialog(inputText);
        dialog.setTitle(title);
        dialog.setHeaderText("You have to input the requested data!");
        dialog.setContentText(message);
        final Optional<String> result = dialog.showAndWait();
        return result.orElse("");
    }

    public static void setSearchButtonGraphic(Button searchButton) {
        Image imageDecline = new Image(FxWindowFactory.class.getResourceAsStream(BUTTON_ICON_PATH));
        HBox hBox = new HBox();
        Label label = new Label("Search");
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 17.0");
        hBox.getChildren().addAll(label, new ImageView(imageDecline));
        hBox.setAlignment(Pos.CENTER);
        searchButton.setGraphic(hBox);
        searchButton.setStyle("-fx-base: #b6e7c9;");
    }

    public void openStartWindow() throws IOException {
        openWindow(FXML_PATH, CSS_PATH, false);
    }

    private static final class Holder {
        static final FxWindowFactory INSTANCE = new FxWindowFactory();
    }

    public static Stage getStage(Node node) {
        final Window window = node.getScene().getWindow();
        return (Stage) window;
    }

    public static Stage getStage(ActionEvent event) {
        final Object source = event.getSource();
        return getStage((Node) source);
    }

}

