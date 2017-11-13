package com.dexmohq.dexml;

import com.dexmohq.dexml.format.XmlContext;
import com.dexmohq.dexml.format.XmlFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class AttributeParser<T> implements NodeParser<T> {

    private final XmlFormat<T> format;
    private final XmlContext context;

    public AttributeParser(XmlFormat<T> format, XmlContext context) {
        this.format = format;
        this.context = context;
    }

    @Override
    public void appendChild(Document document, Node parent, String name, T t) {
        if (!(parent instanceof Element))
            throw new XmlConfigurationException("Attribute can only be child of an element");

        ((Element) parent).setAttribute(context.toAttributeName(name), format.write(t));//todo configure if underscores
    }


}
