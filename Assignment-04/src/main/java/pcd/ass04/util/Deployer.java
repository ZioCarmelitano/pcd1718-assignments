package pcd.ass04.util;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

import java.util.function.Supplier;

public final class Deployer {

    public static void deploy(String address, int port, Supplier<Verticle> verticleSupplier) {
        Vertx.clusteredVertx(new VertxOptions().setClustered(true), ar -> {
            if (ar.succeeded()) {
                final Vertx vertx = ar.result();
                vertx.deployVerticle(verticleSupplier, new DeploymentOptions()
                        .setConfig(new JsonObject()
                                .put("address", address)
                                .put("port", port)));
            }
        });
    }

    private Deployer() {
    }

}
