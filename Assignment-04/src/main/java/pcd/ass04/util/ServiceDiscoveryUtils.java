package pcd.ass04.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;
import pcd.ass04.services.healthcheck.HealthCheckService;
import pcd.ass04.services.room.RoomService;
import pcd.ass04.services.user.UserService;
import pcd.ass04.services.webapp.WebAppService;

import java.util.function.Supplier;

public final class ServiceDiscoveryUtils {

    public static void getWebClientOnce(Vertx vertx, ServiceDiscovery discovery, long delay, JsonObject filter, Handler<AsyncResult<WebClient>> resultHandler) {
        getWebClient(vertx, discovery, delay, filter, true, resultHandler);
    }

    public static void getWebClient(Vertx vertx, ServiceDiscovery discovery, long delay, JsonObject filter, Handler<AsyncResult<WebClient>> resultHandler) {
        getWebClient(vertx, discovery, delay, filter, false, resultHandler);
    }

    private static void getWebClient(Vertx vertx, ServiceDiscovery discovery, long delay, JsonObject filter, boolean once, Handler<AsyncResult<WebClient>> resultHandler) {
        final Handler<Long> tidHandler = tid -> {
            HttpEndpoint.getWebClient(discovery, filter, ar -> {
                resultHandler.handle(ar);
                if (ar.succeeded()) {
                    vertx.cancelTimer(tid);
                }
            });
        };
        if (once) {
            vertx.setTimer(delay, tidHandler);
        } else {
            vertx.setPeriodic(delay, tidHandler);
        }
    }

    private ServiceDiscoveryUtils() {
    }

}
