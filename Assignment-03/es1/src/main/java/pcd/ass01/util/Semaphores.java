package pcd.ass01.util;

import java.util.concurrent.Semaphore;

import static pcd.ass01.util.Preconditions.checkNotNull;

public final class Semaphores {

    public static Semaphore fairSemaphore(final int permits) {
        return new Semaphore(permits, true);
    }

    public static Semaphore unfairSemaphore(final int permits) {
        return new Semaphore(permits, false);
    }

    public static void acquire(final Semaphore semaphore) {
        checkNotNull(semaphore, "semaphore");

        wrap(semaphore::acquire);
    }

    public static void acquire(final Semaphore semaphore, final int permits) {
        checkNotNull(semaphore, "semaphore");

        wrap(() -> semaphore.acquire(permits));
    }

    private static void wrap(final InterruptibleRunnable action) {
        checkNotNull(action, "action");

        final Thread ct = Thread.currentThread();
        try {
            action.run();
        } catch (InterruptedException e) {
            ct.interrupt();
        }
    }

    private interface InterruptibleRunnable {
        void run() throws InterruptedException;
    }

}
