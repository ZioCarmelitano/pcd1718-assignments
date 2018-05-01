package pcd.ass02.ex2;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
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
import pcd.ass02.interactors.OccurrencesCounter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class  VertxOccurrencesCounter implements OccurrencesCounter {

    private static final int INSTANCES = 10;
    private static final int EVENT_LOOP_POOL_SIZE = 1;
    private static final int WORKER_POOL_SIZE = 2 * INSTANCES;

    private final Vertx vertx;
    private final EventBus eventBus;
    private final Handler<? super SearchStatistics> resultHandler;
    private final Handler<? super Long> completionHandler;

    public VertxOccurrencesCounter(Handler<? super SearchStatistics> resultHandler, Handler<? super Long> completionHandler) {
        this.resultHandler = resultHandler;
        this.completionHandler = completionHandler;
        vertx = Vertx.vertx(new VertxOptions()
                .setWorkerPoolSize(WORKER_POOL_SIZE)
                .setEventLoopPoolSize(EVENT_LOOP_POOL_SIZE));
        eventBus = vertx.eventBus();
    }

    @Override
    public void start() {
        eventBus.registerDefaultCodec(Document.class, DocumentMessageCodec.getInstance());
        eventBus.registerDefaultCodec(Folder.class, FolderMessageCodec.getInstance());
        eventBus.registerDefaultCodec(SearchResult.class, SearchResultMessageCodec.getInstance());
        eventBus.registerDefaultCodec(SearchStatistics.class, SearchStatisticsMessageCodec.getInstance());
    }

    @Override
    public void stop() {
        vertx.close();
    }

    @Override
    public long countOccurrences(Folder rootFolder, String regex) {
        final DeploymentOptions options = new DeploymentOptions()
                .setWorker(true)
                .setInstances(INSTANCES);
        vertx.deployVerticle(FolderSearchVerticle::new, options);
        vertx.deployVerticle(() -> new DocumentSearchVerticle(regex), options);

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicLong result = new AtomicLong();
        vertx.deployVerticle(new SearchResultAccumulatorVerticle(resultHandler, totalOccurrences -> {
            completionHandler.handle(totalOccurrences);
            result.set(totalOccurrences);
            latch.countDown();
        }));

        eventBus.send("folderSearch", rootFolder);
        try {
            latch.await();
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
        return result.get();
    }

}
