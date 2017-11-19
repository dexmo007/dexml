package com.dexmohq.dexml.util;

import com.dexmohq.dexml.XmlParseException;
import org.w3c.dom.Node;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Property {

    private final PropertyDescriptor descriptor;
    private final Method getter;
    private final Method setter;
    private final Field field;
    private final short nodeType;
    private final int index;
    private final String xmlName;


    public Property(String xmlName, PropertyDescriptor descriptor, Field field, short nodeType, int index) {
        this.xmlName = xmlName;
        this.descriptor = descriptor;
        this.getter = descriptor.getReadMethod();
        this.setter = descriptor.getWriteMethod();
        this.field = field;
        this.field.setAccessible(true);
        this.nodeType = nodeType;
        this.index = index;
    }

    public Property(String xmlName, PropertyDescriptor descriptor, Field field, int index) {
        this(xmlName, descriptor, field, getNodeType(descriptor, field), index);
    }

    private static short getNodeType(PropertyDescriptor descriptor, Field field) {
        final Method getter = descriptor.getReadMethod();
        final Method setter = descriptor.getWriteMethod();
        return Node.ELEMENT_NODE;//todo evaluate XmlAttr, XmlElem, XmlValue OR attr if format available, else elem
    }

    public Method getGetter() {
        return getter;
    }

    public Method getSetter() {
        return setter;
    }

    public Field getField() {
        return field;
    }

    public boolean isFinal() {
        return getSetter() == null && (field == null || Modifier.isFinal(field.getModifiers()));
    }

    public void setAccessible() {
        getter.setAccessible(true);
        setter.setAccessible(true);
    }

    public String getName() {
        return xmlName;
    }

    public Class<?> getType() {
        return descriptor.getPropertyType();
    }

    public short getNodeType() {
        return nodeType;
    }

    public int getIndex() {
        return index;
    }

    public Object get(Object instance) {
        try {
            return getter.invoke(instance);//todo field access
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new XmlParseException(e);
        }
    }
}
