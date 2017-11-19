package com.dexmohq.dexml;

import com.dexmohq.dexml.format.XmlWrites;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class AttributeWriter<T> implements NodeWriter<T> {

    private final XmlWrites<T> writes;

    public AttributeWriter(XmlWrites<T> writes) {
        this.writes = writes;
    }

    @Override
    public void appendChild(Document document, Node parent, String name, T t) {
        if (!(parent instanceof Element))
            throw new XmlConfigurationException("Attribute can only be child of an element");

        ((Element) parent).setAttribute(name, writes.write(t));//todo configure if underscores
    }


}
