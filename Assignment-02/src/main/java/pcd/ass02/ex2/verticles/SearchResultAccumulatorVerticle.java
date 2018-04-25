package pcd.ass02.ex2.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import pcd.ass02.domain.SearchResultStatistics;

import java.util.ArrayList;
import java.util.List;

public class SearchResultAccumulatorVerticle extends AbstractVerticle {

    private final Handler<SearchResultStatistics> handler;

    private long fileCount;
    private long fileWithOccurrences;
    private long totalOccurrences;
    private double averageMatches;
    private final List<String> files;

    private long timerID;

    public SearchResultAccumulatorVerticle(Handler<SearchResultStatistics> handler) {
        this.handler = handler;
        files = new ArrayList<>();
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().<JsonObject>consumer("accumulator",
                (message) -> onMessage(message.body()));
        timerID = vertx.setTimer(2000, completionHandler());
    }

    private void onMessage(JsonObject message){
        vertx.cancelTimer(timerID);

        long occurrences = message.getLong("occurrences");
        String documentName = message.getString("documentName");

        fileCount++;
        if (occurrences > 0) {
            files.add(documentName);
            fileWithOccurrences++;
            totalOccurrences += occurrences;
            averageMatches = ((double) totalOccurrences) / ((double) fileWithOccurrences);
        }
        final double matchingRate = ((double) fileWithOccurrences) / ((double) fileCount);

        SearchResultStatistics statistics = new SearchResultStatistics(files, matchingRate, averageMatches);
        handler.handle(statistics);

        timerID = vertx.setTimer(2000, completionHandler());
    }

    private Handler<Long> completionHandler() {
        return (event) -> {
            System.out.println("Total occurrences:" + totalOccurrences);
            vertx.close();
        };
    }

}
