package pcd.ass01.view.game;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import pcd.ass01.domain.Board;
import pcd.ass01.interactors.BoardUpdater;
import pcd.ass01.interactors.impl.GUIUpdater;

import java.net.URL;
import java.util.ResourceBundle;

import static pcd.ass01.domain.Board.Order.ROW_MAJOR;
import static pcd.ass01.domain.Boards.randomBoard;
import static pcd.ass01.view.factories.FxWindowFactory.*;
import static pcd.ass01.view.settings.SettingsPresenter.getHeight;
import static pcd.ass01.view.settings.SettingsPresenter.getWidth;
import static pcd.ass01.view.settings.SettingsPresenter.getWorkersNumber;

public class GamePresenter implements Initializable{

    private static final String PLAY_ICON_PATH = "/play.png";
    private static final String STOP_ICON_PATH = "/stop.png";

    private static final int BTN_START_HEIGHT = 30;
    private static final int BTN_START_WIDTH = 30;
    private static final int BTN_STOP_WIDTH = 30;
    private static final int BTN_STOP_HEIGHT = 30;
    private static final String PAUSE_ICON_PATH = "/pause.png";
    private static final int BTN_PAUSE_HEIGHT = 30;
    private static final int BTN_PAUSE_WIDTH = 30;

    @FXML
    private Button buttonStart;

    @FXML
    private Button btnStop;

    private GUIUpdater guiUpdater;

    private Board board;

    private BoardUpdater updater;

    @FXML
    void playOrResume(ActionEvent event) {
        if(guiUpdater == null){
            createGUIUpdater(event);
            guiUpdater.start();
        } else if(guiUpdater.isPaused()) {
            guiUpdater.resumeGame();
        }else{
            guiUpdater.pauseGame();
        }
        switchButtonGraphic();
    }

    private void switchButtonGraphic() {
        if(guiUpdater.isPaused()){
            defaultInstance().buildGameButton(PLAY_ICON_PATH, buttonStart,
                    BTN_START_HEIGHT, BTN_START_WIDTH);
        } else if (!guiUpdater.isPaused() && guiUpdater.isUpdating()){
            defaultInstance().buildGameButton(PAUSE_ICON_PATH, buttonStart,
                    BTN_PAUSE_HEIGHT, BTN_PAUSE_WIDTH);
        }
    }

    private void createGUIUpdater(ActionEvent event) {
        if(guiUpdater == null) {
            Canvas boardView = (Canvas) getStage(event)
                    .getScene().lookup("#canvas");
            guiUpdater = new GUIUpdater(board, updater, boardView);
        }
    }

    @FXML
    void stop(ActionEvent event){
        if(!guiUpdater.stopGame()){
            showDialog("Execution Error",
                    "Game has been already stopped", Alert.AlertType.ERROR);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buildBoardUpdater();
        buildGameBoard();
        defaultInstance().buildGameButton(PLAY_ICON_PATH, buttonStart, BTN_START_HEIGHT, BTN_START_WIDTH);
        defaultInstance().buildGameButton(STOP_ICON_PATH, btnStop, BTN_STOP_HEIGHT, BTN_STOP_WIDTH);
    }

    private void buildGameBoard() {
        board = randomBoard(getHeight(), getWidth(), ROW_MAJOR);
    }

    private void buildBoardUpdater() {
        int numWorkers = getWorkersNumber();
        updater = BoardUpdater.create(numWorkers);
        updater.start();
    }

}
