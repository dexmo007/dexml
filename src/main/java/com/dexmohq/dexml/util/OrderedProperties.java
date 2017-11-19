package com.dexmohq.dexml.util;

import com.dexmohq.dexml.exception.XmlConfigurationException;
import com.google.common.collect.Iterators;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OrderedProperties implements Properties {

    private final String[] ordering;
    private final Map<String, Property> ordered;
    private final Properties remaining;

    public OrderedProperties(String[] ordering, Properties remaining) {
        this.ordering = ordering;
        this.ordered = new TreeMap<>(new OrderedComparator(ordering));
        this.remaining = remaining;
    }

    @Override
    public Property getByName(String name) {
        final Property property = ordered.get(name);
        if (property == null) {
            return remaining.getByName(name);
        }
        return property;
    }

    @Override
    public Property getByIndex(int index) {
        if (index < ordered.size()) {
            return ordered.values().stream().skip(index).findFirst().orElse(null);
        }
        return remaining.getByIndex(index - ordered.size());
    }

    @Override
    public int size() {
        return ordered.size() + remaining.size();
    }

    @Override
    public boolean containsName(String name) {
        return ordered.containsKey(name) || remaining.containsName(name);
    }

    @Override
    public void put(String xmlName, PropertyDescriptor descriptor, Field field, short nodeType, int forceIndex) {
        if (ArrayUtils.contains(ordering, xmlName)) {
            if (ordered.put(xmlName, new Property(xmlName, descriptor, field, nodeType, forceIndex)) != null) {
                throw new XmlConfigurationException("Duplicate names: " + xmlName);
            }
            return;
        }
        remaining.put(xmlName, descriptor, field, nodeType, ordering.length + forceIndex);//todo addition necessary?
    }

    @Override
    public int indexOf(String name) {
        final Property ordered = this.ordered.get(name);
        if (ordered != null) {
            return ordered.getIndex();
        }
        return remaining.indexOf(name);
    }

    @Override
    public Set<String> names() {
        return Stream.concat(ordered.keySet().stream(), remaining.names().stream()).collect(Collectors.toSet());
    }

    @Override
    public Stream<Property> stream() {
        return Stream.concat(ordered.values().stream(), remaining.stream());
    }

    @Override
    public Iterator<Property> iterator() {
        return Iterators.concat(ordered.values().iterator(), remaining.iterator());
    }

    private static class OrderedComparator implements Comparator<String> {
        private final String[] ordering;

        public OrderedComparator(String[] ordering) {
            this.ordering = ordering;
        }

        @Override
        public int compare(String o1, String o2) {
            return ArrayUtils.indexOf(ordering, o1) - ArrayUtils.indexOf(ordering, o2);
        }

    }

}
