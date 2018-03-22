package pcd.ass01.util;

import java.lang.reflect.Array;

public final class ArrayUtils {

    public static <T> T[] flatten(final T[][] arrays) {
        int length = 0;
        for (final T[] a : arrays)
            length += a.length;

        final T[] result = (T[]) Array.newInstance(arrays[0].getClass().getComponentType(), length);

        int offset = 0;
        for (final T[] a : arrays) {
            System.arraycopy(a, 0, result, offset, a.length);
            offset += a.length;
        }
        return result;
    }

    private ArrayUtils() {
    }

}