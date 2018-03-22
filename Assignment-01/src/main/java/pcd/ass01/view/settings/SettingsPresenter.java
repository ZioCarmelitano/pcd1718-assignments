package pcd.ass01.view.settings;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import pcd.ass01.view.factories.FxWindowFactory;
import pcd.ass01.view.factories.WindowFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsPresenter implements Initializable{

    @FXML
    private TextField txtWidth;

    @FXML
    private TextField txtHeight;

    @FXML
    private TextField txtNWorkers;

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
        if(widthInserted.isEmpty() || heightInserted.isEmpty() || workersNumberInserted.isEmpty()){
            FxWindowFactory.showDialog("Missing fields", "Please fill out all fields!",
                    Alert.AlertType.ERROR);
            return;
        }
        int width = Integer.parseInt(txtWidth.getText());
        int height = Integer.parseInt(txtHeight.getText());
        int workersNumber = Integer.parseInt(txtNWorkers.getText());
        closeSettingWindow(event);
        WindowFactory windowFactory = FxWindowFactory.defaultInstance();
        windowFactory.openGameWindow(width, height);
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
}
