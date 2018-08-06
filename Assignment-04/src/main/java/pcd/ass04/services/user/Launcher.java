package pcd.ass04.services.user;

import pcd.ass04.util.Deployer;

import java.net.InetAddress;

public final class Launcher {

    public static void main(String[] args) throws Exception {
        final String address = InetAddress.getLocalHost().getHostAddress();
        final int port = Integer.parseInt(args[0]);

        Deployer.deploy(address, port, UserService::new);
    }

}
