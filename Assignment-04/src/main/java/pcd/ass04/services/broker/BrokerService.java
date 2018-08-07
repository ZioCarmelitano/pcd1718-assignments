package pcd.ass04.services.broker;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;

import static pcd.ass04.util.ServiceDiscoveryUtils.getWebClient;

public final class BrokerService extends AbstractVerticle {

    private ServiceDiscovery discovery;
    private Record record;

    private String host;
    private int port;

    private WebClient guiClient;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

        final JsonObject config = context.config();
        host = config.getString("host");
        port = config.getInteger("port");
    }

    @Override
    public void start() {
        discovery = ServiceDiscovery.create(vertx);

        getWebClient(vertx, discovery, 10_000, new JsonObject().put("name", "gui-service"), ar -> {
            if (ar.succeeded()) {
                guiClient = ar.result();
                System.out.println("Got gui WebClient");
            } else {
                System.err.println("Could not retrieve GUI client: " + ar.cause().getMessage());
            }
        });

        final Router apiRouter = Router.router(vertx);

        apiRouter.post("/messages")
                .consumes("application/json")
                .produces("application/json")
                .handler(this::handleMessage);

        final Router router = Router.router(vertx);

        router.mountSubRouter("/api", apiRouter);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(port, host, ar -> {
           if (ar.succeeded()) {
               discovery.publish(HttpEndpoint.createRecord("broker-service", host, port, "/api"), ar1 -> {
                   if (ar1.succeeded()) {
                       record = ar1.result();
                   } else {
                       System.err.println("Could not publish record: " + ar1.cause().getMessage());
                   }
               });
               System.out.println("HTTP server started at http://" + host + ":" + port);
           } else {
               System.err.println("Could not start HTTP server: " + ar.cause().getMessage());
           }
        });
    }

    @Override
    public void stop() {
        discovery.unpublish(record.getRegistration(),
                ar -> {
                    if (ar.succeeded()) {
                        System.out.println("Record " + record.getName() + " withdrawn successfully");
                    } else {
                        System.out.println("Could not withdraw record " + record.getName());
                    }
                });
        discovery.close();
    }

    private void handleMessage(RoutingContext ctx) {
    }

}
