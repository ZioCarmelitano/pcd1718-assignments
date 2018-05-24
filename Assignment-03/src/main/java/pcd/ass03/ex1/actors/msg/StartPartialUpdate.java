package pcd.ass03.ex1.actors.msg;

import pcd.ass03.ex1.domain.Board;

public class StartPartialUpdate extends StartUpdate {
    private final int maxToRow;

    public StartPartialUpdate(int fromRow, int toRow, Board oldBoard, Board newBoard, int maxToRow) {
        super(fromRow, toRow, oldBoard, newBoard);
        this.maxToRow = maxToRow;
    }

    public int getMaxToRow() {
        return maxToRow;
    }
}
