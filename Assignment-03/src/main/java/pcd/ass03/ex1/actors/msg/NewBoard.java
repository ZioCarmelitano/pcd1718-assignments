package pcd.ass03.ex1.actors.msg;

import pcd.ass03.ex1.domain.Board;

import java.io.Serializable;

public class NewBoard implements Serializable {
    private final Board newBoard;


    public NewBoard(Board newBoard) {
        this.newBoard = newBoard;
    }

    public Board getNewBoard() {
        return this.newBoard;
    }
}
