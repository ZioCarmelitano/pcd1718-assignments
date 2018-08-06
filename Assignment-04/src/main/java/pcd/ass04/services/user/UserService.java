package pcd.ass04.services.user;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.reactivex.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.Record;

public class UserService extends AbstractVerticle {

    private ServiceDiscovery discovery;
    private Record record;
    private String address;
    private int port;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

        final JsonObject config = context.config();
        address = config.getString("address");
        port = config.getInteger("port");
    }

    @Override
    public void start() {
        discovery = ServiceDiscovery.create(vertx);

        discovery.publish(HttpEndpoint.createRecord("user-service", address, port, "/api"), ar -> {
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
