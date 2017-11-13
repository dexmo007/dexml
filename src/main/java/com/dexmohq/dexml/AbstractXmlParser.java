package com.dexmohq.dexml;

import com.dexmohq.dexml.format.XmlContext;
import com.dexmohq.dexml.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public abstract class AbstractXmlParser implements XmlParser {

    protected final XmlContext context;
    private boolean useFields = true;
    private boolean followTransientModifier = true;
    private boolean useGetters = false;

    protected AbstractXmlParser(XmlContext context) {
        this.context = context;
    }

    private static final DocumentBuilder documentBuilder = getDocumentBuilder();

    private static DocumentBuilder getDocumentBuilder() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Node write(Object object) {//todo handle null
        final Class<?> type = object.getClass();
        final Document document = documentBuilder.newDocument();
        final String name = StringUtils.transformCamelCase(type.getSimpleName(), "-");

        final NodeParser parser = context.computeElementParserIfAbsent(type);
        parser.appendChild(document, document, name, object);
        return document;
    }

    @Override
    public XmlContext getContext() {
        return context;
    }
}
