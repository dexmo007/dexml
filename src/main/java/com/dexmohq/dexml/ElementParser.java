package com.dexmohq.dexml;

import com.dexmohq.dexml.format.XmlContext;
import com.dexmohq.dexml.format.XmlFormat;
import com.dexmohq.dexml.util.Property;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlValue;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

public class ElementParser<T> implements NodeParser<T> {

    private static final String DEFAULT_NAME_IDENTIFIER = "##default";
    private static final String VALUE_IDENTIFIER = "##VALUE";

    private final Class<T> type;
    private final XmlContext context;
    private final Map<String, NodeInfo> nodes;

    public ElementParser(Class<T> type, XmlContext context) {
        this.type = type;
        this.context = context;
        this.nodes = initMap();
//        System.out.println(type.getSimpleName() + " <<<<");
//        for (Map.Entry<String, NodeInfo> entry : nodes.entrySet()) {
//            System.out.println(entry.getKey() + ": " + entry.getValue().appender.getClass().getSimpleName());
//        }
//        System.out.println("----------------------");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void appendChild(Document document, Node parent, String name, T t) {
        final String tagName = context.toTagName(name);
        final Element element = document.createElement(tagName);
        for (NodeInfo nodeInfo : nodes.values()) {
            nodeInfo.appender.appendChild(document, element, nodeInfo.name, nodeInfo.get(t));
        }
        parent.appendChild(element);
    }

    private static class NodeInfo {

        private String name;
        private final NodeParser appender;
        private final Method getter;

        NodeInfo(String name, NodeParser appender, Method getter) {
            this.name = name;
            this.appender = appender;
            this.getter = getter;
        }

        Object get(Object instance) {
            try {
                return getter.invoke(instance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new XmlConfigurationException("Getter not accessible");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, NodeInfo> initMap() {
        final HashMap<String, NodeInfo> map = new HashMap<>();
        for (Property property : context.getProperties(type)) {
            final Method getter = property.getGetter();
            final Method setter = property.getGetter();
            String name = property.getName();
            // extracts xml type that might be annotated on getter, setter or the field; fails on ambiguity
            Annotation xmlType = tryGetXmlType(getter.getAnnotations(), null, name);
            if (setter != null)
                xmlType = tryGetXmlType(setter.getAnnotations(), xmlType, name);
            xmlType = tryGetXmlType(property.getField().getAnnotations(), xmlType, name);
            final Class<?> type = property.getType();
            if (xmlType == null) {
                final NodeParser<?> parser = context.getArbitraryParser(type);
                map.put(name, new NodeInfo(name, parser, getter));
            } else {
                if (xmlType.annotationType() == XmlValue.class) {
                    final XmlFormat format = context.getFormat(type);
                    final NodeParser appender = new ValueParser(format);
                    if (map.put(VALUE_IDENTIFIER, new NodeInfo(null, appender, getter)) != null) {
                        throw new XmlConfigurationException("Duplicate @XmlValue not allowed");
                    }
                    continue;
                }
                final String actualName = extractName(xmlType, name);
                if (xmlType.annotationType() == XmlAttribute.class) {
                    final XmlFormat format = context.getFormat(type);
                    final NodeParser appender = new AttributeParser(format, context);
                    map.put(actualName, new NodeInfo(actualName, appender, getter));
                } else { // @XmlElement
                    final NodeParser appender = context.computeElementParserIfAbsent(type);
                    map.put(actualName, new NodeInfo(actualName, appender, getter));
                }
            }
        }
        return map;
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    private static String extractName(Annotation a, String fieldName) {
        try {
            // todo this or cast to element or attribute and call actual method
            final String name = (String) a.annotationType().getMethod("name").invoke(a);
            if (name.equals(DEFAULT_NAME_IDENTIFIER))
                return fieldName;
            return name;
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            return fieldName;
        }
    }

    private static Annotation tryGetXmlType(Annotation[] annotations, Annotation previous, String name) {
        for (Annotation a : annotations) {
            final Class<? extends Annotation> annotationType = a.annotationType();
            if (annotationType == XmlElement.class
                    || annotationType == XmlAttribute.class
                    || annotationType == XmlValue.class) {
                if (previous != null) {
                    throw new XmlConfigurationException("Ambiguous xml type annotations for property: " + name);
                }
                previous = a;
            }
        }
        return previous;
    }
}
