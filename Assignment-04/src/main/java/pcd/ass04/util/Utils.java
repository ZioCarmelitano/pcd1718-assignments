package pcd.ass04.util;

import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

final class Utils {

    public static void run(Lock lock, Runnable operation) {
        get(lock, () -> {
            operation.run();
            return null;
        });
    }

    public static <T> T get(Lock lock, Supplier<? extends T> operation) {
        try {
            lock.lock();
            return operation.get();
        } finally {
            lock.unlock();
        }
    }

    private Utils() {
    }

}
