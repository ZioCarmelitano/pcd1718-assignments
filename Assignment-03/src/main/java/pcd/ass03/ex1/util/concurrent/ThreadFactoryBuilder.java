package pcd.ass03.ex1.util.concurrent;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static pcd.ass03.ex1.util.Preconditions.checkNotNull;

public class ThreadFactoryBuilder {

    private static final AtomicLong THREAD_FACTORY_COUNTER = new AtomicLong();

    public static final long DEFAULT_STACK_SIZE = 0L; // Stack size ignored
    public static final boolean DEFAULT_DAEMON = false;
    public static final int DEFAULT_PRIORITY = Thread.NORM_PRIORITY;

    private Supplier<String> name;
    private ThreadGroup threadGroup;
    private long stackSize;

    private ClassLoader contextClassLoader;
    private boolean daemon;
    private int priority;
    private UncaughtExceptionHandler uncaughtExceptionHandler;

    public ThreadFactoryBuilder() {
        stackSize = DEFAULT_STACK_SIZE;

        daemon = DEFAULT_DAEMON;
        priority = DEFAULT_PRIORITY;
    }

    private String getName() {
        return name == null ? null : name.get();
    }

    private ThreadGroup getThreadGroup() {
        return threadGroup;
    }

    private long getStackSize() {
        return stackSize;
    }

    private ClassLoader getContextClassLoader() {
        return contextClassLoader;
    }

    private boolean isDaemon() {
        return daemon;
    }

    private int getPriority() {
        return priority;
    }

    private UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return uncaughtExceptionHandler;
    }

    public ThreadFactoryBuilder setName(final Supplier<? extends String> name) {
        this.name = requireNonNull(name)::get;
        return this;
    }

    public ThreadFactoryBuilder setName(final String name) {
        return setName(() -> name);
    }

    public ThreadFactoryBuilder setThreadGroup(final ThreadGroup group) {
        this.threadGroup = group;
        return this;
    }

    public ThreadFactoryBuilder setStackSize(final long stackSize) {
        this.stackSize = stackSize;
        return this;
    }

    public ThreadFactoryBuilder setDaemon(final boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    public ThreadFactoryBuilder setContextClassLoader(final ClassLoader contextClassLoader) {
        this.contextClassLoader = contextClassLoader;
        return this;
    }

    public ThreadFactoryBuilder setPriority(final int priority) {
        this.priority = priority;
        return this;
    }

    public ThreadFactoryBuilder setUncaughtExceptionHandler(final UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
        return this;
    }

    public ThreadFactory build() {
        final AtomicLong threadCounter = new AtomicLong();

        final ThreadGroup threadGroup = getThreadGroup();
        final String name = getName() == null ? defaultThreadName(threadCounter) : getName();
        final long stackSize = getStackSize() < 0 ? getStackSize() : DEFAULT_STACK_SIZE;
        final ClassLoader contextClassLoader = getContextClassLoader();
        final boolean daemon = isDaemon();
        final int priority = getPriority();
        final UncaughtExceptionHandler uncaughtExceptionHandler = getUncaughtExceptionHandler();

        return action -> {
            checkNotNull(action, "action");

            final Thread thread = new Thread(
                    threadGroup,
                    action,
                    name,
                    stackSize);

            if (contextClassLoader != null) {
                thread.setContextClassLoader(getContextClassLoader());
            }

            thread.setDaemon(daemon);
            thread.setPriority(priority);
            thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);

            return thread;
        };
    }

    private static String defaultThreadName(final AtomicLong threadCounter) {
        return "ThreadFactory-" + THREAD_FACTORY_COUNTER.getAndIncrement() + "-Thread-" + threadCounter.getAndIncrement();
    }

}
