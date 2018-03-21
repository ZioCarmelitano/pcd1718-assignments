package pcd.ass01;

import java.util.Objects;

public final class Launcher {

    public static void main(String[] args) {

    }

    static {
        String path = Objects.requireNonNull(Launcher.class.getClassLoader()
                .getResource("/logging.properties"))
                .getFile();
        System.setProperty("java.util.logging.config.file", path);
    }

}
