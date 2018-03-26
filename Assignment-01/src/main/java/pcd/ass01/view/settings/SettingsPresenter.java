package pcd.ass01.view.settings;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import pcd.ass01.domain.Board;
import pcd.ass01.view.factories.FxWindowFactory;
import pcd.ass01.view.factories.WindowFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static pcd.ass01.domain.Boards.gosperGliderGun;
import static pcd.ass01.domain.Boards.randomBoard;

public class SettingsPresenter implements Initializable{

    @FXML
    private TextField txtWidth;

    @FXML
    private TextField txtHeight;

    @FXML
    private TextField txtNWorkers;

    private static int width;
    private static int height;
    private static int workersNumber;

    private static Board boardConfiguration;

    private void addChangeListener(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                textField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    @FXML
    void startGame(ActionEvent event) throws IOException {
        String widthInserted = txtWidth.getText();
        String heightInserted = txtHeight.getText();
        String workersNumberInserted = txtNWorkers.getText();
        if (inputIsIncorrect(widthInserted, heightInserted, workersNumberInserted)) return;
        width = Integer.parseInt(widthInserted);
        height = Integer.parseInt(heightInserted);
        workersNumber = Integer.parseInt(workersNumberInserted);
        boardConfiguration = randomBoard(width, height);
        closeSettingWindow(event);
        WindowFactory windowFactory = FxWindowFactory.defaultInstance();
        windowFactory.openGameWindow(width, height, boardConfiguration);
    }

    private boolean inputIsIncorrect(String widthInserted, String heightInserted, String workersNumberInserted) {
        if(widthInserted.isEmpty() || heightInserted.isEmpty() || workersNumberInserted.isEmpty()){
            FxWindowFactory.showDialog("Missing fields", "Please fill out all fields!",
                    Alert.AlertType.ERROR);
            return true;
        }
        return false;
    }

    private void closeSettingWindow(ActionEvent event) {
        Button playButton = (Button) event.getSource();
        Stage stage = (Stage) playButton.getScene().getWindow();
        stage.close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addChangeListener(txtWidth);
        addChangeListener(txtHeight);
        addChangeListener(txtNWorkers);
    }

    public static Board getBoardConfiguration(){
        return boardConfiguration;
    }

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }

    public static int getWorkersNumber() {
        return workersNumber;
    }
}
