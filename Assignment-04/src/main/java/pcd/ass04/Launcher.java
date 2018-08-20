package pcd.ass04;

import pcd.ass04.util.Deployer;

import java.net.InetAddress;

final class Launcher {

    public static void main(String... args) throws Exception {
        final String serviceName = args[0];

        final String host = InetAddress.getLocalHost().getHostAddress();
        final int port = Integer.parseInt(args[1]);

        Deployer.deploy(serviceName, host, port);
    }

}
