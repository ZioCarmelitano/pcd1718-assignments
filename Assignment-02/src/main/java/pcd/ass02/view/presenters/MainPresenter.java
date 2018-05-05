package pcd.ass02.view.presenters;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import org.reactivestreams.Subscription;
import pcd.ass02.domain.Folder;
import pcd.ass02.domain.SearchStatistics;
import pcd.ass02.ex3.RxJavaOccurrencesCounter;
import pcd.ass02.ex3.SearchResultSubscriber;
import pcd.ass02.interactors.OccurrencesCounter;
import pcd.ass02.view.datamodel.DocumentResult;
import pcd.ass02.view.factories.FxWindowFactory;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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
    private TableColumn<DocumentResult, Long> occurrencesColumn;

    @FXML
    private TableColumn<DocumentResult, String> documentNameColumn;

    @FXML
    private Label matchingRate;

    @FXML
    private Label averageMatches;

    @FXML
    private Label totalOccurrences;

    @FXML
    private Button searchButton;

    private static ObservableList<DocumentResult> tableItems = FXCollections.observableArrayList();

    private OccurrencesCounter counter;

    /* Spinner options */
    private static final int MIN_DEPTH_SELECTABLE = 1;
    private static final int MAX_DEPTH_SELECTABLE = 1000;
    private static final int DEFAULT_MAX_DEPTH = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TableView init
        table.setItems(tableItems);
        documentNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("documentName"));
        occurrencesColumn.setCellValueFactory(
                new PropertyValueFactory<>("occurrences"));

        // Spinner Value factory setting
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_DEPTH_SELECTABLE,
                        MAX_DEPTH_SELECTABLE, DEFAULT_MAX_DEPTH);

        maxDepthField.setValueFactory(valueFactory);

        // Button graphic setting
        FxWindowFactory.setSearchButtonGraphic(searchButton);

        counter = new RxJavaOccurrencesCounter(new SearchResultSubscriber() {
            @Override
            protected void onNext(SearchStatistics statistics) {
                final Map<String, Long> documentResults = statistics.getDocumentResults();
                final double averageMatches = statistics.getAverageMatches();
                final double matchingRate = statistics.getMatchingRate();

                updateTable(documentResults);
                updateStatisticsField(averageMatches, matchingRate, documentResults.size());
            }

            @Override
            protected void onComplete(long totalOccurrences) {
                searchButton.setDisable(false);
            }

            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
            }
        });

        counter.start();
    }

    @FXML
    void search() {
        final String rootFolder = path.getText();
        final String regularExp = regex.getText();
        final Integer maxDepth = maxDepthField.getValue();
        searchButton.setDisable(true);
        new Thread(new Task<Void>() {
            @Override
            protected Void call() {
                performSearch(rootFolder, regularExp, maxDepth);
                return null;
            }
        }).start();

        //Debug
        System.out.println("Max Depth selected: " + maxDepth);
    }

    private void performSearch(String path, String regex, Integer maxDepth) {
        final Folder rootFolder = Folder.fromDirectory(new File(path), maxDepth);
        counter.countOccurrences(rootFolder, regex);
        counter.reset();
    }

    private void updateStatisticsField(double averageMatches, double matchingRate, int filesWithOccurrences) {
        Platform.runLater(() -> {
            this.averageMatches.setText(String.format("%.2f", averageMatches));
            this.matchingRate.setText(String.format("%.2f", matchingRate * 100) + "%");
            totalOccurrences.setText(String.valueOf(filesWithOccurrences));
        });
    }

    private void updateTable(Map<String, Long> documentResults) {
        tableItems.setAll(documentResults.keySet().stream()
                .map(doc -> new DocumentResult(doc, documentResults.get(doc)))
                .collect(Collectors.toList()));
    }

    @FXML
    void browse() {
        final DirectoryChooser chooser = new DirectoryChooser();
        final File selectedDirectory = chooser.showDialog(path.getScene().getWindow());
        if (selectedDirectory != null) {
            path.setText(selectedDirectory.getAbsolutePath());
        }
    }

}
