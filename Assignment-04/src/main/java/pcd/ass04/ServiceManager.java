package pcd.ass04;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.servicediscovery.ServiceDiscovery;
import pcd.ass04.util.ServiceDiscoveryUtils;

public class ServiceManager extends AbstractVerticle {

    private ServiceDiscovery discovery;

    private String host;
    private int port;
    private String serviceName;

    private WebClient client;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

        final JsonObject config = context.config();
        host = config.getString("host");
        port = config.getInteger("port");

        serviceName = config.getString("serviceName");
    }

    @Override
    public void start() {
        discovery = ServiceDiscovery.create(vertx);

        getWebClient();
    }

    @Override
    public void stop() throws Exception {
        discovery.close();
    }

    private void getWebClient() {
        ServiceDiscoveryUtils.getWebClient(vertx, discovery, 10_000, new JsonObject().put("name", serviceName + "-service"), ar -> {
            if (ar.succeeded()) {
                client = ar.result();
                System.out.println("Got " + serviceName + " WebClient");
            } else {
                System.err.println("Could not retrieve " + serviceName + " client: " + ar.cause().getMessage());
            }
        });
    }

}
