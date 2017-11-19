package com.dexmohq.dexml.util;

import java.util.*;
import java.util.stream.Collectors;

public class IndexedProperties implements SortedMap<String, Property> {

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
    public Comparator<? super String> comparator() {
        throw new UnsupportedOperationException();
    }

    private int indexOf(String name) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public SortedMap<String, Property> subMap(String s, String k1) {
        final int start = indexOf(s);
        final int end = indexOf(k1);
        if (start == -1 || end == -1) {
            throw new IllegalArgumentException();
        }
        return new IndexedProperties(list.subList(start, end + 1));
    }

    @Override
    public SortedMap<String, Property> headMap(String s) {
        final int end = indexOf(s);
        if (end == -1) {
            throw new IllegalArgumentException();
        }
        return new IndexedProperties(list.subList(0, end + 1));
    }

    @Override
    public SortedMap<String, Property> tailMap(String s) {
        final int start = indexOf(s);
        if (start == -1) {
            throw new IllegalArgumentException();
        }
        return new IndexedProperties(list.subList(start, list.size()));
    }

    @Override
    public String firstKey() {
        return list.get(0).getName();
    }

    @Override
    public String lastKey() {
        return list.get(list.size() - 1).getName();
    }

    @Override
    public Set<String> keySet() {
        return list.stream().map(Property::getName).collect(Collectors.toSet());
    }

    @Override
    public List<Property> values() {
        return Collections.unmodifiableList(list);
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
