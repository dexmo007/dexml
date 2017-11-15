package com.dexmohq.dexml;

import com.dexmohq.dexml.annotation.ReadsMethod;
import com.dexmohq.dexml.annotation.ReadsString;
import com.dexmohq.dexml.format.XmlReads;
import com.dexmohq.dexml.util.ReflectUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class AnnotatedValueReads<T> implements XmlReads<T> {

    private final Method readsMethod;

    public AnnotatedValueReads(Class<T> clazz) {
        if (!clazz.isAnnotationPresent(ReadsString.class)) {
            throw new IllegalArgumentException("Type must be annotated with @ReadsString");
        }
        final Method readsMethod = ReflectUtils.getUniquelyAnnotatedMethod(clazz, ReadsMethod.class);
        if (readsMethod == null) {
            throw new XmlConfigurationException("No @ReadsMethod present");
        }
        if (!Modifier.isStatic(readsMethod.getModifiers())) {
            throw new XmlConfigurationException("Reads method must be static");
        }
        if (clazz.isAssignableFrom(readsMethod.getReturnType())) {
            throw new XmlConfigurationException("Reads method must return a type that is assignable to " + clazz);
        }
        if (readsMethod.getParameterCount() != 1 || readsMethod.getParameterTypes()[0] != String.class) {
            throw new XmlConfigurationException("Reads method must take a single String parameter");
        }
        this.readsMethod = readsMethod;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T read(String s) {
        try {
            return (T) readsMethod.invoke(null, s);
        } catch (IllegalAccessException | InvocationTargetException e) {
            // should not happen
            throw new InternalError(e);
        }
    }
}
