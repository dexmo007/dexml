package com.dexmohq.dexml.util;

import java.util.*;
import java.util.stream.Collectors;

public class IndexedProperties implements Map<String, Property> {

    private final ArrayList<Property> list;

    public IndexedProperties(int initialCapacity) {
        this.list = new ArrayList<>(initialCapacity);
    }

    public IndexedProperties() {
        this(16);
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
    public boolean containsKey(Object o) {
        return list.stream().anyMatch(p -> p.getName().equals(o));
    }

    @Override
    public boolean containsValue(Object o) {
        return list.contains(o);
    }

    @Override
    public Property get(Object o) {
        return list.stream().filter(p -> p.getName().equals(o)).findFirst().orElse(null);
    }

    @Override
    public Property put(String s, Property property) {
        if (!s.equals(property.getName())) {
            throw new IllegalArgumentException("Key does not equal the property's name");
        }
        if (list.stream().anyMatch(p -> p.getName().equals(s))) {
            throw new IllegalArgumentException("Duplicate entries for name: " + s);
        }
        list.add(property);
        return null;
    }

    @Override
    public Property remove(Object o) {
        return list.remove(o) ? (Property) o : null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Property> map) {
        for (java.util.Map.Entry<? extends String, ? extends Property> entry : map.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public Set<String> keySet() {
        return list.stream().map(Property::getName).collect(Collectors.toSet());
    }

    @Override
    public Collection<Property> values() {
        return new ArrayList<>(list);
    }

    @Override
    public Set<java.util.Map.Entry<String, Property>> entrySet() {
        return list.stream().map(p -> new Entry(p.getName(), p)).collect(Collectors.toSet());
    }

    private static class Entry implements java.util.Map.Entry<String, Property> {
        private final String key;
        private Property value;

        public Entry(String key, Property value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Property getValue() {
            return value;
        }

        @Override
        public Property setValue(Property property) {
            final Property old = value;
            value = property;
            return old;
        }
    }

}
