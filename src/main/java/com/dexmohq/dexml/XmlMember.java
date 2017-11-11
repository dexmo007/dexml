package com.dexmohq.dexml;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;

abstract class XmlMember implements AnnotatedElement {

    private static final String DEFAULT_NAME_IDENTIFIER = "##default";

    private final Member member;
    private final int index;

    XmlMember(Member member, int index) {
        this.member = member;
        this.index = index;
    }

    abstract Object get(Object instance);

    abstract String getName();

    abstract Class<?> getType();

    String getName(Class<? extends Annotation> a) {
        try {
            String name = (String) a.getMethod("name").invoke(((AnnotatedElement) member).getAnnotation(a));
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

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return ((AnnotatedElement) member).getAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return ((AnnotatedElement) member).getAnnotations();
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return ((AnnotatedElement) member).getDeclaredAnnotations();
    }
}
