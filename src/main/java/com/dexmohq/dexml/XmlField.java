package com.dexmohq.dexml;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;

public class XmlField extends XmlMember {

    private final Field field;

    public XmlField(Field field, int index) {
        super(index);
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
    void set(Object instance, Object value) {
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            //should not happen
            throw new InternalError(e);
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

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return field.getAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return field.getAnnotations();
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return field.getDeclaredAnnotations();
    }
}
