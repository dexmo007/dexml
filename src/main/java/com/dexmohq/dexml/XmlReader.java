package com.dexmohq.dexml;

import com.dexmohq.dexml.format.XmlContext;
import org.w3c.dom.Node;

public interface XmlReader {

    <T> T read(Class<T> clazz, Node root);

    XmlContext getContext();

}
