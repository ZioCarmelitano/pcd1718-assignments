package pcd.ass03.ex1.util;

import java.lang.reflect.Array;

final class ArrayUtils {

    public static <T> T[] flatten(final T[][] arrays) {
        int length = 0;
        for (final T[] a : arrays)
            length += a.length;

        final T[] result = newArray((Class<T>) arrays[0].getClass().getComponentType(), length);

        int offset = 0;
        for (final T[] a : arrays) {
            System.arraycopy(a, 0, result, offset, a.length);
            offset += a.length;
        }
        return result;
    }

    private static <T> T[] newArray(Class<? extends T> clazz, int length) {
        return (T[]) Array.newInstance(clazz, length);
    }

    private ArrayUtils() {
    }

}