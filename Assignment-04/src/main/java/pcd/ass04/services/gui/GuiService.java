package pcd.ass04.services.gui;

import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.circuitbreaker.CircuitBreaker;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.reactivex.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.Record;
import pcd.ass04.util.ServiceDiscoveryUtils;

public class GuiService extends AbstractVerticle {

    private ServiceDiscovery discovery;
    private Record record;
    private String address;
    private int port;
    private WebClient guiClient;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

        address = context.config().getString("address");
        port = context.config().getInteger("port");
    }

    @Override
    public void start() {
        discovery = ServiceDiscovery.create(vertx);

        final CircuitBreaker circuitBreaker = CircuitBreaker.create("gui-circuit-breaker", vertx,
                new CircuitBreakerOptions()
                        .setMaxFailures(5) // number of failure before opening the circuit
                        .setTimeout(2000) // consider a failure if the operation does not succeed in time
                        .setFallbackOnFailure(true) // do we call the fallback on failure
                        .setResetTimeout(10000) // time spent in open state before attempting to re-try
        );

        ServiceDiscoveryUtils.getWebClient(discovery, circuitBreaker, new JsonObject().put("name", "room-service"), ar -> {
            if (ar.succeeded()) {
                guiClient = ar.result();
            } else {
                System.err.println("Could not retrieve room client: " + ar.cause().getMessage());
            }
        });

        ServiceDiscoveryUtils.getWebClient(discovery, circuitBreaker, new JsonObject().put("name", "user-service"), ar -> {
            if (ar.succeeded()) {
                final WebClient userClient = ar.result();
            } else {
                System.err.println("Could not retrieve user client: " + ar.cause().getMessage());
            }
        });

        discovery.publish(HttpEndpoint.createRecord("gui-service", address, port, "/api"), ar -> {
            if (ar.succeeded()) {
                record = ar.result();
            } else {
                System.err.println("Could not publish record: " + ar.cause().getMessage());
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

}
