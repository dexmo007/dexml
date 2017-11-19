package com.dexmohq.dexml;

import com.dexmohq.dexml.annotation.Immutable;
import com.dexmohq.dexml.format.XmlContext;
import com.dexmohq.dexml.util.Property;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ImmutableNodeReader<T> extends AbstractNodeReader<T> {

    public ImmutableNodeReader(Class<T> type, XmlContext context) {
        super(type,context);
        final Immutable annotation = type.getAnnotation(Immutable.class);
        final boolean strict = annotation != null && annotation.strict();//non-strict mode if not explicitly annotated
        // if strict mode, fail on mutable property
        if (strict) {
            for (Property property : properties.values()) {
                if (!property.isFinal()) {
                    throw new XmlConfigurationException("Type annotated as immutable has non-final field: " + property.getName());
                }
            }
        }
    }

    @Override
    protected Constructor<T> getConstructor(Class<T> type) {
        final Immutable annotation = type.getAnnotation(Immutable.class);
        final boolean allowInaccessible = annotation != null && annotation.allowInaccessible();
        final Class[] constructorParameterTypes = new Class[properties.size()];
        int i = 0;
        for (Property property : properties.values()) {
            constructorParameterTypes[i++] = property.getType();
        }

        try {
            if (allowInaccessible) {
                final Constructor<T> constructor = type.getDeclaredConstructor(constructorParameterTypes);
                constructor.setAccessible(true);
                return constructor;
            } else {
                return type.getConstructor(constructorParameterTypes);
            }
        } catch (NoSuchMethodException e) {
            throw new XmlConfigurationException("No constructor found to initiate immutable type");
        }
    }

    @Override
    public T read(Node node) {
        final Object[] args = new Object[constructor.getParameterCount()];
        readProperties(node).forEach(pv -> {
            args[pv.getProperty().getIndex()] = pv.getValue();
        });
        // todo validate requirement annotations and fail if requirement is not met
        try {
            return constructor.newInstance(args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            //should not happen
            throw new InternalError(e);
        } catch (InstantiationException e) {
            throw new XmlParseException(e);
        }
    }
}
