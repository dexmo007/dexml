package com.dexmohq.dexml.format;

@FunctionalInterface
public interface XmlReads<T> {

    T read(String s);

}
