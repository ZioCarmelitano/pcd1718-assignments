package pcd.ass01.view.game;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.canvas.Canvas;
import pcd.ass01.domain.Board;
import pcd.ass01.interactors.BoardUpdater;
import pcd.ass01.util.time.SystemClock;

import java.util.concurrent.atomic.AtomicBoolean;

import static pcd.ass01.view.factories.FxWindowFactory.drawBoard;
import static pcd.ass01.view.factories.FxWindowFactory.getStage;

public class GuiUpdater extends Task<Void> {

    private Board board;

    private final BoardUpdater boardUpdater;

    private final Canvas boardView;

    private final AtomicBoolean isRunning;
    private final AtomicBoolean isNotPaused;

    public GuiUpdater(Board board, BoardUpdater boardUpdater, Canvas boardView) {
        this.board = board;
        this.boardUpdater = boardUpdater;
        this.boardView = boardView;
        this.isRunning = new AtomicBoolean(true);
        this.isNotPaused = new AtomicBoolean(true);
        getStage(boardView).setOnCloseRequest(event -> stopGame());
    }

    public boolean stopGame() {
        if(isRunning.get()) {
            isRunning.set(false);
            boardUpdater.stop();
            return true;
        }
        return false;
    }

    public boolean isUpdating(){
        return this.isRunning.get();
    }

    public boolean isPaused(){
        return !this.isNotPaused.get();
    }

    public void pauseGame() {
        isNotPaused.set(false);
    }

    public void resumeGame(){
        isNotPaused.set(true);
    }

    @Override
    protected Void call() {
        // Game loop
        while(isRunning.get()){
            if (isNotPaused.get()) {
                board = boardUpdater.update(board);
                Platform.runLater(() -> drawBoard(boardView, board));
            }
            SystemClock.sleep(200);
        }
        return null;
    }
}
