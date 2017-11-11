package com.dexmohq.dexml.format;

public interface XmlFormat<T> extends XmlReads<T>, XmlWrites<T> {

    static <T> XmlFormat<T> fromReadsAndWrites(XmlReads<T> reads, XmlWrites<T> writes) {
        return new XmlFormat<T>() {
            @Override
            public T read(String s) {
                return reads.read(s);
            }

            @Override
            public String write(T t) {
                return writes.write(t);
            }
        };
    }

}
