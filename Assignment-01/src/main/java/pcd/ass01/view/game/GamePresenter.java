package pcd.ass01.view.game;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import pcd.ass01.domain.Board;
import pcd.ass01.interactors.BoardUpdater;

import java.net.URL;
import java.util.ResourceBundle;

import static pcd.ass01.view.factories.FxWindowFactory.*;
import static pcd.ass01.view.settings.SettingsPresenter.*;

public class GamePresenter implements Initializable{

    private static final String PLAY_ICON_PATH = "/play.png";
    private static final String STOP_ICON_PATH = "/stop.png";
    private static final String PAUSE_ICON_PATH = "/pause.png";

    private static final int BTN_START_HEIGHT = 30;
    private static final int BTN_START_WIDTH = 30;

    private static final int BTN_STOP_WIDTH = 30;
    private static final int BTN_STOP_HEIGHT = 30;

    private static final int BTN_PAUSE_HEIGHT = 30;
    private static final int BTN_PAUSE_WIDTH = 30;

    @FXML
    private Button buttonStart;

    @FXML
    private Button buttonStop;

    private GameUpdater gameUpdater;

    private Board board;

    private BoardUpdater updater;

    @FXML
    void playOrResume(ActionEvent event) {
        if(gameUpdater == null){
            launchGUIUpdater(event);
        } else if(gameUpdater.isPaused() && gameUpdater.isUpdating()) {
            gameUpdater.resumeGame();
        }else if(!gameUpdater.isPaused() && gameUpdater.isUpdating()){
            gameUpdater.pauseGame();
        }
        switchButtonGraphic();
    }

    private void switchButtonGraphic() {
        if(gameUpdater.isPaused()){
            defaultInstance().buildGameButton(PLAY_ICON_PATH, buttonStart,
                    BTN_START_HEIGHT, BTN_START_WIDTH);
        } else if (!gameUpdater.isPaused() && gameUpdater.isUpdating()){
            defaultInstance().buildGameButton(PAUSE_ICON_PATH, buttonStart,
                    BTN_PAUSE_HEIGHT, BTN_PAUSE_WIDTH);
        }
    }

    private void launchGUIUpdater(ActionEvent event) {
        if(gameUpdater == null) {
            Canvas boardView = (Canvas) getStage(event)
                    .getScene().lookup("#" + getBoardPanelId());
            gameUpdater = new GameUpdater(board, updater, boardView);
        }
        new Thread(gameUpdater).start();
    }

    @FXML
    void stop(ActionEvent event){
        if(gameUpdater == null){
            showDialog("Game isn't started",
                    "Please start the game before press STOP", Alert.AlertType.ERROR);
        } else{
            gameUpdater.stopGame();
            buttonStart.setDisable(true);
            buttonStop.setDisable(true);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buildBoardUpdater();
        buildGameBoard();
        defaultInstance().buildGameButton(PLAY_ICON_PATH, buttonStart, BTN_START_HEIGHT, BTN_START_WIDTH);
        defaultInstance().buildGameButton(STOP_ICON_PATH, buttonStop, BTN_STOP_HEIGHT, BTN_STOP_WIDTH);
    }

    private void buildGameBoard() {
        board = getBoardConfiguration();
    }

    private void buildBoardUpdater() {
        int numWorkers = getWorkersNumber();
        updater = BoardUpdater.create(numWorkers);
        updater.start();
    }

}
