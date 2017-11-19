package com.dexmohq.dexml.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.stream.Stream;

public interface Properties extends Iterable<Property> {

    Property getByName(String name);

    Property getByIndex(int index);

    int size();

    default boolean isEmpty() {
        return size() > 0;
    }

    boolean containsName(String name);

    default void put(String xmlName, PropertyDescriptor descriptor, Field field, short nodeType) {
        put(xmlName, descriptor, field, nodeType, size());
    }

    void put(String xmlName, PropertyDescriptor descriptor, Field field, short nodeType, int forceIndex);

    int indexOf(String name);

    Set<String> names();

    Stream<Property> stream();
}
