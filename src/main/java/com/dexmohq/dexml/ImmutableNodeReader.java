package com.dexmohq.dexml;

import com.dexmohq.dexml.annotation.Immutable;
import com.dexmohq.dexml.format.XmlContext;
import com.dexmohq.dexml.util.Property;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImmutableNodeReader<T> implements NodeReader<T> {

    private final Constructor<T> constructor;
    private final XmlContext context;

    private final Map<String, Property> map;

    public ImmutableNodeReader(Class<T> type, XmlContext context) {
        this.context = context;
        final Immutable annotation = type.getAnnotation(Immutable.class);
        final boolean strict = annotation != null && annotation.strict();//non-strict mode if not explicitly annotated
        final boolean allowInaccessible = annotation != null && annotation.allowInaccessible();
        final List<Property> properties = context.getProperties(type);
        final int propertyCount = properties.size();
        final Class[] constructorParameterTypes = new Class[propertyCount];
        this.map = new HashMap<>(propertyCount);
        for (int i = 0; i < propertyCount; i++) {
            final Property property = properties.get(i);

            final Class<?> propertyType = property.getType();
            constructorParameterTypes[i] = propertyType;
            map.put(property.getName(), property);
        }
        // if strict mode, fail on mutable property
        if (strict) {
            for (Property property : map.values()) {
                if (!property.isFinal()) {
                    throw new XmlConfigurationException("Type annotated as immutable has non-final field: " + property.getName());
                }
            }
        }
        try {
            if (allowInaccessible) {
                this.constructor = type.getDeclaredConstructor(constructorParameterTypes);
                this.constructor.setAccessible(true);
            } else {
                this.constructor = type.getConstructor(constructorParameterTypes);
            }
        } catch (NoSuchMethodException e) {
            throw new XmlConfigurationException("No constructor found to initiate immutable type");
        }

    }

    @Override
    public T read(Node node) {
        final Object[] args = new Object[constructor.getParameterCount()];
        final NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            final String name = child.getNodeName();
            final Property property = map.get(name);
            Class<?> type = property.getType();
            final NodeReader<?> reader = context.computeNodeReaderIfAbsent(property.getNodeType(), type);
            if (reader == null) {
                //todo configure what happens if ignore unknown props are founds
                continue;
            }
            args[i] = reader.read(child);
        }
        // todo validate requirement annotations and fail if requirement is not met
        try {
            return constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            //should not happen
            throw new InternalError(e);
        }
    }
}
