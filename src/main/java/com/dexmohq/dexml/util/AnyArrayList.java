package com.dexmohq.dexml.util;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Objects;

public class AnyArrayList implements Iterable {

    private final Object array;
    private final int length;

    public AnyArrayList(Object array) {
        if (!ArrayUtils.isArray(array))
            throw new IllegalArgumentException("Argument must be an array.");
        this.array = Objects.requireNonNull(array);
        this.length = Array.getLength(array);
    }

    @Override
    public Iterator iterator() {
        return new Iterator() {
            private int index = 0;
            @Override
            public boolean hasNext() {
                return index < length;
            }

            @Override
            public Object next() {
                return Array.get(array, index++);
            }
        };
    }
}
