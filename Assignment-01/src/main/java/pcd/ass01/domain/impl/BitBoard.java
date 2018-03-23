package pcd.ass01.domain.impl;

import pcd.ass01.collection.BitMatrix;
import pcd.ass01.domain.Board;
import pcd.ass01.domain.Cell;

import static pcd.ass01.domain.Cell.*;

public class BitBoard implements Board {

    private BitMatrix board;

    public BitBoard(int width, int height){
        board = new BitMatrix(height, width);
    }

    @Override
    public int getHeight() {
        return board.getRowsNumber();
    }

    @Override
    public int getWidth() {
        return board.getColumnsNumber();
    }

    @Override
    public Cell getCell(int x, int y) {
        return board.get(x, y) ? ALIVE : DEAD;
    }

    @Override
    public void setCell(int x, int y, Cell cell) {
        board.set(x, y, cell == ALIVE);
    }
}
