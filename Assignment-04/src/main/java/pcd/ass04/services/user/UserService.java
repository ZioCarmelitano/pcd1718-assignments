package pcd.ass04.services.user;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;
import pcd.ass04.services.user.exceptions.UserNotFoundException;
import pcd.ass04.services.user.model.User;
import pcd.ass04.services.user.repositories.InMemoryUserRepository;
import pcd.ass04.services.user.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.vertx.core.http.HttpMethod.PATCH;
import static io.vertx.core.http.HttpMethod.PUT;

public final class UserService extends AbstractVerticle {

    private ServiceDiscovery discovery;
    private Record record;

    private String host;
    private int port;

    private UserRepository userRepository;

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
                        this.userRepository = InMemoryUserRepository.getInstance();
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
        List<User> users = this.userRepository.getAll();
        JsonArray userArray = new JsonArray();
        users.stream().map(User::toJson).forEach(userArray::add);
        ctx.response().end(userArray.toString());
    }

    private void store(RoutingContext ctx) {
        JsonObject userToStoreJson = ctx.getBodyAsJson();
        this.userRepository.store(new User(userToStoreJson));
    }

    private void show(RoutingContext ctx) {
        long userId = Long.valueOf(ctx.request().getParam("id"));
        Optional<User> user = this.userRepository.get(userId);
        if (user.isPresent()) {
            ctx.response().end(user.get().toJson().toString());
        } else {
            ctx.response().setStatusCode(NOT_FOUND.code()).end("User to show not found");
        }
    }

    private void update(RoutingContext ctx) {
        JsonObject userToUpdateJson = ctx.getBodyAsJson();
        try {
            this.userRepository.update(new User(userToUpdateJson));
            ctx.response().end();
        } catch (UserNotFoundException e) {
            ctx.response().setStatusCode(NOT_FOUND.code()).end("User to update not found");
        }
    }

    private void destroy(RoutingContext ctx) {
        long userId = Long.valueOf(ctx.request().getParam("id"));
        try {
            this.userRepository.destroy(userId);
            ctx.response().end();
        } catch (UserNotFoundException e) {
            ctx.response().setStatusCode(NOT_FOUND.code()).end("User to delete not found");
        }
    }

}