package pcd.ass03.ex1.actors.msg;

import pcd.ass03.ex1.domain.Board;

import java.io.Serializable;

public class StartUpdateMsg implements Serializable {
    private final Board oldBoard;
    private final Board newBoard;
    private final int fromRow;
    private final int toRow;

    public StartUpdateMsg(int fromRow, int toRow, Board oldBoard, Board newBoard) {
        this.oldBoard = oldBoard;
        this.fromRow = fromRow;
        this.toRow = toRow;
        this.newBoard = newBoard;
    }

    public Board getOldBoard() {
        return oldBoard;
    }

    public int getFromRow() {
        return fromRow;
    }

    public int getToRow() {
        return toRow;
    }

    public Board getNewBoard() {
        return newBoard;
    }
}
