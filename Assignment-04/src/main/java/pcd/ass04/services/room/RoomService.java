package pcd.ass04.services.room;

import io.reactivex.Completable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

import static io.vertx.core.http.HttpMethod.*;
import static pcd.ass04.util.ServiceDiscoveryUtils.getWebClient;

public final class RoomService extends AbstractVerticle {

    private static final long CRITICAL_SECTION_TIMEOUT = 30_000;

    private final RoomRepository repository;
    private ServiceDiscovery discovery;
    private Record record;

    private String host;
    private int port;

    private WebClient guiClient;

    // Critical section
    private final Map<Room, Optional<User>> csMap = new HashMap<>();
    private OptionalLong csTimerId = OptionalLong.empty();

    public RoomService() {
        repository = new RoomRepositoryImpl();
    }

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
            } else {
                System.err.println("Could not retrieve GUI client: " + ar.cause().getMessage());
            }
        });

        final Router apiRouter = Router.router(vertx);

        apiRouter.route()
                .handler(BodyHandler.create());

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
        repository.findAll()
                .map(Room::toJson)
                .toList()
                .map(JsonArray::new)
                .map(Object::toString)
                .subscribe(
                        ctx.response()::end,
                        cause -> ctx.response()
                                .setStatusCode(500)
                                .end(new JsonObject().put("error", cause.getMessage()).toString()));
    }

    private void store(RoutingContext ctx) {
        final Room room = Room.fromJson(ctx.getBodyAsJson());

        repository.save(room)
                .flatMap(repository::findById)
                .map(Object::toString)
                .subscribe(
                        chunk -> {
                            csMap.put(room, Optional.empty());
                            ctx.response().end(chunk);
                        },
                        cause -> ctx.response()
                                .setStatusCode(500)
                                .end(cause.getMessage()));
    }

    private void show(RoutingContext ctx) {
        final long id = Long.parseLong(ctx.request().getParam("id"));
        repository.findById(id)
                .map(Object::toString)
                .subscribe(
                        ctx.response()::end,
                        cause -> ctx.response()
                                .setStatusCode(500)
                                .end(cause.getMessage()));

    }

    private void update(RoutingContext ctx) {
        repository.save(Room.fromJson(ctx.getBodyAsJson()))
                .flatMap(repository::findById)
                .map(Object::toString)
                .subscribe(
                        ctx.response()::end,
                        cause -> ctx.response()
                                .setStatusCode(500)
                                .end(cause.getMessage()));
    }

    private void destroy(RoutingContext ctx) {
        final long id = Long.parseLong(ctx.request().getParam("id"));
        repository.findById(id)
                .map(csMap::remove)
                .flatMap(v -> repository.deleteById(id))
                .subscribe(
                        roomId -> {
                            ctx.response().end(new JsonObject().put("id", roomId).toString());
                        },
                        cause -> ctx.response()
                                .setStatusCode(500)
                                .end(cause.getMessage()));
    }

    private void join(RoutingContext ctx) {
        final long roomId = Long.parseLong(ctx.request().getParam("roomId"));
        final User user = User.fromJson(ctx.getBodyAsJson());

        repository.findById(roomId)
                .flatMapCompletable(room -> repository.addUser(room, user))
                .subscribe(
                        ctx.response()::end,
                        cause -> ctx.response()
                                .setStatusCode(500)
                                .end(cause.getMessage()));
    }

    private void leave(RoutingContext ctx) {
        final long roomId = Long.parseLong(ctx.request().getParam("roomId"));
        final long userId = Long.parseLong(ctx.request().getParam("userId"));

        repository.findById(roomId)
                .flatMapCompletable(room -> repository.findUserById(room, userId)
                        .flatMapCompletable(user -> repository.removeUser(room, user)))
                .subscribe(
                        ctx.response()::end,
                        cause -> ctx.response()
                                .setStatusCode(500)
                                .end(cause.getMessage()));
    }

    private void messages(RoutingContext ctx) {
    }

    private void status(RoutingContext ctx) {
        final long roomId = Long.parseLong(ctx.request().getParam("roomId"));

        repository.findById(roomId)
                .subscribe(room -> {
                            final JsonObject response = new JsonObject();

                            response.put("held", false);
                            response.putNull("user");

                            csMap.get(room).ifPresent(user -> {
                                response.put("held", true);
                                response.put("user", user.toJson());
                            });

                            ctx.response().end(response.toString());
                        },
                        cause -> ctx.response()
                                .setStatusCode(500)
                                .end(cause.getMessage()));
    }

    private void enter(RoutingContext ctx) {
        final long id = Long.parseLong(ctx.request().getParam("roomId"));
        final User user = User.fromJson(ctx.getBodyAsJson());

        repository.findById(id)
                .map(room -> {
                    final Optional<User> csUser = csMap.get(room);

                    if (csUser.isPresent()) {
                        throw new IllegalStateException("Critical section is already held by " + csUser.get().getName());
                    }
                    csMap.put(room, Optional.of(user));
                    final long tid = vertx.setTimer(CRITICAL_SECTION_TIMEOUT, v -> csMap.put(room, Optional.empty()));
                    csTimerId = OptionalLong.of(tid);
                    return room;
                })
                .subscribe(
                        room -> ctx.response().end(),
                        cause -> ctx.response()
                                .setStatusCode(500)
                                .end(cause.getMessage()));
    }

    private void exit(RoutingContext ctx) {
        final long roomId = Long.parseLong(ctx.request().getParam("roomId"));
        final long userId = Long.parseLong(ctx.request().getParam("userId"));

        repository.findById(roomId)
                .flatMapCompletable(room -> repository.findUserById(room, userId)
                        .flatMapCompletable(user -> Completable.fromAction(() -> {
                            final Optional<User> csUserOpt = csMap.get(room);
                            if (!csUserOpt.isPresent()) {
                                throw new IllegalStateException("Critical section is not held by a user");
                            } else {
                                if (csUserOpt.get().equals(user)) {
                                    csMap.put(room, Optional.empty());
                                    csTimerId.ifPresent(vertx::cancelTimer);
                                    csTimerId = OptionalLong.empty();
                                } else {
                                    throw new RuntimeException("The user who tried to release the critical section is not the user who acquired it");
                                }
                            }
                        })))
                .subscribe(
                        ctx.response()::end,
                        cause -> ctx.response()
                                .setStatusCode(500)
                                .end(cause.getMessage()));
    }

}
