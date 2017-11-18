package com.dexmohq.dexml.util;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.soap.Node;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Property {

    private final PropertyDescriptor descriptor;
    private final Field field;
    private final NodeType nodeType;

    public Property(PropertyDescriptor descriptor, Field field, NodeType nodeType) {
        this.descriptor = descriptor;
        this.field = field;
        this.nodeType = nodeType;
    }

    public Property(PropertyDescriptor descriptor, Field field) {
        this.descriptor = descriptor;
        this.field = field;
        this.nodeType = getNodeType(descriptor, field);
    }

    private static NodeType getNodeType(PropertyDescriptor descriptor, Field field) {
        final Method getter = descriptor.getReadMethod();
        final Method setter = descriptor.getWriteMethod();
        return NodeType.ELEMENT;//todo evaluate XmlAttr, XmlElem, XmlValue OR attr if format available, else elem
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

    public NodeType getNodeType() {
        return nodeType;
    }

    public enum NodeType {
        ELEMENT, ATTRIBUTE, VALUE
    }
}
