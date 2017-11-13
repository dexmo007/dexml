package com.dexmohq.dexml;

import com.dexmohq.dexml.format.XmlContext;

public class XmlParserFactory {

    public static XmlParser createParser() {
        return createParser(XmlContext.newDefault());
    }

    public static XmlParser createParser(XmlContext context) {
        return new ImmutableXmlParser(context);
    }
}
