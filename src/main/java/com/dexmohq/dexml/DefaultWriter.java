package com.dexmohq.dexml;

import com.dexmohq.dexml.format.XmlContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class DefaultWriter implements XmlWriter {

    private final XmlContext context;

    public DefaultWriter(XmlContext context) {
        this.context = context;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Node write(Object object) {
        final Document document = context.newDocument();
        if (object == null) {
            return document;
        }
        final Class<?> type = object.getClass();
        final NodeWriter nodeWriter = context.computeElementWriterIfAbsent(type);
        nodeWriter.appendChild(document,document,context.toTagName(type.getSimpleName()), object);
        return document;
//        final Element element = document.createElement(context.toTagName(type.getSimpleName()));
//        if (type.isAnnotationPresent(WritesString.class)) {
//            final XmlWrites<?> writes = context.computeAnnotatedWritesIfAbsent(type);
//        }
//        return null;
    }

    @Override
    public XmlContext getContext() {
        return context;
    }
}
