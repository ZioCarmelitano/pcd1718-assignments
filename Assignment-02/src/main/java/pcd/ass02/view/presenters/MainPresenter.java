package pcd.ass02.view.presenters;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import org.reactivestreams.Subscription;
import pcd.ass02.domain.Folder;
import pcd.ass02.domain.SearchStatistics;
import pcd.ass02.ex3.RxJavaOccurrencesCounter;
import pcd.ass02.ex3.SearchResultSubscriber;
import pcd.ass02.interactors.OccurrencesCounter;
import pcd.ass02.view.datamodel.DocumentResult;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class MainPresenter implements Initializable {

    @FXML
    private TextField path;

    @FXML
    private TextField regex;

    @FXML
    private Spinner<Integer> maxDepthField;

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
        final Integer maxDepth = maxDepthField.getValue();
        new Thread(new Task<Void>() {
            @Override
            protected Void call(){
                performSearch(rootFolder, regularExp, maxDepth);
                return null;
            }
        }).start();
    }

    private void performSearch(String path, String regularExp, Integer maxDepth) {
        final OccurrencesCounter counter = new RxJavaOccurrencesCounter(new SearchResultSubscriber() {
            private long startTime;
            private long filesWithOccurrencesCount;

            @Override
            protected void onNext(SearchStatistics statistics) {
                final List<String> documentNames = statistics.getDocumentNames();
                final double averageMatches = statistics.getAverageMatches();
                final double matchingRate = statistics.getMatchingRate();

                if (documentNames.size() > filesWithOccurrencesCount) {
                    filesWithOccurrencesCount = documentNames.size();
                    updateTable(documentNames);
                    updateStatisticsField(averageMatches, matchingRate);
                }
            }

            @Override
            protected void onComplete(long totalOccurrences) {
                final long endTime = System.currentTimeMillis();
                System.out.println();
                System.out.println("Total occurrences: " + totalOccurrences);
                System.out.println("Execution time: " + (endTime - startTime) + "ms");
            }

            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
                startTime = System.currentTimeMillis();
            }
        });

        final Folder rootFolder = Folder.fromDirectory(new File(path), maxDepth);
        counter.start();
        counter.countOccurrences(rootFolder, regularExp);
        counter.stop();
    }

    private void updateStatisticsField(double averageMatches, double matchingRate) {
        Platform.runLater(() -> {
            txtAverageMatching.setText(String.valueOf(averageMatches));
            txtMatchingRate.setText(String.valueOf(matchingRate));
        });
    }

    //TODO
    private void updateTable(List<String> documentNames) {
        Platform.runLater(() -> tableItems.add(new DocumentResult("file.txt", 0)));
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
