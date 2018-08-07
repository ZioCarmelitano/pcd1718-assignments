package pcd.ass04.services.user;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.Record;

import static io.vertx.core.http.HttpMethod.PATCH;
import static io.vertx.core.http.HttpMethod.PUT;

public final class UserService extends AbstractVerticle {

    private ServiceDiscovery discovery;
    private Record record;

    private String host;
    private int port;

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

        final Router apiRouter = Router.router(vertx);

        apiRouter.get("/users")
                .produces("application/json")
                .handler(this::index);

        apiRouter.post("/users")
                .consumes("application/json")
                .produces("application/json")
                .handler(this::store);

        apiRouter.get("/users/:id")
                .produces("application/json")
                .handler(this::show);

        apiRouter.route("/users/:id")
                .method(PUT)
                .method(PATCH)
                .consumes("application/json")
                .produces("application/json")
                .handler(this::update);

        apiRouter.delete("/users/:id")
                .produces("application/json")
                .handler(this::destroy);

        final Router router = Router.router(vertx);

        router.mountSubRouter("/api", apiRouter);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(port, host, ar -> {
                    if (ar.succeeded()) {
                        discovery.publish(HttpEndpoint.createRecord("user-service", host, port, "/api"), ar1 -> {
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

}
