package pcd.ass04.util;

import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

public final class Utils {

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
