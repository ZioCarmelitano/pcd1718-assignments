package pcd.ass03.ex1.view.game;

import javafx.concurrent.Task;
import javafx.scene.canvas.Canvas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pcd.ass03.ex1.domain.Board;
import pcd.ass03.ex1.interactors.BoardUpdater;
import pcd.ass03.ex1.util.time.SystemClock;
import pcd.ass03.ex1.view.factories.FxWindowFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

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
        FxWindowFactory.getStage(boardView).setOnCloseRequest(event -> {
            stopGame();
            try {
                FxWindowFactory.defaultInstance().openStartWindow();
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        });
    }

    public void stopGame() {
        if(isRunning.get()) {
            isRunning.set(false);
            boardUpdater.stop();
        }
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
                RenderingService.renderBoard(boardView, board);
            }else{
                semaphore.acquireUninterruptibly();
            }
            SystemClock.sleep(UPDATE_INTERVAL);
        }
        return null;
    }

}
