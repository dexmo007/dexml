package com.dexmohq.dexml;

import com.dexmohq.dexml.annotation.WritesString;
import com.dexmohq.dexml.format.XmlContext;
import com.dexmohq.dexml.format.XmlWrites;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DefaultWriter implements XmlWriter {

    private final XmlContext context;

    public DefaultWriter(XmlContext context) {
        this.context = context;
    }

    @Override
    public Node write(Object t) {
        final Document document = context.newDocument();
        if (t == null) {
            return document;
        }
        final Class<?> type = t.getClass();
        final Element element = document.createElement(context.toTagName(type.getSimpleName()));
        if (type.isAnnotationPresent(WritesString.class)) {
            final XmlWrites<?> writes = context.computeAnnotatedWritesIfAbsent(type);
        }
        return null;
    }

    @Override
    public XmlContext getContext() {
        return context;
    }
}
