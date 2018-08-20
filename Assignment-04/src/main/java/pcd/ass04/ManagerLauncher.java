package pcd.ass04;

import pcd.ass04.util.Deployer;

import java.net.InetAddress;

final class ManagerLauncher {

    public static void main(String... args) throws Exception {
        final String serviceName = args[0];

        final int port = Integer.parseInt(args[1]);

        Deployer.deployManager(serviceName, port);
    }

}
