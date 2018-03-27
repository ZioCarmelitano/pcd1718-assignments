package pcd.ass01.view.properties;

import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;
import pcd.ass01.domain.Board;

public final class ViewProperties {

    private static Double scrollHorizontalNewValue;
    private static Double scrollVerticalNewValue;
    private static Double scrollVerticalOldValue;
    private static Double scrollHorizontalOldValue;

    private static Integer startY;
    private static Integer endY;
    private static Integer startX;
    private static Integer endX;

    private static double getScrollHorizontalTranslation() {
        return scrollHorizontalNewValue - scrollHorizontalOldValue;
    }

    private static double getScrollVerticalTranslation() {
        return scrollVerticalNewValue - scrollVerticalOldValue;
    }

    public static void setScrollHorizontalProperty(Canvas gameBoardPanel, ScrollPane scrollPane) {
        scrollPane.hvalueProperty()
                .multiply(gameBoardPanel.getHeight())
                .addListener(((observable, oldValue, newValue) -> {
                    scrollHorizontalOldValue = oldValue.doubleValue();
                    scrollHorizontalNewValue = newValue.doubleValue();
                    //endX = isBoardWidthGreater(canvas, board)
                    //        ? (int) (endX + getScrollHorizontalTranslation()) : boardWidth;
                    //startX = isBoardWidthGreater(canvas, board)
                    //        ? (int) (startX + getScrollHorizontalTranslation()) : 0;
                }));
    }

    public static void setScrollVerticalProperty(Canvas gameBoardPanel, ScrollPane scrollPane) {
        scrollPane.vvalueProperty()
                .multiply(gameBoardPanel.getWidth())
                .addListener((observable, oldValue, newValue) -> {
                    scrollVerticalOldValue = oldValue.doubleValue();
                    scrollVerticalNewValue = newValue.doubleValue();
                    //endY = isBoardHeightGreater(gameBoardPanel, board)
                    //        ? (int) (endY + getScrollVerticalTranslation()) : boardHeight;
                    //startY = isBoardHeightGreater(gameBoardPanel, board)
                    //        ? (int) (startY + getScrollVerticalTranslation()) : 0;
                });
    }

    public static int getStartY(){
        return 0;
    }

    private static boolean isBoardHeightGreater(Canvas canvas, Board board) {
        return board.getHeight() > canvas.getHeight();
    }

    private static boolean isBoardWidthGreater(Canvas canvas, Board board) {
        return board.getWidth() > canvas.getWidth();
    }
}
