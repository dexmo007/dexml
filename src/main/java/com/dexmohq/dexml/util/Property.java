package com.dexmohq.dexml.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Property {

    private final PropertyDescriptor descriptor;
    private final Field field;

    public Property(PropertyDescriptor descriptor, Field field) {
        this.descriptor = descriptor;
        this.field = field;
    }

    public Method getGetter() {
        return descriptor.getReadMethod();
    }

    public Method getSetter() {
        return descriptor.getWriteMethod();
    }

    public Field getField() {
        return field;
    }

    public boolean isFinal() {
        return getSetter() == null && (field == null || Modifier.isFinal(field.getModifiers()));
    }

    public String getName() {
        return descriptor.getName();
    }

    public Class<?> getType() {
        return descriptor.getPropertyType();
    }
}
