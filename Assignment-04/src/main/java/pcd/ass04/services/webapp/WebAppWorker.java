package pcd.ass04.services.webapp;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.servicediscovery.ServiceDiscovery;
import pcd.ass04.ServiceVerticle;

import static pcd.ass04.services.webapp.Channels.*;

final class WebAppWorker extends ServiceVerticle {

    private WebClient roomClient;
    private WebClient userClient;
    private WebClient healthCheckClient;

    @Override
    public void start() throws Exception {

        final EventBus eventBus = vertx.eventBus();

        getRoomClient();
        getUserClient();
        getHealthCheckClient();

        eventBus.<JsonObject>consumer(NEW_USER, msg -> {
            this.checkHealth("user", () -> userClient.post("/api/users")
                    .sendJson(msg.body(), ar -> {
                        if (ar.succeeded()) {
                            System.out.println("Message (newUser) sent correctly" + " " + ar.result().bodyAsString());
                            final JsonObject response = ar.result().bodyAsJsonObject();
                            msg.reply(response);
                        } else {
                            System.out.println("Error, message (newUser) was not sent correctly");
                        }
                    }));
        });

        eventBus.<JsonObject>consumer(DELETE_USER, msg -> {
            final JsonObject request = msg.body();
            final Long userId = getUserId(request);

            this.checkHealth("user", () -> userClient.delete("/api/users/" + userId)
                    .send(ar -> {
                        if (ar.succeeded()) {
                            System.out.println("Message (deleteUser) sent correctly");
                            msg.reply(request);
                        } else {
                            System.out.println("Error, message (deleteUser) was not sent correctly");
                        }
                    }));
        });

        eventBus.<JsonObject>consumer(ROOMS, msg -> {
            this.checkHealth("room", () -> roomClient.get("/api/rooms")
                    .send(ar -> {
                        if (ar.succeeded()) {
                            System.out.println("Message (getRooms) sent correctly");
                            final JsonArray response = ar.result().bodyAsJsonArray();
                            msg.reply(response);
                        } else {
                            System.out.println("Error, message (rooms) was not sent correctly");
                        }
                    }));
        });

        eventBus.<JsonObject>consumer(NEW_ROOM, msg -> {
            this.checkHealth("room", () -> roomClient.post("/api/rooms")
                    .sendJson(msg.body(), ar -> {
                        if (ar.succeeded()) {
                            final JsonObject response = ar.result().bodyAsJsonObject();
                            System.out.println("Message (newRoom) sent correctly");
                            System.out.println("Response: " + response);
                            msg.reply(response);
                        } else {
                            System.out.println("Error, message (newRoom) was not sent correctly");
                        }
                    }));
        });

        eventBus.<JsonObject>consumer(ROOM, msg -> {
            final JsonObject request = msg.body();
            final Long roomId = getRoomId(request);

            this.checkHealth("room", () -> roomClient.get("/api/rooms/" + roomId)
                    .send(ar -> {
                        if (ar.succeeded()) {
                            System.out.println("Message (getRoom) sent correctly");
                            final JsonObject response = ar.result().bodyAsJsonObject();
                            msg.reply(response);
                        } else {
                            System.out.println("Error, message (getRoom) was not sent correctly");
                        }
                    }));
        });

        eventBus.<JsonObject>consumer(DELETE_ROOM, msg -> {
            final JsonObject request = msg.body();
            final Long roomId = getRoomId(request);

            this.checkHealth("room", () -> roomClient.delete("/api/rooms/" + roomId)
                    .send(ar -> {
                        if (ar.succeeded()) {
                            System.out.println("Message (deleteRoom) sent correctly");
                            msg.reply(msg.body());
                        } else {
                            System.out.println("Error, message (deleteRoom) was not sent correctly");
                        }
                    }));
        });

        eventBus.<JsonObject>consumer(JOIN, msg -> {
            final JsonObject request = msg.body();
            final Long roomId = getRoomId(request);

            this.checkHealth("room", () -> roomClient.post("/api/rooms/" + roomId + "/join")
                    .sendJson(request.getJsonObject("user"), ar -> {
                        if (ar.succeeded()) {
                            final JsonObject response = ar.result().bodyAsJsonObject();
                            System.out.println("Message (joinRoom) sent correctly");
                            msg.reply(request.mergeIn(response));
                        } else {
                            System.out.println("Error, message (addUserToRoom) was not sent correctly");
                        }
                    }));
        });

        eventBus.<JsonObject>consumer(LEAVE, msg -> {
            final JsonObject request = msg.body();
            final Long userId = getUserId(request);
            final Long roomId = getRoomId(request);

            this.checkHealth("room", () -> roomClient.delete("/api/rooms/" + roomId + "/leave/" + userId)
                    .send(ar -> {
                        if (ar.succeeded()) {
                            System.out.println("Message (exitUserFromRoom) sent correctly");
                            msg.reply(request);
                        } else {
                            System.out.println("Error, message (exitUserFromRoom) was not sent correctly");
                        }
                    }));
        });

        eventBus.<JsonObject>consumer(MESSAGES, msg -> {
            final JsonObject request = msg.body();
            final Long roomId = getRoomId(request);

            this.checkHealth("room", () -> roomClient.post("/api/rooms/" + roomId + "/messages")
                    .sendJson(request, ar -> {
                        if (ar.succeeded()) {
                            final JsonObject response = ar.result().bodyAsJsonObject();
                            System.out.println("Message (saveMessageInRoom) sent correctly");
                            System.out.println("Response: " + response);
                            msg.reply(response
                                    .put("user", request.getJsonObject("user"))
                                    .put("room", request.getJsonObject("room")));
                        } else {
                            System.out.println("Error, message (saveMessageInRoom) was not sent correctly");
                        }
                    }));
        });

        eventBus.<JsonObject>consumer(ENTERCS, msg -> {
            final JsonObject request = msg.body();
            final Long roomId = getRoomId(request);

            this.checkHealth("room", () -> roomClient.post("/api/rooms/" + roomId + "/cs/enter")
                    .sendJson(request.getJsonObject("user"), ar -> {
                        if (ar.succeeded()) {
                            System.out.println("Message (enterCriticalSection) sent correctly");
                            final JsonObject response = ar.result().bodyAsJsonObject();
                            if (response != null) {
                                request.mergeIn(response);
                            }
                            System.out.println("Response: " + request);
                            msg.reply(request);
                        } else {
                            System.out.println("Error, message (enterCriticalSection) was not sent correctly");
                        }
                    }));
        });

        eventBus.<JsonObject>consumer(EXITCS, msg -> {
            final JsonObject request = msg.body();
            final Long roomId = getRoomId(request);
            final Long userId = getUserId(request);

            this.checkHealth("room", () -> roomClient.delete("/api/rooms/" + roomId + "/cs/exit/" + userId).send(ar -> {
                if (ar.succeeded()) {
                    System.out.println("Message (exitCriticalSection) sent correctly");
                    final JsonObject response = ar.result().bodyAsJsonObject();
                    if (response != null)
                        request.mergeIn(response);
                    System.out.println("Response: " + request);
                    msg.reply(request);
                } else {
                    System.out.println("Error, message (exitCriticalSection) was not sent correctly");
                }
            }));
        });

    }

    private static Long getRoomId(JsonObject request) {
        if (request != null) {
            if (request.containsKey("roomId")) {
                return request.getLong("roomId");
            } else if (request.containsKey("room")) {
                final JsonObject room = request.getJsonObject("room");
                if (room.containsKey("id")) {
                    return room.getLong("id");
                }
            }
        }
        return null;
    }
    private static Long getUserId(JsonObject request) {
        if (request != null) {
            if (request.containsKey("userId")) {
                return request.getLong("userId");
            } else if (request.containsKey("user")) {
                final JsonObject user = request.getJsonObject("user");
                if (user.containsKey("id")) {
                    return user.getLong("id");
                }
            }
        }
        return null;
    }

    private void getRoomClient() {
        getWebClient(10_000, new JsonObject().put("name", "room-service"), ar -> {
            if (ar.succeeded()) {
                roomClient = ar.result();
                System.out.println("Got room WebClient");
            } else {
                System.err.println("Could not retrieve room client: " + ar.cause().getMessage());
            }
        });
    }

    private void getUserClient() {
        getWebClient(10_000, new JsonObject().put("name", "user-service"), ar -> {
            if (ar.succeeded()) {
                userClient = ar.result();
                System.out.println("Got user WebClient");
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
