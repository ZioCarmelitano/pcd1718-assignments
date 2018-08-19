package pcd.ass04.services.webapp;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;

import static io.vertx.core.http.HttpMethod.*;
import static pcd.ass04.util.ServiceDiscoveryUtils.getWebClient;

public final class WebAppService extends AbstractVerticle {

    private static final String CHAT_TO_SERVER = "chat.to.server";
    private static final String CHAT_TO_CLIENT = "chat.to.client";

    private static final String NEW_USER = CHAT_TO_CLIENT + ".newUser";
    private static final String DELETE_USER = CHAT_TO_CLIENT + ".deleteUser";
    private static final String ROOMS = CHAT_TO_CLIENT + ".rooms";
    private static final String NEW_ROOM = CHAT_TO_CLIENT + ".newRoom";
    private static final String GET_ROOM = CHAT_TO_CLIENT + ".getRoom";
    private static final String DELETE_ROOM = CHAT_TO_CLIENT + ".deleteRoom";
    private static final String JOIN_ROOM = CHAT_TO_CLIENT + ".joinRoom";
    private static final String LEAVE_ROOM = CHAT_TO_CLIENT + ".leaveRoom";
    private static final String NEW_MESSAGE = CHAT_TO_CLIENT + ".newMessage";
    private static final String ENTER_CS = CHAT_TO_CLIENT + ".enterCS";
    private static final String EXIT_CS = CHAT_TO_CLIENT + ".exitCS";
    private static final String TIMEOUT_EXPIRED = CHAT_TO_CLIENT + ".timeoutExpired";

    private ServiceDiscovery discovery;
    private Record record;
    private EventBus eventBus;

    private String host;
    private int port;

    private WebClient roomClient;
    private WebClient userClient;
    private WebClient healthCheckClient;

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

        getHealthCheckClient();
        getRoomClient();
        getUserClient();

        final Router apiRouter = Router.router(vertx);

        final Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.route().handler(StaticHandler.create());

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

        apiRouter.route("/eventbus/*").handler(sockJSHandler());

        apiRouter.post("/messages").handler(this::messages);

        router.mountSubRouter("/api", apiRouter);

        final HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx);

        healthCheckHandler.register("health-check-procedure", future -> future.complete(Status.OK()));

        router.get("/health*")
                .produces("application/json")
                .handler(healthCheckHandler);

        eventBus = vertx.eventBus();

        eventBus.<JsonObject>consumer(CHAT_TO_SERVER, msg -> {
            final JsonObject message = msg.body();

            final String type = message.getString("type");
            final JsonObject request = message.getJsonObject("request");
            final Long roomId = getRoomId(request);
            final Long userId = getUserId(request);

            System.out.println("Request: " + request);

            switch (type) {
                case "newUser":
                    userClient.post("/api/users")
                            .sendJson(request, ar -> {
                                if (ar.succeeded()) {
                                    System.out.println("Message (newUser) sent correctly" + " " + ar.result().bodyAsString());
                                    final JsonObject response = ar.result().bodyAsJsonObject();
                                    eventBus.publish(NEW_USER, response);
                                } else {
                                    System.out.println("Error, message (newUser) was not sent correctly");
                                }
                            });
                    break;
                case "deleteUser":
                    userClient.delete("/api/users/" + userId)
                            .send(ar -> {
                                if (ar.succeeded()) {
                                    System.out.println("Message (deleteUser) sent correctly");
                                    eventBus.publish(DELETE_USER, request);
                                } else {
                                    System.out.println("Error, message (deleteUser) was not sent correctly");
                                }
                            });
                    break;
                case "rooms":
                    roomClient.get("/api/rooms")
                            .send(ar -> {
                                if (ar.succeeded()) {
                                    System.out.println("Message (getRooms) sent correctly");
                                    final JsonArray response = ar.result().bodyAsJsonArray();
                                    eventBus.publish(ROOMS, response);
                                } else {
                                    System.out.println("Error, message (rooms) was not sent correctly");
                                }
                            });
                    break;
                case "newRoom":
                    roomClient.post("/api/rooms")
                            .sendJson(request, ar -> {
                                if (ar.succeeded()) {
                                    System.out.println("Message (newRoom) sent correctly");
                                    final JsonObject response = ar.result().bodyAsJsonObject();
                                    System.out.println("response: " + response);
                                    eventBus.publish(NEW_ROOM, response);
                                } else {
                                    System.out.println("Error, message (newRoom) was not sent correctly");
                                }
                            });
                    break;
                case "getRoom":
                    roomClient.get("/api/rooms/" + roomId)
                            .send(ar -> {
                                if (ar.succeeded()) {
                                    System.out.println("Message (getRoom) sent correctly");
                                    final JsonObject response = ar.result().bodyAsJsonObject();
                                    eventBus.publish(GET_ROOM, response);
                                } else {
                                    System.out.println("Error, message (getRoom) was not sent correctly");
                                }
                            });
                    break;
                case "deleteRoom":
                    roomClient.delete("/api/rooms/" + roomId)
                            .send(ar -> {
                                if (ar.succeeded()) {
                                    System.out.println("Message (deleteRoom) sent correctly");
                                    eventBus.publish(DELETE_ROOM, request);
                                } else {
                                    System.out.println("Error, message (deleteRoom) was not sent correctly");
                                }
                            });
                    break;
                case "joinRoom":
                    roomClient.post("/api/rooms/" + roomId + "/join")
                            .sendJson(request.getJsonObject("user"), ar -> {
                                if (ar.succeeded()) {
                                    System.out.println("Message (joinRoom) sent correctly");
                                    eventBus.publish(JOIN_ROOM, request.mergeIn(ar.result().bodyAsJsonObject()));
                                } else {
                                    System.out.println("Error, message (addUserToRoom) was not sent correctly");
                                }
                            });
                    break;
                case "leaveRoom":
                    roomClient.delete("/api/rooms/" + roomId + "/leave/" + userId)
                            .send(ar -> {
                                if (ar.succeeded()) {
                                    System.out.println("Message (exitUserFromRoom) sent correctly");
                                    eventBus.publish(LEAVE_ROOM, request);
                                } else {
                                    System.out.println("Error, message (exitUserFromRoom) was not sent correctly");
                                }
                            });
                    break;
                case "newMessage":
                    roomClient.post("/api/rooms/" + roomId + "/messages")
                            .sendJson(request, ar -> {
                                if (ar.succeeded()) {
                                    System.out.println("Message (saveMessageInRoom) sent correctly");
                                    System.out.println("Response: " + ar.result().bodyAsString());
                                    eventBus.publish(NEW_MESSAGE, ar.result().bodyAsJsonObject()
                                            .put("user", request.getJsonObject("user"))
                                            .put("room", request.getJsonObject("room")));
                                } else {
                                    System.out.println("Error, message (saveMessageInRoom) was not sent correctly");
                                }
                            });
                    break;
                case "enterCS":
                    roomClient.post("/api/rooms/" + roomId + "/cs/enter")
                            .sendJson(request.getJsonObject("user"), ar -> {
                                if (ar.succeeded()) {
                                    System.out.println("Message (enterCriticalSection) sent correctly");
                                    eventBus.publish(ENTER_CS, request);
                                } else {
                                    System.out.println("Error, message (enterCriticalSection) was not sent correctly");
                                }
                            });
                    break;
                case "exitCS":
                    roomClient.delete("/api/rooms/" + roomId + "/cs/exit/" + userId).send(ar -> {
                        if (ar.succeeded()) {
                            System.out.println("Message (exitCriticalSection) sent correctly");
                            eventBus.publish(EXIT_CS, request);
                        } else {
                            System.out.println("Error, message (exitCriticalSection) was not sent correctly");
                        }
                    });
                    break;
                default:
                    System.out.println("The type of the message (" + message.getString("type") + ") has not been recognized");
                    break;
            }
        });

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(port, host, ar -> {
                    if (ar.succeeded()) {
                        discovery.publish(HttpEndpoint.createRecord("webapp-service", host, port, "/"), ar1 -> {
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

    private void messages(RoutingContext ctx) {
        eventBus.publish(TIMEOUT_EXPIRED, ctx.getBodyAsJson());
        ctx.response().end();
        System.out.println("Timeout expired");
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

    private SockJSHandler sockJSHandler() {
        // Allow events for the designated addresses in/out of the event bus bridge
        BridgeOptions opts = new BridgeOptions()
                .addInboundPermitted(new PermittedOptions().setAddress(CHAT_TO_SERVER))
                .addOutboundPermitted(new PermittedOptions().setAddress(NEW_USER))
                .addOutboundPermitted(new PermittedOptions().setAddress(DELETE_USER))
                .addOutboundPermitted(new PermittedOptions().setAddress(ROOMS))
                .addOutboundPermitted(new PermittedOptions().setAddress(NEW_ROOM))
                .addOutboundPermitted(new PermittedOptions().setAddress(GET_ROOM))
                .addOutboundPermitted(new PermittedOptions().setAddress(DELETE_ROOM))
                .addOutboundPermitted(new PermittedOptions().setAddress(JOIN_ROOM))
                .addOutboundPermitted(new PermittedOptions().setAddress(LEAVE_ROOM))
                .addOutboundPermitted(new PermittedOptions().setAddress(NEW_MESSAGE))
                .addOutboundPermitted(new PermittedOptions().setAddress(DELETE_ROOM))
                .addOutboundPermitted(new PermittedOptions().setAddress(ENTER_CS))
                .addOutboundPermitted(new PermittedOptions().setAddress(EXIT_CS))
                .addOutboundPermitted(new PermittedOptions().setAddress(TIMEOUT_EXPIRED));

        // Create the event bus bridge and add it to the router.
        return SockJSHandler.create(vertx).bridge(opts);
    }

    private void getRoomClient() {
        getWebClient(vertx, discovery, 10_000, new JsonObject().put("name", "room-service"), ar -> {
            if (ar.succeeded()) {
                roomClient = ar.result();
                System.out.println("Got room WebClient");
            } else {
                System.err.println("Could not retrieve room client: " + ar.cause().getMessage());
            }
        });
    }

    private void getUserClient() {
        getWebClient(vertx, discovery, 10_000, new JsonObject().put("name", "user-service"), ar -> {
            if (ar.succeeded()) {
                userClient = ar.result();
                System.out.println("Got user WebClient");
            } else {
                System.err.println("Could not retrieve user client: " + ar.cause().getMessage());
            }
        });
    }

    private void getHealthCheckClient() {
        getWebClient(vertx, discovery, 10_000, new JsonObject().put("name", "healthcheck-service"), ar -> {
            if (ar.succeeded()) {
                healthCheckClient = ar.result();
                System.out.println("Got healthcheck WebClient");
            } else {
                System.err.println("Could not retrieve healthcheck client: " + ar.cause().getMessage());
            }
        });
    }

}
