package pcd.ass04.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.circuitbreaker.CircuitBreaker;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.reactivex.servicediscovery.types.HttpEndpoint;

public class ServiceDiscoveryUtils {

    public static void getWebClient(ServiceDiscovery discovery, CircuitBreaker circuitBreaker, JsonObject filter, Handler<AsyncResult<WebClient>> resultHandler) {
        circuitBreaker.<WebClient>execute(future -> {
            HttpEndpoint.getWebClient(discovery, filter, future.completer());
        }).setHandler(resultHandler);
    }

}
