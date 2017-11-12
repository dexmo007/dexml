package com.dexmohq.dexml.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class ArrayUtils {
    private ArrayUtils() {
    }

    public static <T> T[] concat(T[] first, T[] second) {
        final T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    private static final Set<Class<?>> primitiveArrayClasses = new HashSet<>(Arrays.asList(
            boolean[].class, byte[].class, short[].class, int[].class, long[].class, float[].class, double[].class
    ));

    public static boolean isPrimitiveArray(Class<?> clazz) {
        return clazz != null && primitiveArrayClasses.contains(clazz);
    }

    public static boolean isPrimitiveArray(Object o) {
        return o != null && isPrimitiveArray(o.getClass());
    }

    public static boolean isArray(Class<?> clazz) {
        return clazz != null && (isPrimitiveArray(clazz) || Object[].class.isAssignableFrom(clazz));
    }

    public static boolean isArray(Object o) {
        return o != null && isArray(o.getClass());
    }

    public static void main(String[] args) {
        System.out.println(isArray(new String[5]));
        System.out.println(isArray(new int[5]));
    }

}
