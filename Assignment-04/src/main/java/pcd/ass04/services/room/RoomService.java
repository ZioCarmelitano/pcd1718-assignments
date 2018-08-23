package pcd.ass04.services.room;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;
import pcd.ass04.services.room.domain.Room;
import pcd.ass04.services.room.domain.User;
import pcd.ass04.services.room.repository.RoomRepository;
import pcd.ass04.services.room.repository.RoomRepositoryImpl;

import java.util.*;
import java.util.stream.Collectors;

import static io.vertx.core.http.HttpMethod.*;
import static pcd.ass04.services.room.Channels.*;
import static pcd.ass04.util.ServiceDiscoveryUtils.getWebClient;

public final class RoomService extends AbstractVerticle {

    private static final long CRITICAL_SECTION_TIMEOUT = 30_000;
    public static final List<HttpMethod> METHODS_WITH_BODY = Arrays.asList(POST, PUT, PATCH);

    private ServiceDiscovery discovery;
    private Record record;

    private String host;
    private int port;

    private EventBus eventBus;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

        final JsonObject config = context.config();
        host = config.getString("host");
        port = config.getInteger("port");
    }

    @Override
    public void start() {
        eventBus = vertx.eventBus();
        discovery = ServiceDiscovery.create(vertx);

        deployWorkers();

        final Router apiRouter = Router.router(vertx);

        apiRouter.route().handler(BodyHandler.create());
        apiRouter.route().handler(CorsHandler.create("*")
                .allowedMethod(GET)
                .allowedMethod(POST)
                .allowedMethod(PATCH)
                .allowedMethod(PUT)
                .allowedMethod(DELETE)
                .allowedHeader("Access-Control-Allow-Method")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Access-Control-Allow-Credentials")
                .allowedHeader("Content-Type"));

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

        apiRouter.delete("/rooms/:roomId/leave/:userId")
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

        final HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx);

        healthCheckHandler.register("health-check-procedure", future -> future.complete(Status.OK()));

        router.get("/health*")
                .produces("application/json")
                .handler(healthCheckHandler);

        router.mountSubRouter("/api", apiRouter);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(port, host, ar -> {
                    if (ar.succeeded()) {
                        discovery.publish(HttpEndpoint.createRecord("room-service", host, port, "/"), ar1 -> {
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

    private void deployWorkers() {
        final DeploymentOptions options = new DeploymentOptions()
                .setWorker(true)
                .setInstances(10);

        final RoomRepository repository = new RoomRepositoryImpl();
        final Map<Room, Optional<User>> csMap = Collections.synchronizedMap(new HashMap<>());
        final Map<Room, OptionalLong> timerIdMap = Collections.synchronizedMap(new HashMap<>());
        final Map<Room, Long> counterMap = Collections.synchronizedMap(new HashMap<>());
        final Map<Room, Map<User, Long>> userCounterMap = Collections.synchronizedMap(new HashMap<>());

        vertx.deployVerticle(() -> new RoomWorker(repository, csMap, timerIdMap, counterMap, userCounterMap), options);
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
        send(INDEX, ctx);
    }

    private void store(RoutingContext ctx) {
        send(STORE, ctx);
    }

    private void show(RoutingContext ctx) {
        send(SHOW, ctx);

    }

    private void update(RoutingContext ctx) {
        send(UPDATE, ctx);
    }

    private void destroy(RoutingContext ctx) {
        send(DESTROY, ctx);
    }

    private void join(RoutingContext ctx) {
        send(JOIN, ctx);
    }

    private void leave(RoutingContext ctx) {
        send(LEAVE, ctx);
    }

    private void messages(RoutingContext ctx) {
        send(MESSAGES, ctx);
    }

    private void status(RoutingContext ctx) {
        send(STATUSCS, ctx);
    }

    private void enter(RoutingContext ctx) {
        send(ENTERCS, ctx);
    }

    private void exit(RoutingContext ctx) {
        send(EXITCS, ctx);
    }

    private void send(String channel, RoutingContext ctx) {
        final JsonObject params = JsonObject.mapFrom(ctx.pathParams());
        System.out.println("Params: " + params);

        final JsonObject request = getRequest(ctx);
        System.out.println("Request: " + request);
        eventBus.send(channel, new JsonObject()
                .put("params", params)
                .put("request", request), ar -> {
            if (ar.succeeded()) {
                final Object response = ar.result().body();
                System.out.println("Response: " + response);
                ctx.response().end(response.toString());
            } else {
                System.out.println("Error: " + ar.cause().getMessage());
                final String message = ar.cause().getMessage();
                ctx.response()
                        .setStatusCode(500)
                        .end(new JsonObject().put("error", message).toString());
            }
        });
    }

    private static JsonObject getRequest(RoutingContext ctx) {
        final HttpMethod method = ctx.request().method();
        if (METHODS_WITH_BODY.contains(method)) {
            return ctx.getBodyAsJson();
        } else {
            return new JsonObject();
        }
    }

}
