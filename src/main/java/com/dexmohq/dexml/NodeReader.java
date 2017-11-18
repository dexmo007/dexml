package com.dexmohq.dexml;

import org.w3c.dom.Node;

public interface NodeReader<T> {

    T read(Node node);

}
