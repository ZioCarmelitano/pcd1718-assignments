package pcd.ass02.ex2;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import pcd.ass02.domain.*;
import pcd.ass02.ex2.verticles.DocumentSearchVerticle;
import pcd.ass02.ex2.verticles.FolderSearchVerticle;
import pcd.ass02.ex2.verticles.SearchCoordinatorVerticle;
import pcd.ass02.ex2.verticles.SearchResultAccumulatorVerticle;
import pcd.ass02.ex2.verticles.codecs.DocumentMessageCodec;
import pcd.ass02.ex2.verticles.codecs.FolderMessageCodec;
import pcd.ass02.ex2.verticles.codecs.SearchResultMessageCodec;
import pcd.ass02.ex2.verticles.codecs.SearchStatisticsMessageCodec;
import pcd.ass02.interactors.AbstractOccurrencesCounter;

import java.util.concurrent.Semaphore;
import java.util.stream.Stream;

public class VertxOccurrencesCounter extends AbstractOccurrencesCounter {

    private static final int INSTANCES = 10;
    private static final int EVENT_LOOP_POOL_SIZE = 2;
    private static final int WORKER_POOL_SIZE = 2 * INSTANCES;

    private final EventBus eventBus;
    private final Semaphore semaphore;
    private final Vertx vertx;

    public VertxOccurrencesCounter(Handler<? super SearchStatistics> resultHandler) {
        final SearchResultAccumulator accumulator = new SearchResultAccumulator();
        this.vertx = Vertx.vertx(new VertxOptions()
                .setWorkerPoolSize(WORKER_POOL_SIZE)
                .setEventLoopPoolSize(EVENT_LOOP_POOL_SIZE));

        setAccumulator(accumulator);

        eventBus = vertx.eventBus();

        eventBus.registerDefaultCodec(Document.class, DocumentMessageCodec.getInstance());
        eventBus.registerDefaultCodec(Folder.class, FolderMessageCodec.getInstance());
        eventBus.registerDefaultCodec(SearchResult.class, SearchResultMessageCodec.getInstance());
        eventBus.registerDefaultCodec(SearchStatistics.class, SearchStatisticsMessageCodec.getInstance());

        final DeploymentOptions options = new DeploymentOptions()
                .setWorker(true)
                .setInstances(INSTANCES);
        vertx.deployVerticle(FolderSearchVerticle::new, options);
        vertx.deployVerticle(DocumentSearchVerticle::new, options);

        semaphore = new Semaphore(0);
        vertx.deployVerticle(new SearchResultAccumulatorVerticle(accumulator, resultHandler));
        vertx.deployVerticle(new SearchCoordinatorVerticle());

        eventBus.consumer("coordinator.done", msg -> semaphore.release());
    }

    @Override
    protected void onStop() {
        vertx.close();
    }

    @Override
    protected long doCount(Folder rootFolder, String regex) {
        eventBus.publish("coordinator.documentCount", getDocuments(rootFolder).count());
        eventBus.publish("documentSearch.regex", regex);
        eventBus.send("folderSearch", rootFolder);

        semaphore.acquireUninterruptibly();

        return getTotalOccurrences();
    }

    private static Stream<Document> getDocuments(Folder folder) {
        return Stream.concat(
                folder.getDocuments().stream(),
                folder.getSubFolders().stream()
                        .flatMap(VertxOccurrencesCounter::getDocuments));
    }

}
