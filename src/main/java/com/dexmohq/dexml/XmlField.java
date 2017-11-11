package com.dexmohq.dexml;

import java.lang.reflect.Field;

public class XmlField extends XmlMember {

    private final Field field;

    public XmlField(Field field, int index) {
        super(field, index);
        this.field = field;
        this.field.setAccessible(true);
    }

    @Override
    public Object get(Object instance) {
        try {
            return field.get(instance);
        } catch (IllegalAccessException e) {
            throw new XmlParseException(e);
        }
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }
}
