package com.dexmohq.dexml.util;

import com.dexmohq.dexml.exception.XmlConfigurationException;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IndexedProperties implements Properties {

    private final ArrayList<Property> list;

    public IndexedProperties(int initialCapacity) {
        this.list = new ArrayList<>(initialCapacity);
    }

    public IndexedProperties() {
        this(16);
    }

    public IndexedProperties(List<Property> list) {
        this.list = new ArrayList<>(list);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean containsName(String name) {
        return list.stream().anyMatch(p -> p.getName().equals(name));
    }

    @Override
    public Property getByName(String name) {
        return list.stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
    }

    @Override
    public Property getByIndex(int index) {
        return list.get(index);
    }

    @Override
    public void put(String xmlName, PropertyDescriptor descriptor, Field field, short nodeType, int forceIndex) {
        if (containsName(xmlName)) {
            throw new XmlConfigurationException("Duplicate names: " + xmlName);
        }
        list.add(new Property(xmlName, descriptor, field, nodeType, forceIndex));
    }

    @Override
    public int indexOf(String name) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public Set<String> names() {
        return list.stream().map(Property::getName).collect(Collectors.toSet());
    }

    @Override
    public Iterator<Property> iterator() {
        return list.iterator();
    }

    @Override
    public Stream<Property> stream() {
        return list.stream();
    }
}
