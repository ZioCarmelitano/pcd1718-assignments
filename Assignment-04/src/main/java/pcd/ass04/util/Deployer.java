package pcd.ass04.util;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

import java.util.function.Supplier;

public final class Deployer {

    public static void deploy(Supplier<Verticle> verticleSupplier, String host, int port) {
        Vertx.clusteredVertx(new VertxOptions().setClustered(true), ar -> {
            if (ar.succeeded()) {
                final Vertx vertx = ar.result();
                vertx.deployVerticle(verticleSupplier, new DeploymentOptions()
                        .setConfig(new JsonObject()
                                .put("host", host)
                                .put("port", port)));
            }
        });
    }

    private Deployer() {
    }

}
