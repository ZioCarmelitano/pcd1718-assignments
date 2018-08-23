package pcd.ass04.manager;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import pcd.ass04.ServiceVerticle;

import com.julienviet.childprocess.Process;

import java.nio.file.FileSystems;
import java.util.Arrays;

public class ServiceManager extends ServiceVerticle {

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
        System.out.println(serviceName + " manager started!");

        vertx.setPeriodic(2_000, tid -> {
            switch (status) {
                case TRY_RETRIEVE:
                    getWebClientOnce(10_000, filter, ar -> {
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
                                        process.kill(true);
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
        final String separator = FileSystems.getDefault().getSeparator();

        Process.create(vertx, "java", Arrays.asList(
                "-cp",
                "build" + separator + "libs" + separator + "Assignment-04-1.0.jar",
                "pcd.ass04.ServiceLauncher",
                serviceName,
                Integer.toString(port)))
                .start(process -> {
                    process.stdout().handler(buffer -> System.out.println(serviceName + " process wrote: " + buffer));
                    System.out.println(serviceName + " process started, PID: " + process.pid());

                    this.process = process;
                });
    }

    private void getWebClient() {
        getWebClient(10_000, filter, ar -> {
            if (ar.succeeded()) {
                client = ar.result();
                System.out.println("Got " + serviceName + " WebClient");
            } else {
                System.err.println("Could not retrieve " + serviceName + " client: " + ar.cause().getMessage());
            }
        });
    }

}
