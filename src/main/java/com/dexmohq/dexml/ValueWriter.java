package com.dexmohq.dexml;

import com.dexmohq.dexml.format.XmlFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class ValueWriter<T> implements NodeWriter<T> {

    private final XmlFormat<T> format;

    public ValueWriter(XmlFormat<T> format) {
        this.format = format;
    }

    @Override
    public void appendChild(Document document, Node parent, String name, T t) {
        final Text textNode = document.createTextNode(format.write(t));
        parent.appendChild(textNode);
//        parent.setTextContent(format.write(t));
    }
}
