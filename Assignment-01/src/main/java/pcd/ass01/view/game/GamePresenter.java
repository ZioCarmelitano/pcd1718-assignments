package pcd.ass01.view.game;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import pcd.ass01.domain.Board;
import pcd.ass01.interactors.BoardUpdater;

import java.net.URL;
import java.util.ResourceBundle;

import static pcd.ass01.domain.Board.Order.ROW_MAJOR;
import static pcd.ass01.view.factories.FxWindowFactory.*;
import static pcd.ass01.view.settings.SettingsPresenter.getHeight;
import static pcd.ass01.view.settings.SettingsPresenter.getWidth;

public class GamePresenter implements Initializable{

    private static final String PLAY_ICON_PATH = "/play.png";
    private static final String STOP_ICON_PATH = "/stop.png";

    private static final int BTN_START_HEIGHT = 30;
    private static final int BTN_START_WIDTH = 30;
    private static final int BTN_STOP_WIDTH = 30;
    private static final int BTN_STOP_HEIGHT = 30;

    @FXML
    private Button buttonStart;

    @FXML
    private Button btnStop;

    private BoardUpdater updater;

    private Board board;

    private Canvas gameBoard;

    @FXML
    void play(ActionEvent event) throws InterruptedException {
        System.out.println(board);
        this.board = updater.update(board);
        if(gameBoard == null) {
            gameBoard = (Canvas) getStage(buttonStart).getScene().lookup("#canvas");
        }
        System.out.println(board);
        drawBoard(gameBoard, board);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buildBoardUpdater();
        defaultInstance().buildGameButton(PLAY_ICON_PATH, buttonStart, BTN_START_HEIGHT, BTN_START_WIDTH);
        defaultInstance().buildGameButton(STOP_ICON_PATH, btnStop, BTN_STOP_HEIGHT, BTN_STOP_WIDTH);
    }

    private void buildBoardUpdater() {
        board = Board.board(getHeight(), getWidth(), ROW_MAJOR);
        updater = BoardUpdater.create(5);
        updater.start();
    }


}
