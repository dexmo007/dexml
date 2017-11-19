package com.dexmohq.dexml;

import com.dexmohq.dexml.annotation.ReadsMethod;
import com.dexmohq.dexml.annotation.ReadsString;
import com.dexmohq.dexml.exception.XmlConfigurationException;
import com.dexmohq.dexml.exception.XmlParseException;
import com.dexmohq.dexml.format.XmlReads;
import com.dexmohq.dexml.util.ReflectUtils;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public abstract class AnnotatedValueReads<T> implements XmlReads<T> {

    private AnnotatedValueReads() {
    }

    @SuppressWarnings("unchecked")
    public static <T> AnnotatedValueReads<T> create(Class<T> clazz) {
        final ReadsString readsString = clazz.getAnnotation(ReadsString.class);
        if (readsString == null) {
            throw new IllegalArgumentException("Type must be annotated with @ReadsString");
        }
        final boolean allowInaccessible = readsString.allowInaccessible();
        final List<Method> methods;
        final List<Constructor<?>> constructors;
        if (allowInaccessible) {
            methods = ReflectUtils.filterByAnnotation(clazz.getDeclaredMethods(), ReadsMethod.class);
            constructors = ReflectUtils.filterByAnnotation(clazz.getDeclaredConstructors(), ReadsMethod.class);
        } else {
            methods = ReflectUtils.filterByAnnotation(clazz.getMethods(), ReadsMethod.class);
            constructors = ReflectUtils.filterByAnnotation(clazz.getDeclaredConstructors(), ReadsMethod.class);
        }
        if (methods.size() == 1) {
            if (constructors.size() > 0) {
                throw new XmlConfigurationException("Multiple @ReadsMethod defined");
            }
            return new ByStaticMethod(clazz, methods.get(0));
        } else if (constructors.size() == 1) {
            if (methods.size() > 0) {
                throw new XmlConfigurationException("Multiple @ReadsMethod defined");
            }
            return new ByConstructor(constructors.get(0));
        } else {
            throw new XmlConfigurationException("No or ambiguous @ReadsMethod present");
        }
    }

    private static class ByStaticMethod<T> extends AnnotatedValueReads<T> {

        private final Method readsMethod;

        private ByStaticMethod(Class<T> clazz, Method readsMethod) {
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

    private static class ByConstructor<T> extends AnnotatedValueReads<T> {
        private final Constructor<T> constructor;

        private ByConstructor(Constructor<T> constructor) {
            if (constructor.getParameterCount() != 1 || constructor.getParameterTypes()[0] != String.class) {
                throw new XmlConfigurationException("@ReadsMethod constructor must have a single String argument");
            }
            this.constructor = constructor;
            this.constructor.setAccessible(true);
        }

        @Override
        public T read(String s) {
            try {
                return constructor.newInstance(s);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new XmlParseException(e);
            }
        }
    }


}
