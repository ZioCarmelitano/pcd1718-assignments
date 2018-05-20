package pcd.ass01.util;

public class Preconditions {

    public static void checkNotNull(Object o, String label) {
        checkArgument(o != null, "%s is null", label);
    }

    public static void checkNotNulls(Object[] a, String label) {
        checkNotNull(a, label);
        for (int i = 0; i < a.length; i++)
            checkNotNull(a[i], label + "[" + i + "]");
    }

    public static void checkLength(Object[] a, int expectedLength, String label) {
        checkNotNull(a, label);
        checkArgument(a.length == expectedLength, "%s.length(%d) != %d", label, a.length, expectedLength);
    }

    public static void checkNonNegative(final int x, final String label) {
        checkArgument(x >= 0, "%s(%d) < 0", label, x);
    }

    public static void checkPositive(final int x, final String label) {
        checkArgument(x > 0, "%s(%d) <= 0", label, x);
    }

    public static void checkArgument(boolean expression) {
        checkArgument(expression, "");
    }

    private static void checkArgument(boolean expression, String message, Object... args) {
        if (!expression) {
            throw new IllegalArgumentException(String.format(message, args));
        }
    }

    public static void checkState(boolean expression) {
        checkArgument(expression, "");
    }

    public static void checkState(boolean expression, String message, Object... args) {
        if (!expression) {
            throw new IllegalStateException(String.format(message, args));
        }
    }

    private Preconditions() {
    }

}
