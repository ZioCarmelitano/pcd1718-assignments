package pcd.ass03.ex1.view.factories;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pcd.ass03.ex1.domain.Board;
import pcd.ass03.ex1.util.Preconditions;
import pcd.ass03.ex1.view.game.RenderingService;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

import static pcd.ass03.ex1.util.Preconditions.checkState;
import static pcd.ass03.ex1.view.utils.ScrollManager.*;

/**
 * Utility class to create JavaFx windows using pattern Static Factory.
 */
public final class FxWindowFactory implements WindowFactory{

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String APP_TITLE = "Game Of Life";
    private static final String APP_ICON_PATH = "/game_of_life_icon.png";
    private static final String GAME_FXML_PATH = "/game_of_life.fxml";
    private static final String SETTINGS_FXML_PATH = "/initial_settings.fxml";
    private static final String GAME_CSS_PATH = "/game_of_life_style.css";
    private static final String SETTINGS_CSS_PATH = "/initial_settings_style.css";

    private static final String PANEL_CONTAINER_ID = "panelContainer";
    private static final String BOARD_PANEL_ID = "canvas";
    private static final String SCROLL_PANE_ID = "scrollPane";

    private static FXMLLoader loader;


    public static FxWindowFactory defaultInstance() {
        return Holder.INSTANCE;
    }

    /**
     *
     * @return reference to view presenter.
     * @param <T>
     *            type of the presenter
     */
    public static <T> T getPresenter() {
        return loader == null ? null : loader.getController();
    }

    /**
     * Load a new window. If it is contained in a menu, the method return the
     * root of the new scene.
     *
     * @param fxmlPath
     *            path of the GUI structure file FXML.
     *
     *
     * @return root.
     */
    private static BorderPane openWindow(final String fxmlPath, final String cssPath, final boolean resizable) throws IOException {
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
     * @param sceneToClose
     *            link to the window to close.
     */
    private static void closeWindow(final Scene sceneToClose) {
        final Stage sceneStage = (Stage) sceneToClose.getWindow();
        sceneStage.close();
    }

    /**
     * Replace a old window with a new one.
     *
     * @param fxmlPath
     *            path of the GUI structure file FXML to open.
     *
     * @param sceneToClose
     *            link to the window to close.
     */
    public static void replaceWindow(final String fxmlPath, final Scene sceneToClose) throws IOException {
        FxWindowFactory.openWindow(fxmlPath, GAME_CSS_PATH, true);
        FxWindowFactory.closeWindow(sceneToClose);
    }

    /**
     * Show a simple info dialog with a optional image.
     *
     * @param title
     *            header of the show dialog.
     * @param message
     *            content of the dialog.
     * @param alertType
     *            to select the type of dialog.
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
     *
     * @param title
     *            of dialog window.
     * @param message
     *            to user.
     * @param inputText
     *            to show in input text field.
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

    public void buildGameButton(String iconPath, Button button, int height, int width) {
        Image imageDecline = new Image(getClass().getResourceAsStream(iconPath));
        ImageView btnImage = new ImageView(imageDecline);
        btnImage.setFitHeight(height);
        btnImage.setFitWidth(width);
        button.setGraphic(btnImage);
    }

    @Override
    public void openStartWindow() throws IOException {
        openWindow(SETTINGS_FXML_PATH, SETTINGS_CSS_PATH, false);
    }

    @Override
    public void openGameWindow(int width, int height, Board board) throws IOException {
        BorderPane gamePane = openWindow(GAME_FXML_PATH, GAME_CSS_PATH, true);
        gamePane.setId(PANEL_CONTAINER_ID);
        Canvas gameBoardPanel = createBoardPanel(width, height, board);
        ScrollPane scrollPane = createScrollPane(gameBoardPanel);
        gamePane.setCenter(scrollPane);
        RenderingService.renderBoard(gameBoardPanel, board);
        handleWindowClosing(gamePane);
    }

    private ScrollPane createScrollPane(Canvas gameBoardPanel) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(gameBoardPanel);
        scrollPane.setId(SCROLL_PANE_ID);
        setScrollVerticalProperty(gameBoardPanel, scrollPane);
        setScrollHorizontalProperty(gameBoardPanel, scrollPane);
        return scrollPane;
    }

     private void handleWindowClosing(BorderPane gamePane) {
        getStage(gamePane).setOnCloseRequest((event) -> {
            try {
                openStartWindow();
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        });
    }

    private Canvas createBoardPanel(int width, int height, Board board) {
        Canvas gameBoardPanel = createCanvas(width, height);
        gameBoardPanel.setId(BOARD_PANEL_ID);
        return gameBoardPanel;
    }

    private Canvas createCanvas(int width, int height) {
       return new Canvas(width, height);
    }

    private static final class Holder {
        static final FxWindowFactory INSTANCE = new FxWindowFactory();
    }

    public static Stage getStage(Node node){
        final Window window = node.getScene().getWindow();
        Preconditions.checkState(window instanceof Stage, "window (%s) is not an instance of %s", window.getClass().getName(), Stage.class.getName());
        return (Stage) window;
    }

    public static Stage getStage(ActionEvent event) {
        final Object source = event.getSource();
        Preconditions.checkState(source instanceof Node, "source (%s) is not an instance of %s", source.getClass().getName(), Node.class.getName());
        return getStage((Node) source);
    }

    public static String getBoardPanelId() {
        return BOARD_PANEL_ID;
    }
}

