package pcd.ass04.util;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import pcd.ass04.manager.ServiceManager;
import pcd.ass04.services.healthcheck.HealthCheckService;
import pcd.ass04.services.room.RoomService;
import pcd.ass04.services.user.UserService;
import pcd.ass04.services.webapp.WebAppService;

import java.util.function.Supplier;

public final class Deployer {

    public static void deployManager(String serviceName, int port) {
        deploy(ServiceManager::new, new JsonObject()
                .put("port", port)
                .put("serviceName", serviceName));
    }

    public static void deployService(String serviceName, String host, int port) {
        deploy(getService(serviceName), new JsonObject()
                .put("host", host)
                .put("port", port));
    }

    private static void deploy(Supplier<Verticle> verticleSupplier, JsonObject config) {
        Vertx.clusteredVertx(new VertxOptions().setClustered(true), ar -> {
            if (ar.succeeded()) {
                final Vertx vertx = ar.result();
                vertx.deployVerticle(verticleSupplier, new DeploymentOptions()
                        .setConfig(config));
            } else {
                System.err.println("Could not start clustered Vert.x instance: " + ar.cause().getMessage());
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
