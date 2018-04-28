package pcd.ass02.ex2;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import pcd.ass02.domain.SearchStatistics;
import pcd.ass02.ex2.verticles.FolderSearchVerticle;
import pcd.ass02.ex2.verticles.SearchResultAccumulatorVerticle;

import java.io.File;
import java.util.List;

import static pcd.ass02.domain.Folder.fromDirectory;

final class Launcher {

    private static int fileWithOccurrencesCount;

    public static void main(String... args) {
        File path = new File(args[0]);
        String regex = args[1];
        int maxDepth = Integer.parseInt(args[2]);

        Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(10));
        vertx.deployVerticle(new FolderSearchVerticle(fromDirectory(path, maxDepth), regex),
                new DeploymentOptions().setWorker(true));
        vertx.deployVerticle(new SearchResultAccumulatorVerticle(Launcher::handle));
    }

    private static void handle(SearchStatistics statistics) {
        List<String> files = statistics.getMatches();
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
