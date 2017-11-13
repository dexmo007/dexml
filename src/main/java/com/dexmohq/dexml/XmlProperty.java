package com.dexmohq.dexml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlValue;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;

public class XmlProperty implements AnnotatedElement {

    public enum Type {
        ELEMENT, ATTRIBUTE, VALUE
    }

    private final PropertyDescriptor descriptor;
    private final Field field;

    public XmlProperty(PropertyDescriptor descriptor, Field field) {
        this.descriptor = descriptor;
        this.field = field;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return null;
    }

    @Override
    public Annotation[] getAnnotations() {
        return new Annotation[0];
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return new Annotation[0];
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return false;
    }
//
//    private static void initialize(Class<?> clazz) {
//        try {
//            final BeanInfo beanInfo = Introspector.getBeanInfo(clazz, Object.class);
//            for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
//                final Method getter = descriptor.getReadMethod();
//                final Method setter = descriptor.getWriteMethod();
//                Annotation[] fieldAnnotations = new Annotation[0];
//                final String name = descriptor.getName();
//                try {
//                    fieldAnnotations = clazz.getDeclaredField(name).getAnnotations();
//                } catch (NoSuchFieldException e) {
//                    // fall
//                }
//                // extracts xml type that might be annotated on getter, setter or the field; fails on ambiguity
//                Annotation xmlType = tryGetXmlType(getter.getAnnotations(), null, name);
//                if (setter != null)
//                    xmlType = tryGetXmlType(setter.getAnnotations(), xmlType, name);
//                xmlType = tryGetXmlType(fieldAnnotations, xmlType, name);
//                if (xmlType == null)
//                    xmlType = new DefaultXmlElement(name);//todo or skip
//
//            }
//        } catch (IntrospectionException e) {
//            throw new XmlConfigurationException(e);
//        }
//    }
//
//    private static Annotation tryGetXmlType(Annotation[] annotations, Annotation previous, String name) {
//        for (Annotation a : annotations) {
//            final Class<? extends Annotation> annotationType = a.annotationType();
//            if (annotationType == XmlElement.class
//                    || annotationType == XmlAttribute.class
//                    || annotationType == XmlValue.class) {
//                if (previous != null) {
//                    throw new XmlConfigurationException("Ambiguous xml type annotations for property: " + name);
//                }
//                previous = a;
//            }
//        }
//        return previous;
//    }

    private static class DefaultXmlElement implements XmlElement {
        private final String name;

        public DefaultXmlElement(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object obj) {
            return XmlElement.class.isAssignableFrom(obj.getClass()) && ((XmlElement) obj).name().equals(name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return "XmlElement(" + name + ")";
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return XmlElement.class;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public boolean nillable() {
            return true;
        }

        @Override
        public boolean required() {
            return false;
        }

        @Override
        public String namespace() {
            return "";
        }

        @Override
        public String defaultValue() {
            return "";
        }

        @Override
        public Class type() {
            return DEFAULT.class;
        }
    }
}














