package pcd.ass04;

import io.vertx.core.Verticle;
import pcd.ass04.services.webapp.WebAppService;
import pcd.ass04.services.room.RoomService;
import pcd.ass04.services.user.UserService;
import pcd.ass04.util.Deployer;

import java.net.InetAddress;
import java.util.function.Supplier;

final class Launcher {

    public static void main(String... args) throws Exception {
        final String serviceName = args[0];

        final String host = InetAddress.getLocalHost().getHostAddress();
        final int port = Integer.parseInt(args[1]);

        Deployer.deploy(getService(serviceName), host, port);
    }

    private static Supplier<Verticle> getService(String serviceName) {
        switch (serviceName) {
            case "room":
                return RoomService::new;
            case "user":
                return UserService::new;
            case "webapp":
                return WebAppService::new;
            default:
                throw new IllegalArgumentException("Invalid service name: " + serviceName);
        }
    }

}
