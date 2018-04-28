package pcd.ass02.ex2;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import pcd.ass02.domain.Document;
import pcd.ass02.domain.Folder;
import pcd.ass02.domain.SearchResult;
import pcd.ass02.domain.SearchStatistics;
import pcd.ass02.ex2.verticles.DocumentSearchVerticle;
import pcd.ass02.ex2.verticles.FolderSearchVerticle;
import pcd.ass02.ex2.verticles.SearchResultAccumulatorVerticle;
import pcd.ass02.ex2.verticles.codec.DocumentCodec;
import pcd.ass02.ex2.verticles.codec.FolderCodec;
import pcd.ass02.ex2.verticles.codec.SearchResultCodec;
import pcd.ass02.ex2.verticles.codec.SearchStatisticsCodec;

import java.io.File;
import java.util.List;

import static pcd.ass02.domain.Folder.fromDirectory;

final class Launcher {

    private static int fileWithOccurrencesCount;

    public static void main(String... args) {
        File path = new File(args[0]);
        String regex = args[1];
        int maxDepth = Integer.parseInt(args[2]);

        final Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(10));
        final EventBus eventBus = vertx.eventBus();

        eventBus.registerDefaultCodec(Document.class, DocumentCodec.getInstance());
        eventBus.registerDefaultCodec(Folder.class, FolderCodec.getInstance());
        eventBus.registerDefaultCodec(SearchResult.class, SearchResultCodec.getInstance());
        eventBus.registerDefaultCodec(SearchStatistics.class, SearchStatisticsCodec.getInstance());

        DeploymentOptions options = new DeploymentOptions()
                .setWorker(true)
                .setInstances(10);
        vertx.deployVerticle(FolderSearchVerticle::new, options);
        vertx.deployVerticle(() -> new DocumentSearchVerticle(regex), options);
        vertx.deployVerticle(new SearchResultAccumulatorVerticle(Launcher::handle));

        vertx.eventBus().send("folderSearch", fromDirectory(path, maxDepth));
    }

    private static void handle(SearchStatistics statistics) {
        List<String> files = statistics.getDocumentNames();
        double averageMatches = statistics.getAverageMatches();
        double matchingRate = statistics.getMatchingRate();

        if (files.size() > fileWithOccurrencesCount) {
            fileWithOccurrencesCount = files.size();
            System.out.println(files);
            System.out.println("Matching rate: " + matchingRate);
            System.out.println("Average: " + averageMatches);
            System.out.println("Files with occurrences: " + files.size());
        }
    }

    private Launcher() {
    }

}
