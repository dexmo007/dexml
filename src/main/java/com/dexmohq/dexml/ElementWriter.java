package com.dexmohq.dexml;

import com.dexmohq.dexml.format.XmlContext;
import com.dexmohq.dexml.util.Property;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;

public class ElementWriter<T> implements NodeWriter<T> {

    private static final String DEFAULT_NAME_IDENTIFIER = "##default";
    private static final String VALUE_IDENTIFIER = "##VALUE";

    private final Class<T> type;
    private final XmlContext context;
    private final Map<String, NodeWriter> nodes;
    private final Map<String, Property> properties;

    public ElementWriter(Class<T> type, XmlContext context) {
        this.type = type;
        this.context = context;
        this.properties = context.getProperties(type);
        this.nodes = initMap();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void appendChild(Document document, Node parent, String name, T t) {
        final String tagName = context.toTagName(name);
        final Element element = document.createElement(tagName);
        for (Property property : properties.values()) {
            nodes.get(property.getName()).appendChild(document, element, property.getName(), property.get(t));
        }
        parent.appendChild(element);
    }

    @SuppressWarnings("unchecked")
    private Map<String, NodeWriter> initMap() {
        final HashMap<String, NodeWriter> map = new HashMap<>();
        for (Property property : context.getProperties(type).values()) {
            switch (property.getNodeType()) {
                case Node.ATTRIBUTE_NODE:
                    map.put(property.getName(), new AttributeWriter(context.getWrites(property.getType())));
                    break;
                case Node.TEXT_NODE:
                    map.put(property.getName(), new ValueWriter(context.getWrites(property.getType())));
                    break;
                case Node.ELEMENT_NODE:
                default:
                    map.put(property.getName(), context.computeElementWriterIfAbsent(property.getType()));
                    break;
            }
        }
        return map;
    }
}
