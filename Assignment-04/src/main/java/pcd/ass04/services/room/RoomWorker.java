package pcd.ass04.services.room;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import pcd.ass04.ServiceVerticle;
import pcd.ass04.services.room.domain.Room;
import pcd.ass04.services.room.domain.User;
import pcd.ass04.services.room.repository.RoomRepository;

import java.util.*;
import java.util.stream.Collectors;

import static pcd.ass04.services.room.Channels.*;

final class RoomWorker extends ServiceVerticle {

    private static final long CRITICAL_SECTION_TIMEOUT = 30_000;

    private final RoomRepository repository;

    private final Map<Room, Optional<User>> csMap;
    private final Map<Room, OptionalLong> timerIdMap;

    private final Map<Room, Long> counterMap;
    private final Map<Room, Map<User, Long>> userCounterMap;

    private WebClient healthCheckClient;
    private WebClient webAppClient;

    RoomWorker(RoomRepository repository, Map<Room, Optional<User>> csMap, Map<Room, OptionalLong> timerIdMap, Map<Room, Long> counterMap, Map<Room, Map<User, Long>> userCounterMap) {
        this.repository = repository;
        this.csMap = csMap;
        this.timerIdMap = timerIdMap;
        this.counterMap = counterMap;
        this.userCounterMap = userCounterMap;
    }

    @Override
    public void start() throws Exception {
        getWebAppClient();
        getHealthCheckClient();

        eventBus.consumer(INDEX, msg -> {
            repository.findAll()
                    .map(Room::toJson)
                    .toList()
                    .map(JsonArray::new)
                    .subscribe(
                            msg::reply,
                            cause -> msg.fail(500, cause.getMessage()));

        });

        eventBus.<JsonObject>consumer(STORE, msg -> {
            final JsonObject body = msg.body();
            final Room room = Room.fromJson(body.getJsonObject("request"));

            repository.save(room)
                    .flatMap(repository::findById)
                    .subscribe(
                            r -> {
                                counterMap.put(r, 0L);
                                csMap.put(r, Optional.empty());
                                msg.reply(r.toJson());
                            },
                            cause -> {
                                System.err.println(cause.getMessage());
                                msg.fail(500, cause.getMessage());
                            });
        });

        eventBus.<JsonObject>consumer(SHOW, msg -> {
            final JsonObject body = msg.body();
            final JsonObject params = body.getJsonObject("params");

            final long id = Long.parseLong(params.getString("id"));
            System.out.println("Room id: id");
            repository.findById(id)
                    .map(Room::toJson)
                    .subscribe(
                            msg::reply,
                            cause -> msg.fail(500, cause.getMessage()));
        });

        eventBus.<JsonObject>consumer(UPDATE, msg -> {
            final JsonObject body = msg.body();
            final Room room = Room.fromJson(body.getJsonObject("request"));

            repository.save(room)
                    .flatMap(repository::findById)
                    .map(Room::toJson)
                    .subscribe(
                            msg::reply,
                            cause -> msg.fail(500, cause.getMessage()));
        });

        eventBus.<JsonObject>consumer(DESTROY, msg -> {
            final JsonObject body = msg.body();
            final JsonObject params = body.getJsonObject("params");

            final long id = Long.parseLong(params.getString("id"));

            repository.findById(id)
                    .map(room -> {
                        csMap.remove(room);
                        counterMap.remove(room);
                        return room;
                    })
                    .flatMap(v -> repository.deleteById(id))
                    .subscribe(
                            msg::reply,
                            cause -> msg.fail(500, cause.getMessage()));
        });

        eventBus.<JsonObject>consumer(JOIN, msg -> {
            final JsonObject body = msg.body();
            final JsonObject request = body.getJsonObject("request");
            final JsonObject params = body.getJsonObject("params");

            final long roomId = Long.parseLong(params.getString("roomId"));
            final User user = User.fromJson(request);

            repository.findById(roomId)
                    .flatMap(room -> {
                        JsonObject response = new JsonObject();
                        if (!this.userCounterMap.containsKey(room)) {
                            Map<User, Long> usersCounter = Collections.synchronizedMap(new HashMap<>());
                            this.userCounterMap.put(room, usersCounter);
                        }
                        this.userCounterMap.get(room).put(user, 0L);
                        response.put("usersClock", new JsonArray(this.userCounterMap.get(room).entrySet().stream()
                                .map(e -> new JsonObject()
                                        .put("user", e.getKey().toJson())
                                        .put("userClock", e.getValue()))
                                .collect(Collectors.toList())));
                        response.put("globalCounter", counterMap.get(room));
                        System.out.println("web service join: " + response);
                        return repository.addUser(room, user).andThen(Single.just(response));
                    })
                    .subscribe(
                            msg::reply,
                            cause -> msg.fail(500, cause.getMessage()));
        });

        eventBus.<JsonObject>consumer(LEAVE, msg -> {
            final JsonObject body = msg.body();
            final JsonObject params = body.getJsonObject("params");

            final long roomId = Long.parseLong(params.getString("roomId"));
            final long userId = Long.parseLong(params.getString("userId"));

            repository.findById(roomId)
                    .flatMapCompletable(room -> repository.findUserById(room, userId)
                            .flatMapCompletable(user -> repository.removeUser(room, user)))
                    .subscribe(
                            () -> msg.reply(new JsonObject()),
                            cause -> msg.fail(500, cause.getMessage()));
        });

        eventBus.<JsonObject>consumer(MESSAGES, msg -> {
            final JsonObject body = msg.body();
            final JsonObject request = body.getJsonObject("request");
            final JsonObject params = body.getJsonObject("params");

            final long id = Long.parseLong(params.getString("roomId"));

            System.out.println("Room ID: " + id);

            final User user = User.fromJson(request.getJsonObject("user"));

            System.out.println("User: " + user);

            repository.findById(id)
                    .map(room -> {
                        System.out.println("csMap: " + csMap);
                        final Optional<User> csUser = csMap.get(room);
                        System.out.println("CS user: " + csUser);
                        if (csUser.isPresent() && !csUser.get().equals(user)) {
                            throw new IllegalStateException("The user who tried to send the message is not the user in critical section");
                        }
                        System.out.println("User is allowed to send the message");
                        this.userCounterMap.get(room).computeIfPresent(user, (k, v) -> request.getLong("userClock"));
                        System.out.println("Got user clock");
                        counterMap.computeIfPresent(room, (k, v) -> v + 1);
                        System.out.println("Updated room counter");
                        return request.put("globalCounter", counterMap.get(room)).put("userClock", this.userCounterMap.get(room).get(user));
                    })
                    .subscribe(
                            msg::reply,
                            cause -> msg.fail(500, cause.getMessage()));
        });

        eventBus.<JsonObject>consumer(STATUSCS, msg -> {
            final JsonObject body = msg.body();
            final JsonObject params = body.getJsonObject("params");
            final long roomId = Long.parseLong(params.getString("roomId"));

            repository.findById(roomId)
                    .subscribe(room -> {
                                final JsonObject response = new JsonObject();

                                response.put("held", false);
                                response.putNull("user");

                                csMap.get(room).ifPresent(user -> {
                                    response.put("held", true);
                                    response.put("user", user.toJson());
                                });

                                msg.reply(response);
                            },
                            cause -> msg.fail(500, cause.getMessage()));
        });

        eventBus.<JsonObject>consumer(EXITCS, msg -> {
            final JsonObject body = msg.body();
            final JsonObject request = body.getJsonObject("request");
            final JsonObject params = body.getJsonObject("params");

            final long roomId = Long.parseLong(params.getString("roomId"));
            final User user = User.fromJson(request);

            repository.findById(roomId)
                    .map(room -> {
                        final Optional<User> csUser = csMap.get(room);

                        if (csUser.isPresent()) {
                            throw new IllegalStateException("Critical section is already held by " + csUser.get().getName());
                        }
                        csMap.put(room, Optional.of(user));
                        final long tid = vertx.setTimer(CRITICAL_SECTION_TIMEOUT, v -> {
                            csMap.put(room, Optional.empty());
                            timerIdMap.put(room , OptionalLong.empty());
                            checkHealth("webapp", () -> webAppClient.post("/api/messages")
                                    .sendJson(room, ar -> {
                                        if (ar.succeeded()) {
                                            System.out.println("Sent timeout expired");
                                        } else {
                                            System.err.println("Could not send timeout expired: " + ar.cause().getMessage());
                                        }
                                    }));
                        });
                        timerIdMap.put(room , OptionalLong.of(tid));
                        return room;
                    })
                    .subscribe(
                            room -> msg.reply(new JsonObject()),
                            cause -> msg.fail(500, cause.getMessage()));
        });

        eventBus.<JsonObject>consumer(ENTERCS, msg -> {
            final JsonObject body = msg.body();
            final JsonObject params = body.getJsonObject("params");

            final long roomId = Long.parseLong(params.getString("roomId"));
            final long userId = Long.parseLong(params.getString("userId"));

            repository.findById(roomId)
                    .flatMapCompletable(room -> repository.findUserById(room, userId)
                            .flatMapCompletable(user -> Completable.fromAction(() -> {
                                final Optional<User> csUserOpt = csMap.get(room);
                                if (!csUserOpt.isPresent()) {
                                    throw new IllegalStateException("Critical section is not held by a user");
                                } else {
                                    if (csUserOpt.get().equals(user)) {
                                        csMap.put(room, Optional.empty());
                                        timerIdMap.get(room).ifPresent(vertx::cancelTimer);
                                        timerIdMap.put(room , OptionalLong.empty());
                                    } else {
                                        throw new RuntimeException("The user who tried to release the critical section is not the user who acquired it");
                                    }
                                }
                            })))
                    .subscribe(
                            () -> msg.reply(new JsonObject()),
                            cause -> msg.fail(500, cause.getMessage()));
        });
    }

    private void getWebAppClient() {
        getWebClient(10_000, new JsonObject().put("name", "webapp-service"), ar -> {
            if (ar.succeeded()) {
                webAppClient = ar.result();
                System.out.println("Got webapp WebClient");
            } else {
                System.err.println("Could not retrieve user client: " + ar.cause().getMessage());
            }
        });
    }

    private void getHealthCheckClient() {
        getWebClient(10_000, new JsonObject().put("name", "healthcheck-service"), ar -> {
            if (ar.succeeded()) {
                healthCheckClient = ar.result();
                System.out.println("Got healthcheck WebClient");
            } else {
                System.err.println("Could not retrieve healthcheck client: " + ar.cause().getMessage());
            }
        });
    }

    private void checkHealth(String serviceName, Runnable successBlock) {
        healthCheckClient.get("/health/" + serviceName)
                .send(ar -> {
                    if (ar.succeeded() && ar.result().statusCode() == 200) {
                        successBlock.run();
                    } else {
                        throw new RuntimeException(serviceName + " service is not available!");
                    }
                });
    }

}
