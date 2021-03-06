package pcd.ass01.interactors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pcd.ass01.domain.Board;

import static org.junit.Assert.assertEquals;
import static pcd.ass01.domain.Cell.ALIVE;

public class BoardUpdaterTest {

    private static final int WIDTH = 5;
    private static final int HEIGHT = 8;

    private Board board;
    private BoardUpdater updater;

    @Before
    public void setUp() {
        board = Board.board(WIDTH, HEIGHT);
        updater = BoardUpdater.create(2);
        updater.start();
    }

    @After
    public void tearDown() {
        updater.stop();
    }

    @Test
    public void arisesWith3() {
        board.setCell(0, 1, ALIVE);
        board.setCell(1, 0, ALIVE);
        board.setCell(1, 1, ALIVE);

        final Board expected = Board.board(WIDTH, HEIGHT);
        expected.setCell(0, 0, ALIVE);
        expected.setCell(0, 1, ALIVE);
        expected.setCell(1, 0, ALIVE);
        expected.setCell(1, 1, ALIVE);

        assertEquals(expected, updater.update(board));
    }

    @Test
    public void survivesWith2() {
        board.setCell(0, 0, ALIVE);
        board.setCell(0, 1, ALIVE);
        board.setCell(1, 0, ALIVE);

        final Board expected = Board.board(WIDTH, HEIGHT);
        expected.setCell(0, 0, ALIVE);
        expected.setCell(0, 1, ALIVE);
        expected.setCell(1, 0, ALIVE);
        expected.setCell(1, 1, ALIVE);

        assertEquals(expected, updater.update(board));
    }

    @Test
    public void survivesWith3() {
        board.setCell(0, 0, ALIVE);
        board.setCell(0, 1, ALIVE);
        board.setCell(1, 0, ALIVE);
        board.setCell(1, 1, ALIVE);

        final Board expected = Board.board(WIDTH, HEIGHT);
        expected.setCell(0, 0, ALIVE);
        expected.setCell(0, 1, ALIVE);
        expected.setCell(1, 0, ALIVE);
        expected.setCell(1, 1, ALIVE);

        assertEquals(expected, updater.update(board));
    }

    @Test
    public void diesWithLessThan2() {
        board.setCell(0, 0, ALIVE);
        board.setCell(0, 1, ALIVE);

        final Board expected = Board.board(WIDTH, HEIGHT);

        assertEquals(expected, updater.update(board));
    }

    @Test
    public void diesWithMoreThan3() {
        board.setCell(0, 0, ALIVE);
        board.setCell(0, 1, ALIVE);
        board.setCell(0, 2, ALIVE);
        board.setCell(1, 0, ALIVE);

        final Board result = Board.board(WIDTH, HEIGHT);
        result.setCell(0, 0, ALIVE);
        result.setCell(0, 1, ALIVE);
        result.setCell(1, 0, ALIVE);

        assertEquals(result, updater.update(board));
    }

}
