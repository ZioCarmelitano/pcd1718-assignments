package pcd.ass01.view.utils;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;


public final class ScrollManager {

    private static Double scrollHorizontalNewValue;
    private static Double scrollVerticalNewValue;
    private static Double scrollVerticalOldValue;
    private static Double scrollHorizontalOldValue;

    private static Integer startY = 0;
    private static Integer endY ;
    private static Integer startX = 0;
    private static Integer endX;

    public static int getStartY(){
        return startY;
    }

    public static int getEndY(){
        return endY;
    }

    public static int getStartX(){
        return startX;
    }

    public static int getEndX(){
        return endX;
    }

    public static void setScrollHorizontalProperty(Canvas gameBoardPanel, ScrollPane scrollPane) {
        endX = (int) gameBoardPanel.getWidth();
        scrollPane.hvalueProperty()
                .multiply(gameBoardPanel.getHeight())
                .addListener(((observable, oldValue, newValue) ->
                        updateHorizontalValues(gameBoardPanel, oldValue, newValue)));
    }

    private static void updateHorizontalValues(Canvas gameBoardPanel, Number oldValue, Number newValue) {
        scrollHorizontalOldValue = oldValue.doubleValue();
        scrollHorizontalNewValue = newValue.doubleValue();
        endX = isBoardWidthGreater(gameBoardPanel)
                ? (int) (endX + getScrollHorizontalTranslation())
                : (int) gameBoardPanel.getWidth();
        startX = isBoardWidthGreater(gameBoardPanel)
                ? (int) (startX + getScrollHorizontalTranslation())
                : 0;
    }

    public static void setScrollVerticalProperty(Canvas gameBoardPanel, ScrollPane scrollPane) {
        endY = (int) gameBoardPanel.getHeight();
        scrollPane.vvalueProperty()
                .multiply(gameBoardPanel.getWidth())
                .addListener((observable, oldValue, newValue) ->
                        updateVerticalValues(gameBoardPanel, oldValue, newValue));
    }

    private static void updateVerticalValues(Canvas gameBoardPanel, Number oldValue, Number newValue) {
        scrollVerticalOldValue = oldValue.doubleValue();
        scrollVerticalNewValue = newValue.doubleValue();
        endY = isBoardHeightGreater(gameBoardPanel)
                ? (int) (endY + getScrollVerticalTranslation())
                : (int) gameBoardPanel.getHeight();
        startY = isBoardHeightGreater(gameBoardPanel)
                ? (int) (startY + getScrollVerticalTranslation()) : 0;
    }

    private static double getScrollHorizontalTranslation() {
        return scrollHorizontalNewValue - scrollHorizontalOldValue;
    }

    private static double getScrollVerticalTranslation() {
        return scrollVerticalNewValue - scrollVerticalOldValue;
    }

    private static boolean isBoardHeightGreater(Canvas canvas) {
        return canvas.getHeight() > getPanelScene(canvas).getHeight();
    }

    private static boolean isBoardWidthGreater(Canvas canvas) {
        return canvas.getWidth() > getPanelScene(canvas).getWidth();
    }

    private static Scene getPanelScene(Node component){
        return component.getScene();
    }
}
