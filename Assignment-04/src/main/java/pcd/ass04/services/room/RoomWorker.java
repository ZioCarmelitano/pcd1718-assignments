package pcd.ass04.services.room;

import io.reactivex.Completable;
import io.reactivex.Single;
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

    RoomWorker(RoomRepository repository, Map<Room, Optional<User>> csMap, Map<Room, OptionalLong> timerIdMap, Map<Room, Long> counterMap, Map<Room, Map<User, Long>> userCounterMap) {
        this.repository = repository;
        this.csMap = csMap;
        this.timerIdMap = timerIdMap;
        this.counterMap = counterMap;
        this.userCounterMap = userCounterMap;
    }

    @Override
    public void start() {
        getWebAppClient();
        getHealthCheckClient();

        eventBus.consumer(INDEX, msg -> {
            final JsonArray response = repository.findAll()
                    .stream()
                    .map(Room::toJson)
                    .collect(JsonArray::new, JsonArray::add, JsonArray::add);
            msg.reply(response);
        });

        eventBus.<JsonObject>consumer(STORE, msg -> {
            final JsonObject body = msg.body();
            final Room room = Room.fromJson(body.getJsonObject("request"));

            try {
                final Long id = repository.save(room);
                room.setId(id);
                counterMap.put(room, 0L);
                csMap.put(room, Optional.empty());
                msg.reply(room.toJson());
            } catch (final Throwable e) {
                System.err.println(e.getMessage());
                msg.fail(500, e.getMessage());
            }
        });

        eventBus.<JsonObject>consumer(SHOW, msg -> {
            final JsonObject body = msg.body();
            final JsonObject params = body.getJsonObject("params");

            final long id = Long.parseLong(params.getString("id"));
            System.out.println("Room id: " + id);
            try {
                final JsonObject response = repository.findById(id)
                        .map(Room::toJson).get();
                msg.reply(response);
            } catch (NoSuchElementException e) {
                msg.fail(500, "Could not find room with ID " + id);
            }
        });

        eventBus.<JsonObject>consumer(UPDATE, msg -> {
            final JsonObject body = msg.body();
            final JsonObject params = body.getJsonObject("params");
            final Room room = Room.fromJson(body.getJsonObject("request"));

            final long id = Long.parseLong(params.getString("id"));

            try {
                final Room selectedRoom = repository.findById(id).get();
                selectedRoom.setName(room.getName());
                repository.save(selectedRoom);

                final JsonObject response = selectedRoom.toJson();
                msg.reply(response);
            } catch (NoSuchElementException e) {
                msg.fail(500, "Could not find room with ID " + id);
            }
        });

        eventBus.<JsonObject>consumer(DESTROY, msg -> {
            final JsonObject body = msg.body();
            final JsonObject params = body.getJsonObject("params");

            final long id = Long.parseLong(params.getString("id"));

            try {
                final Room room = repository.findById(id).get();
                csMap.remove(room);
                counterMap.remove(room);
                repository.deleteById(id);
                msg.reply(new JsonObject().put("id", id));
            } catch (NoSuchElementException e) {
                msg.fail(500, "Could not find room with ID " + id);
            }
        });

        eventBus.<JsonObject>consumer(JOIN, msg -> {
            final JsonObject body = msg.body();
            final JsonObject request = body.getJsonObject("request");
            final JsonObject params = body.getJsonObject("params");

            final long roomId = Long.parseLong(params.getString("roomId"));
            final User user = User.fromJson(request);

            try {
                final Room room = repository.findById(roomId).get();
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
                repository.addUser(room, user);
                msg.reply(response);
            } catch (final NoSuchElementException e) {
                msg.fail(500, "Could not find room with ID " + roomId);
            }
        });

        eventBus.<JsonObject>consumer(LEAVE, msg -> {
            final JsonObject body = msg.body();
            final JsonObject params = body.getJsonObject("params");

            final long roomId = Long.parseLong(params.getString("roomId"));
            final long userId = Long.parseLong(params.getString("userId"));

            try {
                final Room room = repository.findById(roomId).get();
                try {
                    final User user = repository.findUserById(room, userId).get();
                    repository.removeUser(room, user);
                    msg.reply(new JsonObject());
                } catch (final NoSuchElementException e) {
                    msg.fail(500, "Could not find user with ID " + userId);
                }
            } catch (final NoSuchElementException e) {
                msg.fail(500, "Could not find room with ID " + roomId);
            }
        });

        eventBus.<JsonObject>consumer(MESSAGES, msg -> {
            final JsonObject body = msg.body();
            final JsonObject request = body.getJsonObject("request");
            final JsonObject params = body.getJsonObject("params");

            final long roomId = Long.parseLong(params.getString("roomId"));

            System.out.println("Room ID: " + roomId);

            final User user = User.fromJson(request.getJsonObject("user"));

            System.out.println("User: " + user);

            try {
                final Room room = repository.findById(roomId).get();
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
                request.put("globalCounter", counterMap.get(room)).put("userClock", this.userCounterMap.get(room).get(user));
                msg.reply(request);
            } catch (final NoSuchElementException e) {
                msg.fail(500, "Could not find room with ID " + roomId);
            }
        });

        eventBus.<JsonObject>consumer(STATUSCS, msg -> {
            final JsonObject body = msg.body();
            final JsonObject params = body.getJsonObject("params");
            final long roomId = Long.parseLong(params.getString("roomId"));

            try {
                final Room room = repository.findById(roomId).get();
                final JsonObject response = new JsonObject();

                response.put("held", false);
                response.putNull("user");

                csMap.get(room).ifPresent(user -> {
                    response.put("held", true);
                    response.put("user", user.toJson());
                });

                msg.reply(response);
            } catch (final NoSuchElementException e) {
                msg.fail(500, "Could not find room with ID " + roomId);
            }
        });

        eventBus.<JsonObject>consumer(ENTERCS, msg -> {
            final JsonObject body = msg.body();
            final JsonObject request = body.getJsonObject("request");
            final JsonObject params = body.getJsonObject("params");

            final long roomId = Long.parseLong(params.getString("roomId"));
            final User user = User.fromJson(request);

            try  {
                final Room room = repository.findById(roomId).get();
                final Optional<User> csUser = csMap.get(room);

                if (csUser.isPresent()) {
                    throw new IllegalStateException("Critical section is already held by " + csUser.get().getName());
                }
                csMap.put(room, Optional.of(user));
                final long tid = vertx.setTimer(CRITICAL_SECTION_TIMEOUT, v -> {
                    csMap.put(room, Optional.empty());
                    timerIdMap.put(room, OptionalLong.empty());
                    checkHealth("webapp", () -> webAppClient.post("/api/messages")
                            .sendJson(room, ar -> {
                                if (ar.succeeded()) {
                                    System.out.println("Sent timeout expired");
                                } else {
                                    System.err.println("Could not send timeout expired: " + ar.cause().getMessage());
                                }
                            }));
                });
                timerIdMap.put(room, OptionalLong.of(tid));
                msg.reply(new JsonObject());
            } catch (final NoSuchElementException e) {
                msg.fail(500, "Could not find room with ID " + roomId);
            }
        });

        eventBus.<JsonObject>consumer(EXITCS, msg -> {
            final JsonObject body = msg.body();
            final JsonObject params = body.getJsonObject("params");

            final long roomId = Long.parseLong(params.getString("roomId"));
            final long userId = Long.parseLong(params.getString("userId"));

            try {
                final Room room = repository.findById(roomId).get();
                try {
                    final User user = repository.findUserById(room, userId).get();
                    final Optional<User> csUserOpt = csMap.get(room);
                    if (!csUserOpt.isPresent()) {
                        throw new IllegalStateException("Critical section is not held by a user");
                    } else {
                        if (csUserOpt.get().equals(user)) {
                            csMap.put(room, Optional.empty());
                            timerIdMap.get(room).ifPresent(vertx::cancelTimer);
                            timerIdMap.put(room, OptionalLong.empty());
                        } else {
                            msg.fail(500, "The user who tried to release the critical section is not the user who acquired it");
                            return;
                        }
                    }
                    msg.reply(new JsonObject());
                } catch (final NoSuchElementException e) {
                    msg.fail(500, "Could not find user with ID " + userId);
                }
            } catch (NoSuchElementException e) {
                msg.fail(500, "Could not find room with ID " + roomId);
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
