package com.dexmohq.dexml;

import com.dexmohq.dexml.format.XmlContext;
import com.dexmohq.dexml.format.XmlReads;
import com.dexmohq.dexml.util.Property;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.stream.Stream;

public abstract class AbstractNodeReader<T> implements NodeReader<T> {

    protected final Constructor<T> constructor;
    protected final XmlContext context;

    protected final Map<String, Property> properties;

    public AbstractNodeReader(Class<T> type, XmlContext context) {
        this.context = context;
        this.properties = context.getProperties(type);
        this.constructor = getConstructor(type);
    }

    protected abstract Constructor<T> getConstructor(Class<T> type);

    protected Stream<PropertyValue> readProperties(Node node) {
        final NamedNodeMap attributes = node.getAttributes();
        return properties.values().stream().map(p -> {
            final Object value;
            switch (p.getNodeType()) {
                case Node.ATTRIBUTE_NODE:
                    final Node attribute = attributes.getNamedItem(p.getName());
                    if (attribute == null) {
                        value = null;
                    } else {
                        final String nodeValue = attribute.getNodeValue();
                        final XmlReads<?> reads = context.getReads(p.getType());
                        value = reads.read(nodeValue);
                    }
                    break;
                case Node.TEXT_NODE:
                    final String nodeValue = node.getNodeValue();
                    if (nodeValue == null) {
                        value = null;
                    } else {
                        value = context.getReads(p.getType()).read(nodeValue);
                    }
                    break;
                case Node.ELEMENT_NODE:
                    final Element childElement = getChildElement(node, p.getName());
                    if (childElement == null) {
                        value = null;
                    } else {
                        value = context.computeElementReaderIfAbsent(p.getType()).read(childElement);
                    }
                    break;
                default:
                    throw new InternalError("Properties map should not hold any node types other that Attr, Elem, Text");
            }
            return new PropertyValue(p, value);
        });
    }

    private static Element getChildElement(Node parent, String name) {
        final NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            if (child instanceof Element && child.getNodeName().equals(name)) {
                return (Element) child;
            }
        }
        return null;
    }

    protected class PropertyValue {

        private final Property property;
        private final Object value;

        private PropertyValue(Property property, Object value) {
            this.property = property;
            this.value = value;
        }

        protected Property getProperty() {
            return property;
        }

        protected Object getValue() {
            return value;
        }
    }


}
