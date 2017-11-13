package com.dexmohq.dexml;

import com.dexmohq.dexml.format.XmlContext;
import org.w3c.dom.Node;

public interface XmlWriter {

    Node write(Object t);

    XmlContext getContext();

}
