package pcd.ass02.view.presenters;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import pcd.ass02.view.datamodel.DocumentResult;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class MainPresenter implements Initializable {

    @FXML
    private TextField path;

    @FXML
    private TextField regex;

    @FXML
    private TextField maxDepthField;

    @FXML
    private TableView<DocumentResult> table;

    @FXML
    private TableColumn<DocumentResult, Integer> occurrencesColumn;

    @FXML
    private TableColumn<DocumentResult, String> documentNameColumn;

    @FXML
    private TextField txtMatchingRate;

    @FXML
    private TextField txtAverageMatching;

    private ObservableList<DocumentResult> tableItems = FXCollections.observableArrayList();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        table.setItems(tableItems);
        documentNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("documentName"));
        occurrencesColumn.setCellValueFactory(
                new PropertyValueFactory<>("occurrences"));
    }

    @FXML
    void search(){
        final String rootFolder = path.getText();
        final String regularExp = regex.getText();
        final String maxDepth = maxDepthField.getText();
        new Thread(new Task<Void>() {
            @Override
            protected Void call(){
                // TODO call the interactor...
                //tableItems.add(new DocumentResult("bau.txt", 50));
                //tableItems.add(new DocumentResult("asd.txt", 50));
                //tableItems.add(new DocumentResult("ggg.txt", 50));

                return null;
            }
        }).start();
    }

    @FXML
    void browse() {
        final DirectoryChooser directoryChooser =
                new DirectoryChooser();
        final File selectedDirectory =
                directoryChooser.showDialog(path.getScene().getWindow());
        if (selectedDirectory != null) {
            path.setText(selectedDirectory.getAbsolutePath());
        }
    }

}
