package pcd.ass01.view.game;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.canvas.Canvas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pcd.ass01.domain.Board;
import pcd.ass01.interactors.BoardUpdater;
import pcd.ass01.util.time.SystemClock;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import static pcd.ass01.view.factories.FxWindowFactory.*;
import static pcd.ass01.view.game.RenderingService.renderBoard;

class GameUpdater extends Task<Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final int UPDATE_INTERVAL = 50;

    private Board board;

    private final BoardUpdater boardUpdater;

    private final Canvas boardView;

    private final Semaphore semaphore;

    private final AtomicBoolean isRunning;
    private final AtomicBoolean isNotPaused;

    public GameUpdater(Board board, BoardUpdater boardUpdater, Canvas boardView) {
        this.board = board;
        this.boardUpdater = boardUpdater;
        this.boardView = boardView;
        this.isRunning = new AtomicBoolean(true);
        this.isNotPaused = new AtomicBoolean(true);
        this.semaphore = new Semaphore(0);
        handleGameClosing(boardView);
    }

    private void handleGameClosing(Canvas boardView) {
        getStage(boardView).setOnCloseRequest(event -> {
            stopGame();
            try {
                defaultInstance().openStartWindow();
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        });
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
        semaphore.release();
    }

    @Override
    protected Void call() {
        // Game loop
        while(isRunning.get()){
            if (isNotPaused.get()) {
                board = boardUpdater.update(board);
                renderBoard(boardView, board);
            }else{
                semaphore.acquireUninterruptibly();
            }
            SystemClock.sleep(UPDATE_INTERVAL);
        }
        return null;
    }

}
