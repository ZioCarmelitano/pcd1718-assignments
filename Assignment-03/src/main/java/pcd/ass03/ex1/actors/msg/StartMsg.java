package pcd.ass03.ex1.actors.msg;

import pcd.ass03.ex1.domain.Board;

import java.io.Serializable;

public class StartMsg implements Serializable {
    private final Board board;

    public StartMsg(Board board) {
        this.board = board;
    }

    public Board getBoard() { return this.board; }
}
