package pcd.ass03.ex1.view.game;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import pcd.ass03.ex1.actors.GuiUpdater;
import pcd.ass03.ex1.actors.msg.Start;
import pcd.ass03.ex1.domain.Board;

import java.net.URL;
import java.util.ResourceBundle;

import static pcd.ass03.ex1.actors.msg.Pause.Pause;
import static pcd.ass03.ex1.actors.msg.Resume.Resume;
import static pcd.ass03.ex1.actors.msg.Stop.Stop;
import static pcd.ass03.ex1.view.factories.FxWindowFactory.*;
import static pcd.ass03.ex1.view.settings.SettingsPresenter.getBoardConfiguration;
import static pcd.ass03.ex1.view.settings.SettingsPresenter.getWorkersNumber;

public class GamePresenter implements Initializable {

    private static final String PLAY_ICON_PATH = "/ex1/play.png";
    private static final String STOP_ICON_PATH = "/ex1/stop.png";
    private static final String PAUSE_ICON_PATH = "/ex1/pause.png";

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

    private Board board;

    private ActorRef guiUpdater;

    private boolean isNotPaused;

    @FXML
    void playOrResume(ActionEvent event) {
        if (guiUpdater == null) {
            launchGUIUpdater(event);
        } else if (isPaused()) {
            resumeGame();
            guiUpdater.tell(Resume, ActorRef.noSender());
        } else if (!isPaused()) {
            pauseGame();
            guiUpdater.tell(Pause, ActorRef.noSender());
        }
        switchButtonGraphic();
    }

    private void switchButtonGraphic() {
        if (isPaused()) {
            defaultInstance().buildGameButton(PLAY_ICON_PATH, buttonStart,
                    BTN_START_HEIGHT, BTN_START_WIDTH);
        } else if (!isPaused()) {
            defaultInstance().buildGameButton(PAUSE_ICON_PATH, buttonStart,
                    BTN_PAUSE_HEIGHT, BTN_PAUSE_WIDTH);
        }
    }

    private void launchGUIUpdater(ActionEvent event) {
        if (guiUpdater == null) {
            Canvas boardView = (Canvas) getStage(event).getScene().lookup("#" + getBoardPanelId());

            ActorSystem system = ActorSystem.create("MySystem");
            guiUpdater = system.actorOf(GuiUpdater.props(boardView, getWorkersNumber()));
        }
        guiUpdater.tell(new Start(board), ActorRef.noSender());
    }

    @FXML
    void stop(ActionEvent event) {
        if (guiUpdater == null) {
            showDialog("Game isn't started", "Please start the game before press STOP", Alert.AlertType.ERROR);
        } else {
            guiUpdater.tell(Stop, ActorRef.noSender());
            buttonStart.setDisable(true);
            buttonStop.setDisable(true);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        isNotPaused = true;
        buildGameBoard();
        defaultInstance().buildGameButton(PLAY_ICON_PATH, buttonStart, BTN_START_HEIGHT, BTN_START_WIDTH);
        defaultInstance().buildGameButton(STOP_ICON_PATH, buttonStop, BTN_STOP_HEIGHT, BTN_STOP_WIDTH);
    }

    private void buildGameBoard() {
        board = getBoardConfiguration();
    }

    public boolean isPaused() {
        return !this.isNotPaused;
    }

    public void pauseGame() {
        isNotPaused = false;
    }

    public void resumeGame() {
        isNotPaused = true;
    }

}
