package pcd.ass01.view.game;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.paint.Color;
import pcd.ass01.domain.Board;
import pcd.ass01.domain.Cell;

import java.nio.IntBuffer;

import static pcd.ass01.domain.CellUtils.throwUnknownCellState;
import static pcd.ass01.view.utils.ScrollManager.*;

public final class RenderingService {

    private static final WritablePixelFormat<IntBuffer> pixelFormat =
            PixelFormat.getIntArgbPreInstance();

    public static void renderBoard(final Canvas canvas, final Board board) {
        int boardWidth = board.getWidth();
        int boardHeight = board.getHeight();
        int[] boardBuffer = buildPixelBuffer(board, boardWidth, boardHeight);
        Platform.runLater(() -> writeBoardPixels(canvas, boardWidth,
                boardHeight, boardBuffer));
    }

    private static int[] buildPixelBuffer(Board board, int boardWidth, int boardHeight) {
        int[] boardBuffer = new int[boardWidth * boardHeight];
        for (int y = getStartX(); y < getEndX(); y++)
            for (int x = getStartY(); x < getEndY(); x++)
                boardBuffer[y + boardWidth * x] = colorToInt(getColor(board.getCell(x,y)));
        return boardBuffer;
    }

    private static void writeBoardPixels(Canvas canvas, int boardWidth,
                                         int boardHeight, int[] boardBuffer) {
        final PixelWriter pw = canvas.getGraphicsContext2D().getPixelWriter();
        pw.setPixels(0, 0, boardWidth, boardHeight, pixelFormat,
                boardBuffer, 0, boardWidth);
    }

    private static int colorToInt(Color c) {
        return (                       255  << 24) |
                ((int) (c.getRed()   * 255) << 16) |
                ((int) (c.getGreen() * 255) << 8)  |
                ((int) (c.getBlue()  * 255));
    }


    private static Color getColor(final Cell cell) {
        switch (cell) {
            case DEAD:
                return Color.WHITE;
            case ALIVE:
                return Color.BLACK;
            default:
                throw throwUnknownCellState(cell);
        }
    }

}
