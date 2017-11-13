package com.dexmohq.dexml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public interface NodeParser<T> {

    void appendChild(Document document, Node parent, String name, T t);

}
