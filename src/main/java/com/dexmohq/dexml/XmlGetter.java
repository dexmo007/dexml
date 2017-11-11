package com.dexmohq.dexml;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class XmlGetter extends XmlMember {
    private final Method getter;

    public XmlGetter(Method getter, int index) {
        super(getter, index);
        this.getter = getter;
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
    public String getName() {
        return getter.getName().substring(3);//todo configure if decapitalize
    }

    @Override
    public Class<?> getType() {
        return getter.getReturnType();
    }
}
