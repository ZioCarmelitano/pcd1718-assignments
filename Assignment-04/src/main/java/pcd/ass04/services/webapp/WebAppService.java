package pcd.ass04.services.webapp;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;

import static pcd.ass04.util.ServiceDiscoveryUtils.getWebClient;

public final class WebAppService extends AbstractVerticle {

    private ServiceDiscovery discovery;
    private Record record;

    private String host;
    private int port;

    private WebClient roomClient;
    private WebClient userClient;

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

        getWebClient(vertx, discovery, 10_000, new JsonObject().put("name", "room-" +
                "service"), ar -> {
            if (ar.succeeded()) {
                roomClient = ar.result();
                System.out.println("Got room WebClient");
            } else {
                System.err.println("Could not retrieve room client: " + ar.cause().getMessage());
            }
        });

        getWebClient(vertx, discovery, 10_000, new JsonObject().put("name", "user-service"), ar -> {
            if (ar.succeeded()) {
                userClient = ar.result();
                System.out.println("Got user WebClient");
            } else {
                System.err.println("Could not retrieve user client: " + ar.cause().getMessage());
            }
        });

        final Router router = Router.router(vertx);
        final Router apiRouter = Router.router(vertx);

        router.mountSubRouter("/api", apiRouter);

        apiRouter.post("/messages")
                .consumes("application/json")
                .produces("application/json")
                .handler(this::handleMessage);

        apiRouter.route("/eventbus/*").handler(sockJSHandler());

        vertx.eventBus().consumer("chat.to.server", msg -> {
            JsonObject message = (JsonObject) msg;
            JsonObject request = message.getJsonObject("request");
            switch (message.getString("type")) {
                case "newUser":
                    userClient.post("/users").sendJsonObject(request, response -> {
                        if (response.succeeded()) {
                            System.out.println("Message (newUser) sent correctly");
                            vertx.eventBus().publish("chat.to.client.newUser", response.result().bodyAsJsonObject());
                        } else {
                            System.out.println("Error, message (newUser) was not sent correctly");
                        }
                    });
                    break;
                case "deleteUser":
                    userClient.delete("/users/" + request.getJsonObject("user").getString("id")).send(response -> {
                        if (response.succeeded()) {
                            System.out.println("Message (deleteUser) sent correctly");
                            vertx.eventBus().publish("chat.to.client.deleteUser", request);
                        } else {
                            System.out.println("Error, message (deleteUser) was not sent correctly");
                        }
                    });
                    break;
                case "getRooms":
                    roomClient.get("/rooms").send(response -> {
                    if (response.succeeded()) {
                        System.out.println("Message (getRooms) sent correctly");
                        vertx.eventBus().publish("chat.to.client.rooms", response.result().bodyAsJsonArray());
                    } else {
                        System.out.println("Error, message (getRooms) was not sent correctly");
                    }
                });
                    break;
                case "newRoom":
                    roomClient.post("/rooms").send(response -> {
                        if (response.succeeded()) {
                            System.out.println("Message (newRoom) sent correctly");
                            vertx.eventBus().publish("chat.to.client.newRoom", response.result().bodyAsJsonObject());
                        } else {
                            System.out.println("Error, message (newRoom) was not sent correctly");
                        }
                    });
                    break;
                case "getRoom":
                    roomClient.get("/rooms/" + request.getString("roomId")).send(response -> {
                        if (response.succeeded()) {
                            System.out.println("Message (getRoom) sent correctly");
                            vertx.eventBus().publish("chat.to.client.getRoom", response.result().bodyAsJsonObject());
                        } else {
                            System.out.println("Error, message (getRoom) was not sent correctly");
                        }
                    });
                    break;
                case "deleteRoom":
                    roomClient.delete("/rooms/" + request.getString("roomId")).send(response -> {
                        if (response.succeeded()) {
                            System.out.println("Message (deleteRoom) sent correctly");
                            vertx.eventBus().publish("chat.to.client.deleteRoom", request);
                        } else {
                            System.out.println("Error, message (deleteRoom) was not sent correctly");
                        }
                    });
                    break;
                case "addUserToRoom":
                    roomClient.post("/rooms/" + request.getString("roomId") + "/join").sendJsonObject(request, response -> {
                        if (response.succeeded()) {
                            System.out.println("Message (addUserToRoom) sent correctly");
                            vertx.eventBus().publish("chat.to.client.joinRoom", response.result().bodyAsJsonObject());
                        } else {
                            System.out.println("Error, message (addUserToRoom) was not sent correctly");
                        }
                    });
                    break;
                case "exitUserFromRoom":
                    roomClient.delete("/rooms/" + request.getString("roomId") + "/leave/" + request.getJsonObject("user").getLong("id")).send(response -> {
                        if (response.succeeded()) {
                            System.out.println("Message (exitUserFromRoom) sent correctly");
                            vertx.eventBus().publish("chat.to.client.leaveRoom", request);
                        } else {
                            System.out.println("Error, message (exitUserFromRoom) was not sent correctly");
                        }
                    });
                    break;
                case "saveMessageInRoom":
                    roomClient.post("/rooms/" + request.getString("roomId") + "/messages").sendJsonObject(request, response -> {
                        if (response.succeeded()) {
                            System.out.println("Message (saveMessageInRoom) sent correctly");
                            vertx.eventBus().publish("chat.to.client.newMessage", response.result().bodyAsJsonObject());
                        } else {
                            System.out.println("Error, message (saveMessageInRoom) was not sent correctly");
                        }
                    });
                    break;
                case "enterCriticalSection":
                    roomClient.post("/rooms/" + request.getString("roomId") + "/cs/enter").sendJsonObject(request.getJsonObject("user"), response -> {
                        if (response.succeeded()) {
                            System.out.println("Message (enterCriticalSection) sent correctly");
                            vertx.eventBus().publish("chat.to.client.enterCS", request);
                        } else {
                            System.out.println("Error, message (enterCriticalSection) was not sent correctly");
                        }
                    });
                    break;
                case "exitCriticalSection":
                    roomClient.delete("/rooms/" + request.getString("roomId") + "/cs/exit/" + request.getJsonObject("user").getString("id")).send(response -> {
                        if (response.succeeded()) {
                            System.out.println("Message (exitCriticalSection) sent correctly");
                            vertx.eventBus().publish("chat.to.client.exitCS", request);
                        } else {
                            System.out.println("Error, message (exitCriticalSection) was not sent correctly");
                        }
                    });
                    break;
                    default:
                        System.out.println("The type of the message ("+ message.getString("type") +") has not been recognized");
                        break;
            }
        });

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(port, host, ar -> {
                    if (ar.succeeded()) {
                        discovery.publish(HttpEndpoint.createRecord("gui-service", host, port, "/api"), ar1 -> {
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

    private SockJSHandler sockJSHandler() {
        // Allow events for the designated addresses in/out of the event bus bridge
        BridgeOptions opts = new BridgeOptions()
                .addInboundPermitted(new PermittedOptions().setAddress("chat.to.server"))
                .addOutboundPermitted(new PermittedOptions().setAddress("chat.to.client.newUser"))
                .addOutboundPermitted(new PermittedOptions().setAddress("chat.to.client.deleteUser"))
                .addOutboundPermitted(new PermittedOptions().setAddress("chat.to.client.rooms"))
                .addOutboundPermitted(new PermittedOptions().setAddress("chat.to.client.newRoom"))
                .addOutboundPermitted(new PermittedOptions().setAddress("chat.to.client.getRoom"))
                .addOutboundPermitted(new PermittedOptions().setAddress("chat.to.client.deleteRoom"))
                .addOutboundPermitted(new PermittedOptions().setAddress("chat.to.client.joinRoom"))
                .addOutboundPermitted(new PermittedOptions().setAddress("chat.to.client.leaveRoom"))
                .addOutboundPermitted(new PermittedOptions().setAddress("chat.to.client.newMessage"))
                .addOutboundPermitted(new PermittedOptions().setAddress("chat.to.client.deleteRoom"))
                .addOutboundPermitted(new PermittedOptions().setAddress("chat.to.client.enterCS"))
                .addOutboundPermitted(new PermittedOptions().setAddress("chat.to.client.exitCS"))

                ;

        // Create the event bus bridge and add it to the router.
        return SockJSHandler.create(vertx).bridge(opts);
    }

    private void handleMessage(RoutingContext ctx) {
        vertx.eventBus().publish("chat.to.client", ctx.getBody());
    }

}
