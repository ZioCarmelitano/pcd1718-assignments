package pcd.ass04.services.healthcheck;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;

import static pcd.ass04.util.ServiceDiscoveryUtils.getWebClient;

public class HealthCheckService extends AbstractVerticle {

    private ServiceDiscovery discovery;
    private Record record;

    private WebClient webClient;

    private String host;
    private int port;

    private WebClient roomClient;
    private WebClient userClient;
    private WebClient webAppClient;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

        final JsonObject config = context.config();
        host = config.getString("host");
        port = config.getInteger("port");
    }

    @Override
    public void start() throws Exception {
        discovery = ServiceDiscovery.create(vertx);

        getRoomClient();
        getUserClient();
        getWebAppClient();

        final Router healthRouter = Router.router(vertx);

        final HealthCheckHandler roomServiceHealthCheckHandler = HealthCheckHandler.create(vertx);

        roomServiceHealthCheckHandler.register("room", this::roomHealthCheckProcedure);
        roomServiceHealthCheckHandler.register("webapp", this::webAppHealthCheckProcedure);

        healthRouter.get("/room*")
                .produces("application/json")
                .handler(roomServiceHealthCheckHandler);

        final HealthCheckHandler userServiceHealthCheckHandler = HealthCheckHandler.create(vertx);

        userServiceHealthCheckHandler.register("user", this::userHealthCheckProcedure);

        healthRouter.get("/user*")
                .produces("application/json")
                .handler(userServiceHealthCheckHandler);

        final HealthCheckHandler webAppServiceHealthCheckHandler = HealthCheckHandler.create(vertx);

        webAppServiceHealthCheckHandler.register("webapp", this::webAppHealthCheckProcedure);
        webAppServiceHealthCheckHandler.register("room", this::roomHealthCheckProcedure);
        webAppServiceHealthCheckHandler.register("user", this::userHealthCheckProcedure);

        healthRouter.get("/webapp*")
                .produces("application/json")
                .handler(webAppServiceHealthCheckHandler);

        final Router router = Router.router(vertx);

        router.mountSubRouter("/health", healthRouter);

        final HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx);

        healthCheckHandler.register("health-check-procedure", future -> future.complete(Status.OK()));

        router.get("/health*")
                .produces("application/json")
                .handler(healthCheckHandler);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(port, host, ar -> {
                    if (ar.succeeded()) {
                        discovery.publish(HttpEndpoint.createRecord("healthcheck-service", host, port, "/"), ar1 -> {
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

    private void roomHealthCheckProcedure(Future<Status> future) {
        if (roomClient == null) {
            future.complete(Status.KO());
        } else {
            roomClient.get("/health")
                    .send(ar -> {
                        if (ar.succeeded()) {
                            final int statusCode = ar.result().statusCode();
                            if (statusCode == 200) {
                                future.complete(Status.OK());
                            } else {
                                future.complete(Status.KO());
                            }
                        } else {
                            future.fail(ar.cause());
                        }
                    });
        }
    }

    private void userHealthCheckProcedure(Future<Status> future) {
        if (userClient == null) {
            future.complete(Status.KO());
        } else {
            userClient.get("/health")
                    .send(ar -> {
                        if (ar.succeeded()) {
                            final int statusCode = ar.result().statusCode();
                            if (statusCode == 200) {
                                future.complete(Status.OK());
                            } else {
                                future.complete(Status.KO());
                            }
                        } else {
                            future.fail(ar.cause());
                        }
                    });
        }
    }

    private void webAppHealthCheckProcedure(Future<Status> future) {
        if (webAppClient == null) {
            future.complete(Status.KO());
        } else {
            webAppClient.get("/health")
                    .send(ar -> {
                        if (ar.succeeded()) {
                            final int statusCode = ar.result().statusCode();
                            if (statusCode == 200) {
                                future.complete(Status.OK());
                            } else {
                                future.complete(Status.KO());
                            }
                        } else {
                            future.fail(ar.cause());
                        }
                    });
        }
    }

    @Override
    public void stop() throws Exception {
        discovery.close();
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

    private void getWebAppClient() {
        getWebClient(vertx, discovery, 10_000, new JsonObject().put("name", "webapp-service"), ar -> {
            if (ar.succeeded()) {
                webAppClient = ar.result();
                System.out.println("Got webapp WebClient");
            } else {
                System.err.println("Could not retrieve user client: " + ar.cause().getMessage());
            }
        });
    }

}
