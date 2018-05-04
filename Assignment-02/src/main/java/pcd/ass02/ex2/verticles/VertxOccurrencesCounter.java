package pcd.ass02.ex2.verticles;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import pcd.ass02.domain.*;
import pcd.ass02.ex2.verticles.codecs.DocumentMessageCodec;
import pcd.ass02.ex2.verticles.codecs.FolderMessageCodec;
import pcd.ass02.ex2.verticles.codecs.SearchResultMessageCodec;
import pcd.ass02.ex2.verticles.codecs.SearchStatisticsMessageCodec;
import pcd.ass02.interactors.AbstractOccurrencesCounter;

import java.util.concurrent.Semaphore;
import java.util.stream.Stream;

public class VertxOccurrencesCounter extends AbstractOccurrencesCounter {

    private static final int EVENT_LOOP_POOL_SIZE = 2;
    private static final int DEFAULT_INSTANCES = VertxOptions.DEFAULT_WORKER_POOL_SIZE / 2;

    private final EventBus eventBus;
    private final Semaphore semaphore;
    private final Vertx vertx;

    public VertxOccurrencesCounter(Handler<? super SearchStatistics> resultHandler) {
        this(new SearchResultAccumulator(), DEFAULT_INSTANCES, resultHandler);
    }

    public VertxOccurrencesCounter(int instances, Handler<? super SearchStatistics> resultHandler) {
        this(new SearchResultAccumulator(), instances, resultHandler);
    }

    private VertxOccurrencesCounter(SearchResultAccumulator accumulator, int instances, Handler<? super SearchStatistics> resultHandler) {
        super(accumulator);

        final int workerPoolSize = 2 * instances;

        this.vertx = Vertx.vertx(new VertxOptions()
                .setWorkerPoolSize(workerPoolSize)
                .setEventLoopPoolSize(EVENT_LOOP_POOL_SIZE));

        eventBus = vertx.eventBus();
        eventBus.registerDefaultCodec(Document.class, DocumentMessageCodec.getInstance());
        eventBus.registerDefaultCodec(Folder.class, FolderMessageCodec.getInstance());
        eventBus.registerDefaultCodec(SearchResult.class, SearchResultMessageCodec.getInstance());
        eventBus.registerDefaultCodec(SearchStatistics.class, SearchStatisticsMessageCodec.getInstance());

        final DeploymentOptions options = new DeploymentOptions()
                .setWorker(true)
                .setInstances(instances);
        vertx.deployVerticle(FolderSearchVerticle::new, options);
        vertx.deployVerticle(DocumentSearchVerticle::new, options);
        vertx.deployVerticle(new SearchResultAccumulatorVerticle(accumulator, resultHandler));
        vertx.deployVerticle(new SearchCoordinatorVerticle());

        semaphore = new Semaphore(0);
        eventBus.consumer(C.coordinator.done,m -> semaphore.release());
    }

    @Override
    protected void onStop() {
        vertx.close();
    }

    @Override
    protected long doCount(Folder rootFolder, String regex) {
        final long documentCount = getDocuments(rootFolder).count();
        eventBus.publish(C.coordinator.documentCount, documentCount);
        eventBus.publish(C.documentSearch.regex, regex);
        eventBus.send(C.folderSearch, rootFolder);

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
