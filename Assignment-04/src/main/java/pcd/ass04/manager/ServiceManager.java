package pcd.ass04.manager;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.servicediscovery.ServiceDiscovery;
import pcd.ass04.util.ServiceDiscoveryUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;

import static pcd.ass04.util.ServiceDiscoveryUtils.getWebClientOnce;

public class ServiceManager extends AbstractVerticle {

    private ServiceDiscovery discovery;

    private int port;
    private String serviceName;

    private JsonObject filter;

    private WebClient client;

    private Process process;

    private enum Status {
        TRY_RETRIEVE,
        NOT_AVAILABLE,
        FETCHING,
        AVAILABLE
    }

    private Status status = Status.TRY_RETRIEVE;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

        final JsonObject config = context.config();
        port = config.getInteger("port");

        serviceName = config.getString("serviceName");
        filter = new JsonObject().put("name", serviceName + "-service");
    }

    @Override
    public void start() {
        discovery = ServiceDiscovery.create(vertx);

        vertx.setPeriodic(2_000, tid -> {
            switch (status) {
                case TRY_RETRIEVE:
                    getWebClientOnce(vertx, discovery, 10_000, filter, ar -> {
                        if (ar.succeeded()) {
                            client = ar.result();
                            System.out.println("Got " + serviceName + " WebClient");
                        } else {
                            status = Status.NOT_AVAILABLE;
                            System.err.println("Could not retrieve " + serviceName + " client: " + ar.cause().getMessage());
                        }
                    });
                    status = Status.FETCHING;
                    break;
                case FETCHING:
                    if (client != null) {
                        status = Status.AVAILABLE;
                    }
                    break;
                case AVAILABLE:
                    client.get("/health")
                            .send(ar -> {
                                if (ar.failed() || ar.result().statusCode() != 200) {
                                    status = Status.NOT_AVAILABLE;
                                    client = null;
                                    if (process != null) {
                                        process.destroyForcibly();
                                        process = null;
                                    }
                                }
                            });
                    break;
                case NOT_AVAILABLE:
                    deployService();
                    getWebClient();
                    status = Status.FETCHING;
                    break;
            }
        });
    }

    private void deployService() {
        try {
            final String separator = FileSystems.getDefault().getSeparator();
            process = new ProcessBuilder(
                    "java",
                    "-cp",
                    "build" + separator + "libs" + separator + "Assignment-04-1.0.jar",
                    "pcd.ass04.ServiceLauncher",
                    serviceName,
                    Integer.toString(port))
                    .start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void stop() throws Exception {
        discovery.close();
    }

    private void getWebClient() {
        ServiceDiscoveryUtils.getWebClient(vertx, discovery, 10_000, filter, ar -> {
            if (ar.succeeded()) {
                client = ar.result();
                System.out.println("Got " + serviceName + " WebClient");
            } else {
                System.err.println("Could not retrieve " + serviceName + " client: " + ar.cause().getMessage());
            }
        });
    }

}
