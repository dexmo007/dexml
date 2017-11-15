package com.dexmohq.dexml;

import com.dexmohq.dexml.format.XmlFormat;

public class AnnotatedFormat<T> implements XmlFormat<T> {

    private final AnnotatedValueReads<T> reads;
    private final AnnotatedValueWrites<T> writes;

    public AnnotatedFormat(Class<T> clazz) {
        this.reads = new AnnotatedValueReads<>(clazz);
        this.writes = AnnotatedValueWrites.create(clazz);
    }

    @Override
    public T read(String s) {
        return reads.read(s);
    }

    @Override
    public String write(T t) {
        return writes.write(t);
    }
}
