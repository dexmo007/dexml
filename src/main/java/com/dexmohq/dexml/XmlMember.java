package com.dexmohq.dexml;

import com.dexmohq.dexml.util.AnyArrayList;
import com.dexmohq.dexml.util.ArrayUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.stream.Stream;

abstract class XmlMember implements AnnotatedElement {

    private static final String DEFAULT_NAME_IDENTIFIER = "##default";

    private final int index;

    XmlMember(int index) {
        this.index = index;
    }

    abstract Object get(Object instance);

    abstract void set(Object instance, Object value);

    abstract String getName();

    abstract Class<?> getType();

    String getName(Class<? extends Annotation> a) {
        try {
            String name = (String) a.getMethod("name").invoke(getAnnotation(a));
            if (name.equals(DEFAULT_NAME_IDENTIFIER)) {
                return getName();
            }
            return name;
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new InternalError(e);
        }
    }

    int getIndex() {
        return index;
    }

    boolean isList() {
        final Class<?> type = getType();
        return Iterable.class.isAssignableFrom(type) || ArrayUtils.isArray(type);
    }

    Iterable<?> getAsIterable(Object instance) {
        final Object value = get(instance);
        if (Iterable.class.isAssignableFrom(getType())) {
            return (Iterable<?>) value;
        } else if (ArrayUtils.isArray(getType())) {
            return new AnyArrayList(value);
        }
        throw new XmlParseException("The given object is not iterable");
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof XmlMember && getName().equals(((XmlMember) obj).getName());
    }
}
