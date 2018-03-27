package pcd.ass01.util.time;

import java.util.concurrent.TimeUnit;

import static pcd.ass01.util.Preconditions.checkNotNull;

public final class TimeUtils {

    public static String toSIString(final TimeUnit unit) {
        checkNotNull(unit, "unit");
        switch (unit) {
            case NANOSECONDS:
                return "ns";
            case MICROSECONDS:
                return "\u03BCs";
            case MILLISECONDS:
                return "ms";
            case SECONDS:
                return "s";
            case MINUTES:
                return "m";
            case HOURS:
                return "h";
            case DAYS:
                return "d";
            default:
                throw new IllegalStateException("Unknown time unit: " + unit);
        }
    }

    private TimeUtils() {
    }

}
