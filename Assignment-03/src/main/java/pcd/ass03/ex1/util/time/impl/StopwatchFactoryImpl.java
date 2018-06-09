package pcd.ass03.ex1.util.time.impl;

import pcd.ass03.ex1.util.time.Stopwatch;
import pcd.ass03.ex1.util.time.StopwatchFactory;

import java.util.concurrent.TimeUnit;

public class StopwatchFactoryImpl implements StopwatchFactory {

    public static StopwatchFactoryImpl defaultInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public Stopwatch stopwatch(TimeUnit timeUnit) {
        return unstartedStopwatch(timeUnit);
    }

    @Override
    public Stopwatch stopwatch(TimeUnit timeUnit, boolean started) {
        return started ? startedStopwatch(timeUnit) : unstartedStopwatch(timeUnit);
    }

    @Override
    public Stopwatch startedStopwatch(TimeUnit timeUnit) {
        return stopwatch(timeUnit).start();
    }

    @Override
    public Stopwatch unstartedStopwatch(TimeUnit timeUnit) {
        return new StopwatchImpl(timeUnit);
    }

    private static final class Holder {
        static final StopwatchFactoryImpl INSTANCE = new StopwatchFactoryImpl();
    }

}
