package pcd.ass04.services.room;

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

import static io.vertx.core.http.HttpMethod.PATCH;
import static io.vertx.core.http.HttpMethod.PUT;
import static pcd.ass04.util.ServiceDiscoveryUtils.getWebClient;

public final class RoomService extends AbstractVerticle {

    private ServiceDiscovery discovery;
    private Record record;

    private String host;
    private int port;

    private WebClient brokerClient;
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

        getWebClient(vertx, discovery, 10_000, new JsonObject().put("name", "broker-service"), ar -> {
            if (ar.succeeded()) {
                brokerClient = ar.result();
                System.out.println("Got broker WebClient");
            } else {
                System.err.println("Could not retrieve broker client: " + ar.cause().getMessage());
            }
        });

        getWebClient(vertx, discovery, 10_000, new JsonObject().put("name", "gui-service"), ar -> {
            if (ar.succeeded()) {
                guiClient = ar.result();
            } else {
                System.err.println("Could not retrieve GUI client: " + ar.cause().getMessage());
            }
        });

        final Router apiRouter = Router.router(vertx);

        apiRouter.get("/rooms")
                .produces("application/json")
                .handler(this::index);

        apiRouter.post("/rooms")
                .consumes("application/json")
                .produces("application/json")
                .handler(this::store);

        apiRouter.get("/rooms/:id")
                .produces("application/json")
                .handler(this::show);

        apiRouter.route("/rooms/:id")
                .method(PUT)
                .method(PATCH)
                .consumes("application/json")
                .produces("application/json")
                .handler(this::update);

        apiRouter.delete("/rooms/:id")
                .produces("application/json")
                .handler(this::destroy);

        apiRouter.post("/rooms/:roomId/join")
                .consumes("application/json")
                .produces("application/json")
                .handler(this::join);

        apiRouter.delete("/rooms/:userId/leave/:userId")
                .produces("application/json")
                .handler(this::leave);

        apiRouter.post("/rooms/:roomId/messages")
                .consumes("application/json")
                .produces("application/json")
                .handler(this::messages);

        apiRouter.delete("/rooms/:roomId/cs")
                .produces("application/json")
                .handler(this::status);

        apiRouter.post("/rooms/:roomId/cs/enter")
                .consumes("application/json")
                .produces("application/json")
                .handler(this::enter);

        apiRouter.delete("/rooms/:roomId/cs/exit/:userId")
                .produces("application/json")
                .handler(this::exit);

        final Router router = Router.router(vertx);

        router.mountSubRouter("/api", apiRouter);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(port, host, ar -> {
                    if (ar.succeeded()) {
                        discovery.publish(HttpEndpoint.createRecord("room-service", host, port, "/api"), ar1 -> {
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

    private void index(RoutingContext ctx) {
    }

    private void store(RoutingContext ctx) {
    }

    private void show(RoutingContext ctx) {
    }

    private void update(RoutingContext ctx) {
    }

    private void destroy(RoutingContext ctx) {
    }

    private void join(RoutingContext ctx) {
    }

    private void leave(RoutingContext ctx) {
    }

    private void messages(RoutingContext ctx) {
    }

    private void status(RoutingContext ctx) {
    }

    private void enter(RoutingContext ctx) {
    }

    private void exit(RoutingContext ctx) {
    }

}
