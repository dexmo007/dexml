package com.dexmohq.dexml.format;

@FunctionalInterface
public interface XmlWrites<T> {

    String write(T t);

}
