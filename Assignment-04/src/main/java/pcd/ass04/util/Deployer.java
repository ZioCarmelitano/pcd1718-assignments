package pcd.ass04.util;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import pcd.ass04.services.healthcheck.HealthCheckService;
import pcd.ass04.services.room.RoomService;
import pcd.ass04.services.user.UserService;
import pcd.ass04.services.webapp.WebAppService;

import java.util.function.Supplier;

public final class Deployer {

    public static void deploy(String serviceName, String host, int port) {
        Vertx.clusteredVertx(new VertxOptions().setClustered(true), ar -> {
            if (ar.succeeded()) {
                final Vertx vertx = ar.result();
                vertx.deployVerticle(getService(serviceName), new DeploymentOptions()
                        .setConfig(new JsonObject()
                                .put("host", host)
                                .put("port", port)));
            }
        });
    }

    public static Supplier<Verticle> getService(String serviceName) {
        switch (serviceName) {
            case "room":
                return RoomService::new;
            case "user":
                return UserService::new;
            case "webapp":
                return WebAppService::new;
            case "healthcheck":
                return HealthCheckService::new;
            default:
                throw new IllegalArgumentException("Invalid service name: " + serviceName);
        }
    }

    private Deployer() {
    }

}
