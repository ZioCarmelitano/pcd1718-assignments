package pcd.ass04.services.webapp;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.servicediscovery.types.HttpEndpoint;
import pcd.ass04.ServiceVerticle;

import static io.vertx.core.http.HttpMethod.*;

public final class WebAppService extends ServiceVerticle {

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

    private String host;
    private int port;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

        final JsonObject config = context.config();
        host = config.getString("host");
        port = config.getInteger("port");
    }

    @Override
    public void start() {

        deployWorkers();

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

        eventBus.<JsonObject>consumer(CHAT_TO_SERVER, msg -> {
            final JsonObject message = msg.body();

            final String type = message.getString("type");
            final JsonObject request = message.getJsonObject("request");

            System.out.println("Request: " + request);

            switch (type) {
                case "newUser":
                    send(request, Channels.NEW_USER, NEW_USER);
                    break;
                case "deleteUser":
                    send(request, Channels.DELETE_USER, DELETE_USER);
                case "rooms":
                    send(request, Channels.ROOMS, ROOMS);
                    break;
                case "newRoom":
                    send(request, Channels.NEW_ROOM, NEW_ROOM);
                    break;
                case "getRoom":
                    send(request, Channels.ROOM, GET_ROOM);
                    break;
                case "deleteRoom":
                    send(request, Channels.DELETE_ROOM, DELETE_ROOM);
                    break;
                case "joinRoom":
                    send(request, Channels.JOIN, JOIN_ROOM);
                    break;
                case "leaveRoom":
                    send(request, Channels.LEAVE, LEAVE_ROOM);
                    break;
                case "newMessage":
                    send(request, Channels.MESSAGES, NEW_MESSAGE);
                    break;
                case "enterCS":
                    send(request, Channels.ENTERCS, ENTER_CS);
                    break;
                case "exitCS":
                    send(request, Channels.EXITCS, EXIT_CS);
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
                        publishRecord(HttpEndpoint.createRecord("webapp-service", host, port, "/"), ar1 -> {
                            if (ar1.succeeded()) {
                                System.out.println("Record published with success!");
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

    private void send(JsonObject request, String requestChannel, String responseChannel) {
        eventBus.send(requestChannel, request, ar -> {
            if (ar.succeeded()) {
                final Object response = ar.result().body();
                System.out.println("Response: " + response);
                eventBus.publish(responseChannel, response);
            } else {
                System.out.println("Error: " + ar.cause().getMessage());
            }
        });
    }

    private void deployWorkers() {
        final DeploymentOptions options = new DeploymentOptions()
                .setWorker(true)
                .setInstances(10);

        vertx.deployVerticle(WebAppWorker::new, options);
    }

    private void messages(RoutingContext ctx) {
        eventBus.publish(TIMEOUT_EXPIRED, ctx.getBodyAsJson());
        ctx.response().end();
        System.out.println("Timeout expired");
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

}
