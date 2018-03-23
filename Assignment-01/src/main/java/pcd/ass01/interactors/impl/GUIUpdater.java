package pcd.ass01.interactors.impl;

import javafx.scene.canvas.Canvas;
import pcd.ass01.domain.Board;
import pcd.ass01.interactors.BoardUpdater;
import pcd.ass01.util.time.SystemClock;

import java.util.concurrent.atomic.AtomicBoolean;

import static pcd.ass01.view.factories.FxWindowFactory.drawBoard;

public class GUIUpdater extends Thread{

    private Board board;

    private BoardUpdater boardUpdater;

    private Canvas boardView;

    private AtomicBoolean isRunning;

    public GUIUpdater(Board board, BoardUpdater boardUpdater, Canvas boardView) {
        this.board = board;
        this.boardUpdater = boardUpdater;
        this.boardView = boardView;
        this.isRunning = new AtomicBoolean(true);
    }

    @Override
    public void run() {
        // Game loop
        while(true){
            if(isRunning.get()) {
                board = boardUpdater.update(board);
                drawBoard(boardView, board);
                SystemClock.sleep(100);
            }
        }

    }

    public void stopGame() {
        isRunning.set(false);
        boardUpdater.stop();
        this.interrupt();
    }
}
