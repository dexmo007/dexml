package com.dexmohq.dexml;

import com.dexmohq.dexml.annotation.Mutable;
import com.dexmohq.dexml.annotation.Mutable.AccessorType;
import com.dexmohq.dexml.exception.XmlConfigurationException;
import com.dexmohq.dexml.exception.XmlParseException;
import com.dexmohq.dexml.format.XmlContext;
import com.dexmohq.dexml.util.Property;
import org.w3c.dom.Node;

import java.lang.reflect.*;

public abstract class MutableNodeReader<T> extends AbstractNodeReader<T> {

    @SuppressWarnings("unchecked")
    private MutableNodeReader(Class<T> type, XmlContext context) {
        super(type, context);
    }

    public static <T> MutableNodeReader<T> create(Class<T> type, XmlContext context) {
        final Mutable mutable = type.getAnnotation(Mutable.class);
        final AccessorType accessorType;
        final boolean allowInaccessible;
        if (mutable != null) {
            accessorType = mutable.accessorType();
            allowInaccessible = mutable.allowInaccessible();
        } else {
            accessorType = AccessorType.AUTO;
            allowInaccessible = false;
        }
        final MutableNodeReader<T> reader;
        switch (accessorType) {
            case SETTER:
                reader = new BySetter<>(type, context);
                for (Property property : reader.properties.values()) {
                    if (property.getSetter() == null) {
                        throw new XmlConfigurationException("No setter avaible for property: " + property.getName());
                    }
                }
                break;
            case FIELD:
                reader = new ByField<>(type, context);
                for (Property property : reader.properties.values()) {
                    if (property.getField() == null) {
                        throw new XmlConfigurationException("No field found for name " + property.getName());
                    }
                    if (Modifier.isFinal(property.getField().getModifiers())) {
                        throw new XmlConfigurationException("Will not be able to set final field: " + property.getName());
                    }
                }
                break;
            case AUTO:
            default:
                reader = new Automated<>(type, context);
        }
        if (allowInaccessible) {
            reader.properties.values().forEach(Property::setAccessible);
            reader.constructor.setAccessible(true);
        }
        return reader;
    }

    @Override
    protected Constructor<T> getConstructor(Class<T> type) {
        try {
            return type.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new XmlConfigurationException("No empty constructor available");
        }
    }

    public abstract void setValue(T t, PropertyValue pv);

    @Override
    public T read(Node node) {
        final T t;
        try {
            t = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new XmlParseException(e);
        }
        readProperties(node).forEach(pv -> setValue(t, pv));
        return t;
    }

    private static class BySetter<T> extends MutableNodeReader<T> {

        private BySetter(Class<T> type, XmlContext context) {
            super(type, context);
        }

        @Override
        public void setValue(T t, PropertyValue pv) {
            try {
                pv.getProperty().getSetter().invoke(t, pv.getValue());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new XmlParseException(e);
            }
        }
    }

    private static class ByField<T> extends MutableNodeReader<T> {
        private ByField(Class<T> type, XmlContext context) {
            super(type, context);
        }

        @Override
        public void setValue(T t, PropertyValue pv) {
            try {
                final Field field = pv.getProperty().getField();
                if (field == null) {
                    throw new XmlParseException("No field found with name " + pv.getProperty().getName());
                }
                field.set(t, pv.getValue());
            } catch (IllegalAccessException e) {
                throw new XmlParseException(e);
            }
        }
    }

    private static class Automated<T> extends MutableNodeReader<T> {

        private Automated(Class<T> type, XmlContext context) {
            super(type, context);//todo cache method to set each value, validate state so there are no surprises at reading time
        }

        @Override
        public void setValue(T t, PropertyValue pv) {
            final Method setter = pv.getProperty().getSetter();
            if (setter != null && setter.isAccessible()) {
                try {
                    setter.invoke(t, pv.getValue());
                } catch (IllegalAccessException e) {
                    // should not happen
                } catch (InvocationTargetException e) {
                    throw new XmlParseException(e);
                }
            } else {
                try {
                    pv.getProperty().getField().set(t, pv.getValue());
                } catch (IllegalAccessException e) {
                    //should not happen
                }
            }
        }
    }


}
