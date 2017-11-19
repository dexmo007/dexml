package com.dexmohq.dexml.util;

import com.dexmohq.dexml.exception.XmlConfigurationException;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

public class AlphabeticProperties implements Properties {

    private final TreeMap<String, Property> map;

    public AlphabeticProperties() {
        this.map = new TreeMap<>();//alphabetic ordering by default
    }

    @Override
    public Property getByName(String name) {
        return map.get(name);
    }

    @Override
    public Property getByIndex(int index) {
        return map.values().stream().skip(index).findFirst().orElse(null);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean containsName(String name) {
        return map.containsKey(name);
    }

    @Override
    public void put(String xmlName, PropertyDescriptor descriptor, Field field, short nodeType, int forceIndex) {
        final Property property = new Property(xmlName, descriptor, field, nodeType, forceIndex);
        if (map.put(xmlName, property) != null) {
            throw new XmlConfigurationException("Duplicate names: " + xmlName);
        }
    }

    @Override
    public int indexOf(String name) {
        return map.get(name).getIndex();
    }

    @Override
    public Set<String> names() {
        return map.keySet();
    }

    @Override
    public Stream<Property> stream() {
        return map.values().stream();
    }

    @Override
    public Iterator<Property> iterator() {
        return map.values().iterator();
    }
}
