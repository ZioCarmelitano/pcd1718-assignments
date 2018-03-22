package pcd.ass01.util.time;

import java.util.concurrent.TimeUnit;

public interface StopwatchFactory {

    Stopwatch stopwatch(final TimeUnit timeUnit);

    Stopwatch stopwatch(final TimeUnit timeUnit, final boolean started);

    Stopwatch startedStopwatch(final TimeUnit timeUnit);

    Stopwatch unstartedStopwatch(final TimeUnit timeUnit);

}
