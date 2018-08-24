package pcd.ass04.services.user;

import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.servicediscovery.types.HttpEndpoint;
import pcd.ass04.ServiceVerticle;
import pcd.ass04.services.user.repositories.UserRepositoryImpl;
import pcd.ass04.services.user.repositories.UserRepository;

import java.util.Arrays;
import java.util.List;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.vertx.core.http.HttpMethod.*;
import static pcd.ass04.services.user.Channels.*;

public final class UserService extends ServiceVerticle {

    private String host;
    private int port;

    private static final List<HttpMethod> METHODS_WITH_BODY = Arrays.asList(POST, PUT, PATCH);

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

        deployUserWorkers();

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

        final HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx);

        healthCheckHandler.register("health-check-procedure", future -> future.complete(Status.OK()));

        router.get("/health*")
                .produces("application/json")
                .handler(healthCheckHandler);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(port, host, ar -> {
                    if (ar.succeeded()) {
                        publishRecord(HttpEndpoint.createRecord("user-service", host, port, "/"), ar1 -> {
                            if (ar1.succeeded()) {
                                System.out.println("UserService record published with success!");
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

    private void deployUserWorkers() {
        final DeploymentOptions options = new DeploymentOptions()
                .setWorker(true)
                .setInstances(10);

        final UserRepository repository = UserRepositoryImpl.getInstance();

        vertx.deployVerticle(() -> new UserWorker(repository), options);
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
                        .setStatusCode(INTERNAL_SERVER_ERROR.code())
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
