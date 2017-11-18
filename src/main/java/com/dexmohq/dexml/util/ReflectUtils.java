package com.dexmohq.dexml.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

public final class ReflectUtils {

    public static Method getUniquelyAnnotatedMethod(Class<?> clazz, Class<? extends Annotation> annotation) {
        Method annotated = null;
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                if (annotated != null) {
                    throw new IllegalStateException("Annotation '" + annotation
                            + "' occurs on multiple methods in class " + clazz);
                }
                annotated = method;
            }
        }
        return annotated;
    }

    public static boolean isMethodAnnotationPresent(Class<?> clazz, Class<? extends Annotation> annotation) {
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                return true;
            }
        }
        return false;
    }

    private ReflectUtils() {
    }

    public static boolean isDerivedAnnotationPresent(Method method, Class<? extends Annotation> annotation) {
        if (method == null) {
            return false;
        }
        if (method.isAnnotationPresent(annotation)) {
            return true;
        }
        Class<?> clazz = method.getDeclaringClass().getSuperclass();
        while (clazz != Object.class) {
            try {
                if (clazz.getMethod(method.getName(), method.getParameterTypes()).isAnnotationPresent(annotation)) {
                    return true;
                }
            } catch (NoSuchMethodException e) {
                return false;
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }

}
