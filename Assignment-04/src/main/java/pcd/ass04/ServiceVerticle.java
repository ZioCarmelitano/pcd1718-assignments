package pcd.ass04;

import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;

import java.util.ArrayList;
import java.util.List;

public abstract class ServiceVerticle extends AbstractVerticle {

    protected EventBus eventBus;

    private ServiceDiscovery discovery;

    protected WebClient webAppClient;

    private final List<Record> records;

    protected ServiceVerticle() {
        records = new ArrayList<>();
    }

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

        discovery = ServiceDiscovery.create(this.vertx);
        eventBus = this.vertx.eventBus();
    }

    @Override
    public void stop() {
        records.forEach(record -> discovery.unpublish(record.getRegistration(), ar -> {
            if (ar.succeeded()) {
                records.remove(record);
            }
        }));
        discovery.close();
    }

    protected void publishRecord(Record record, Handler<AsyncResult<Record>> resultHandler) {
        discovery.publish(record, ar -> {
            if (ar.succeeded()) {
                records.add(ar.result());
            }
            resultHandler.handle(ar);
        });
    }

    protected void getWebClientOnce(long delay, JsonObject filter, Handler<AsyncResult<WebClient>> resultHandler) {
        getWebClientInternal(delay, filter, true, resultHandler);
    }

    protected void getWebClient(long delay, JsonObject filter, Handler<AsyncResult<WebClient>> resultHandler) {
        getWebClientInternal(delay, filter, false, resultHandler);
    }

    protected void getWebAppClient() {
        getWebClient(10_000, new JsonObject().put("name", "webapp-service"), ar -> {
            if (ar.succeeded()) {
                webAppClient = ar.result();
                System.out.println("Got webapp WebClient");
            } else {
                System.err.println("Could not retrieve user client: " + ar.cause().getMessage());
            }
        });
    }

    private void getWebClientInternal(long delay, JsonObject filter, boolean once, Handler<AsyncResult<WebClient>> resultHandler) {
        final Handler<Long> tidHandler = tid ->
            HttpEndpoint.getWebClient(discovery, filter, ar -> {
                resultHandler.handle(ar);
                if (ar.succeeded()) {
                    vertx.cancelTimer(tid);
                }
            });
        if (once) {
            vertx.setTimer(delay, tidHandler);
        } else {
            vertx.setPeriodic(delay, tidHandler);
        }
    }

}
