package pcd.ass04.services.room;

import io.reactivex.Completable;
import io.reactivex.Single;
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
import java.util.stream.Collectors;

import static io.vertx.core.http.HttpMethod.*;
import static pcd.ass04.util.ServiceDiscoveryUtils.getWebClient;

public final class RoomService extends AbstractVerticle {

    private static final long CRITICAL_SECTION_TIMEOUT = 30_000;

    private ServiceDiscovery discovery;
    private Record record;

    private String host;
    private int port;

    private final RoomRepository repository = new RoomRepositoryImpl();

    // Critical section
    private final Map<Room, Optional<User>> csMap = new HashMap<>();
    private OptionalLong csTimerId = OptionalLong.empty();
    private WebClient webAppClient;

    private final Map<Room, Long> counterMap = new HashMap<>();
    private final Map<Room, Map<User, Long>> userCounterMap = new HashMap<>();

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

        getWebClient(vertx, discovery, 10_000, new JsonObject().put("name", "webapp-service"), ar -> {
            if (ar.succeeded()) {
                webAppClient = ar.result();
                System.out.println("Got webapp WebClient");
            } else {
                System.err.println("Could not retrieve user client: " + ar.cause().getMessage());
            }
        });

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
                                .end(new JsonObject().put("error", new JsonObject().put("error", cause.getMessage()).toString()).toString()));
    }

    private void store(RoutingContext ctx) {
        final Room room = Room.fromJson(ctx.getBodyAsJson());

        repository.save(room)
                // findById? dovrebbe essere add room...
                .flatMap(repository::findById)
                .map(Object::toString)
                .subscribe(
                        chunk -> {
                            csMap.put(room, Optional.empty());
                            counterMap.put(room, 0L);
                            ctx.response().end(chunk);
                            System.out.println(chunk);
                        },
                        cause -> ctx.response()
                                .setStatusCode(500)
                                .end(new JsonObject().put("error", cause.getMessage()).toString()));
    }

    private void show(RoutingContext ctx) {
        final long id = Long.parseLong(ctx.request().getParam("id"));
        repository.findById(id)
                .map(Object::toString)
                .subscribe(
                        ctx.response()::end,
                        cause -> ctx.response()
                                .setStatusCode(500)
                                .end(new JsonObject().put("error", cause.getMessage()).toString()));

    }

    private void update(RoutingContext ctx) {
        repository.save(Room.fromJson(ctx.getBodyAsJson()))
                .flatMap(repository::findById)
                .map(Object::toString)
                .subscribe(
                        ctx.response()::end,
                        cause -> ctx.response()
                                .setStatusCode(500)
                                .end(new JsonObject().put("error", cause.getMessage()).toString()));
    }

    private void destroy(RoutingContext ctx) {
        final long id = Long.parseLong(ctx.request().getParam("id"));
        repository.findById(id)
                .map(room -> {csMap.remove(room); counterMap.remove(room); return room;})
                .flatMap(v -> repository.deleteById(id))
                .subscribe(
                        roomId -> {
                            ctx.response().end(new JsonObject().put("id", roomId).toString());
                        },
                        cause -> ctx.response()
                                .setStatusCode(500)
                                .end(new JsonObject().put("error", cause.getMessage()).toString()));
    }

    private void join(RoutingContext ctx) {
        final long roomId = Long.parseLong(ctx.request().getParam("roomId"));
        final User user = User.fromJson(ctx.getBodyAsJson());

        repository.findById(roomId)
                .flatMap(room -> {
                    JsonObject response = new JsonObject();
                    if (!this.userCounterMap.containsKey(room)) {
                        HashMap<User, Long> usersCounter = new HashMap<>();
                        this.userCounterMap.put(room, usersCounter);
                    }
                    this.userCounterMap.get(room).put(user, 0L);
                    response.put("usersClock", new JsonArray(this.userCounterMap.get(room).entrySet().stream()
                            .map(e -> new JsonObject().put("user", e.getKey().toJson()).put("userClock", e.getValue()))
                            .collect(Collectors.toList())));
                    response.put("globalCounter", counterMap.get(room));
                    System.out.println("web service join: " + response.toString());
                    return repository.addUser(room, user).andThen(Single.just(response));
                })
                .map(Object::toString)
                .subscribe(
                        ctx.response()::end,
                        cause -> ctx.response()
                                .setStatusCode(500)
                                .end(new JsonObject().put("error", cause.getMessage()).toString()));
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
                                .end(new JsonObject().put("error", cause.getMessage()).toString()));
    }

    private void messages(RoutingContext ctx) {
        final long id = Long.parseLong(ctx.request().getParam("roomId"));

        final JsonObject body = ctx.getBodyAsJson();
        final String content = body.getString("content");
        final User user = User.fromJson(body.getJsonObject("user"));

        repository.findById(id)
                .map(room -> {
                    final Optional<User> csUser = csMap.get(room);
                    if (csUser.isPresent() && !csUser.get().equals(user)) {
                        throw new IllegalStateException("The user who tried to send the message is not the user in critical section");
                    }
                    this.userCounterMap.get(room).computeIfPresent(user, (k,v) -> body.getLong("userClock"));
                    counterMap.computeIfPresent(room, (k,v) -> v + 1);
                    return body.put("globalCounter", counterMap.get(room)).put("userClock", this.userCounterMap.get(room).get(user));
                })
                .map(Object::toString)
                .subscribe(
                        ctx.response()::end,
                        cause -> ctx.response()
                                .setStatusCode(500)
                                .end(new JsonObject().put("error", cause.getMessage()).toString()));
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
                                .end(new JsonObject().put("error", cause.getMessage()).toString()));
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
                        final long tid = vertx.setTimer(CRITICAL_SECTION_TIMEOUT, v -> {
                        csMap.put(room, Optional.empty());
                        csTimerId = OptionalLong.empty();
                        webAppClient.post("/api/messages")
                                .sendJson(room, ar -> {
                                    if (ar.succeeded()) {
                                        System.out.println("Sent timeout expired");
                                    } else {
                                        System.err.println("Could not send timeout expired: " + ar.cause().getMessage());
                                    }
                                });
                    });
                    csTimerId = OptionalLong.of(tid);
                    return room;
                })
                .subscribe(
                        room -> ctx.response().end(),
                        cause -> ctx.response()
                                .setStatusCode(500)
                                .end(new JsonObject().put("error", cause.getMessage()).toString()));
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
                                .end(new JsonObject().put("error", cause.getMessage()).toString()));
    }

}
