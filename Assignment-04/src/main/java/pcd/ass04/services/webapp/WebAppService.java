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
            switch (message.getString("type")) {
                case "getUsers":
                    userClient.get("/users");
                    break;
                case "newUser":
                    userClient.post("/users");
                    break;
                case "getUser":
                    userClient.get("/users/" + message.getString("id"));
                    break;
                case "deleteUser":
                    userClient.delete("/users/" + message.getString("id"));
                    break;
                case "modifyUser":
                    userClient.put("/users/" + message.getString("id"));
                    break;
                case "getRooms":
                    roomClient.get("/rooms");
                    break;
                case "newRoom":
                    roomClient.post("/rooms");
                    break;
                case "getRoom":
                    roomClient.get("/rooms/" + message.getString("id"));
                    break;
                case "deleteRoom":
                    roomClient.delete("/rooms/" + message.getString("id"));
                    break;
                case "modifyRoom":
                    roomClient.put("/rooms/" + message.getString("id"));
                    break;
                case "addUserToRoom":
                    roomClient.post("/rooms/" + message.getString("id") + "/join").sendJsonObject(message.getJsonObject("user"), response -> {
                        if (response.succeeded()) {
                            System.out.println("Message (addUserToRoom) sent correctly");
                            vertx.eventBus().publish("chat.to.client", message);
                        } else {
                            System.out.println("Error, message (addUserToRoom) was not sent correctly");
                        }
                    });
                    break;
                case "exitUserFromRoom":
                    roomClient.delete("/rooms/" + message.getString("id") + "/leave/" + message.getString("idUser")).send(response -> {
                        if (response.succeeded()) {
                            System.out.println("Message (exitUserFromRoom) sent correctly");
                            vertx.eventBus().publish("chat.to.client", message);
                        } else {
                            System.out.println("Error, message (exitUserFromRoom) was not sent correctly");
                        }
                    });
                    break;
                case "saveMessageInRoom":
                    roomClient.post("/rooms/" + message.getString("id") + "/messages").sendJsonObject(message.getJsonObject("message"), response -> {
                        if (response.succeeded()) {
                            System.out.println("Message (saveMessageInRoom) sent correctly");
                            vertx.eventBus().publish("chat.to.client", message);
                        } else {
                            System.out.println("Error, message (saveMessageInRoom) was not sent correctly");
                        }
                    });
                    break;
                case "isCriticalSection":
                    roomClient.get("/rooms/" + message.getString("id") + "/cs");
                    break;
                case "enterCriticalSection":
                    roomClient.get("/rooms/" + message.getString("id") + "/cs/enter").sendJsonObject(message.getJsonObject("user"), response -> {
                        if (response.succeeded()) {
                            System.out.println("Message (enterCriticalSection) sent correctly");
                            vertx.eventBus().publish("chat.to.client", message);
                        } else {
                            System.out.println("Error, message (enterCriticalSection) was not sent correctly");
                        }
                    });
                    break;
                case "exitCriticalSection":
                    roomClient.delete("/rooms/" + message.getString("id") + "/cs/exit/" + message.getString("idUser")).send(response -> {
                        if (response.succeeded()) {
                            System.out.println("Message (exitCriticalSection) sent correctly");
                            vertx.eventBus().publish("chat.to.client", message);
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
                .addOutboundPermitted(new PermittedOptions().setAddress("chat.to.client"));

        // Create the event bus bridge and add it to the router.
        return SockJSHandler.create(vertx).bridge(opts);
    }

    private void handleMessage(RoutingContext ctx) {
        vertx.eventBus().publish("chat.to.client", ctx.getBody());
    }

}
