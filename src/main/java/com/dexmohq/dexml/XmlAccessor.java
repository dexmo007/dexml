package com.dexmohq.dexml;

import com.dexmohq.dexml.util.ArrayUtils;
import com.dexmohq.dexml.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class XmlAccessor extends XmlMember {
    private final Method getter;
    private final Method setter;

    public XmlAccessor(Class<?> clazz, Method method, int index, boolean getter) {
        super(index);
        try {
            if (getter) {
                this.getter = method;
                this.setter = clazz.getMethod("set" + method.getName().substring(3), method.getReturnType());
            } else {
                this.setter = method;
                this.getter = clazz.getMethod("get" + method.getName().substring(3));
            }
        } catch (NoSuchMethodException e) {
            throw new XmlParseException(e);
        }
    }

    @Override
    public Object get(Object instance) {
        try {
            return getter.invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new XmlParseException(e);
        }
    }

    @Override
    void set(Object instance, Object value) {
        try {
            setter.invoke(instance, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new XmlParseException(e);
        }
    }

    @Override
    public String getName() {
        return StringUtils.decapitalize(getter.getName().substring(3));
    }

    @Override
    public Class<?> getType() {
        return getter.getReturnType();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        final T getterAnnotation = getter.getAnnotation(annotationClass);
        final T setterAnnotation = setter.getAnnotation(annotationClass);
        if (getterAnnotation == null) {
            return setterAnnotation;
        }
        if (setterAnnotation == null) {
            return getterAnnotation;
        }
        throw new XmlParseException("Both the getter the setter have the same annotation.");
    }

    @Override
    public Annotation[] getAnnotations() {
        return ArrayUtils.concat(getter.getAnnotations(), setter.getAnnotations());
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return ArrayUtils.concat(getter.getDeclaredAnnotations(), setter.getDeclaredAnnotations());
    }
}
