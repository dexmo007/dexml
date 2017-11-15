package com.dexmohq.dexml;

import com.dexmohq.dexml.annotation.WritesMethod;
import com.dexmohq.dexml.annotation.WritesString;
import com.dexmohq.dexml.format.XmlWrites;
import com.dexmohq.dexml.util.ReflectUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class AnnotatedValueWrites<T> implements XmlWrites<T> {

    public static <T> AnnotatedValueWrites<T> create(Class<T> clazz) {
        if (!clazz.isAnnotationPresent(WritesString.class)) {
            throw new IllegalArgumentException("Type must be annotated with @WritesString");
        }
        Method writesMethod = ReflectUtils.getUniquelyAnnotatedMethod(clazz, WritesMethod.class);
        if (writesMethod == null) {
            // fall back to toString()-method
            return new ToStringWrites<>();
        }
        if (writesMethod.getParameterCount() != 0) {
            throw new XmlConfigurationException("Writes method must not take any parameters");
        }
        if (writesMethod.getReturnType() != String.class) {
            throw new XmlConfigurationException("Writes method must return type String");
        }
        return new AnnotatedMethodWrites<>(writesMethod);
    }

    private static class ToStringWrites<T> extends AnnotatedValueWrites<T> {

        @Override
        public String write(T t) {
            return t.toString();
        }
    }

    private static class AnnotatedMethodWrites<T> extends AnnotatedValueWrites<T> {

        private final Method writesMethod;

        private AnnotatedMethodWrites(Method writesMethod) {
            this.writesMethod = writesMethod;
        }

        @Override
        public String write(T t) {
            try {
                return (String) writesMethod.invoke(t);
            } catch (IllegalAccessException | InvocationTargetException e) {
                // should not happen
                throw new InternalError(e);
            }
        }

    }


}
