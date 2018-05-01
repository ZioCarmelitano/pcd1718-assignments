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
import pcd.ass02.ex2.verticles.codecs.DocumentMessageCodec;
import pcd.ass02.ex2.verticles.codecs.FolderMessageCodec;
import pcd.ass02.ex2.verticles.codecs.SearchResultMessageCodec;
import pcd.ass02.ex2.verticles.codecs.SearchStatisticsMessageCodec;

import java.io.File;
import java.util.List;

final class Launcher {

    private static final int INSTANCES = 10;
    private static final int EVENT_LOOP_POOL_SIZE = 1;
    private static final int WORKER_POOL_SIZE = 2 * INSTANCES;

    private static final Vertx vertx;

    private static int filesWithOccurrencesCount;

    public static void main(String... args) {
        final File path = new File(args[0]);
        final String regex = args[1];
        final int maxDepth = Integer.parseInt(args[2]);

        final Folder rootFolder = Folder.fromDirectory(path, maxDepth);

        final EventBus eventBus = vertx.eventBus();
        eventBus.registerDefaultCodec(Document.class, DocumentMessageCodec.getInstance());
        eventBus.registerDefaultCodec(Folder.class, FolderMessageCodec.getInstance());
        eventBus.registerDefaultCodec(SearchResult.class, SearchResultMessageCodec.getInstance());
        eventBus.registerDefaultCodec(SearchStatistics.class, SearchStatisticsMessageCodec.getInstance());

        final DeploymentOptions options = new DeploymentOptions()
                .setWorker(true)
                .setInstances(INSTANCES);
        vertx.deployVerticle(FolderSearchVerticle::new, options);
        vertx.deployVerticle(() -> new DocumentSearchVerticle(regex), options);
        vertx.deployVerticle(new SearchResultAccumulatorVerticle(Launcher::handleResult, Launcher::handleCompletion));

        eventBus.send("folderSearch", rootFolder);
    }

    private static void handleResult(SearchStatistics statistics) {
        final List<String> files = statistics.getDocumentNames();
        final double averageMatches = statistics.getAverageMatches();
        final double matchingRate = statistics.getMatchingRate();

        if (files.size() > filesWithOccurrencesCount) {
            filesWithOccurrencesCount = files.size();
            System.out.println(files);
            System.out.println("Matching rate: " + matchingRate);
            System.out.println("Average: " + averageMatches);
            System.out.println("Files with occurrences: " + files.size());
        }
    }

    private static void handleCompletion(long totalOccurrences) {
        System.out.println("Total occurrences: " + totalOccurrences);
        vertx.close();
    }

    static {
        vertx = Vertx.vertx(new VertxOptions()
                .setWorkerPoolSize(WORKER_POOL_SIZE)
                .setEventLoopPoolSize(EVENT_LOOP_POOL_SIZE));
    }

    private Launcher() {
    }

}
