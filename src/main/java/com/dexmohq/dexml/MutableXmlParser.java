package com.dexmohq.dexml;

import com.dexmohq.dexml.format.XmlContext;
import com.dexmohq.dexml.format.XmlReads;
import org.w3c.dom.*;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlValue;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public class MutableXmlParser extends AbstractXmlParser {
    protected MutableXmlParser(XmlContext context) {
        super(context);
    }

    @Override
    public <T> T read(Class<T> clazz, Node root) {
        return null;
    }

//    private final boolean acceptPrivateConstructor = false;
//
//    private final Constructor<T> constructor;
//
//    protected MutableXmlParser(Class<T> clazz, XmlContext context) {
//        super(clazz, context);
//        try {
//            this.constructor = clazz.getConstructor();
//            if (acceptPrivateConstructor) {
//                constructor.setAccessible(true);
//            }
//        } catch (NoSuchMethodException e) {
//            throw new XmlParseException(e);
//        }
//    }
//
//    @Override
//    public T read(Node root) {
//
//        final T result;
//        try {
//            result = constructor.newInstance();
//        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
//            throw new XmlParseException(e);
//        }
//
//        //attributes
//        final NamedNodeMap attributes = root.getAttributes();
//        for (int i = 0; i < attributes.getLength(); i++) {
//            final Node attribute = attributes.item(i);
//            final XmlMember member = getMemberByName(attribute.getNodeName(), XmlAttribute.class);
//            final XmlReads reads = context.getReads(member.getType());
//            final Object value = reads.read(attribute.getNodeValue());
//            member.set(result, value);
//        }
//
//        final NodeList nodes = root.getChildNodes();
//        for (int i = 0; i < nodes.getLength(); i++) {
//            final Node node = nodes.item(i);
//            if (node instanceof Element) {
//                // elements
//                final XmlMember member = getMemberByName(((Element) node).getTagName(), XmlElement.class);
//                // if reads are available, the elements is read as such, other a parser is used
//                Object arg;
//                final Optional<? extends XmlReads<?>> readsOptional = context.getReadsOptional(member.getType());
//                if (readsOptional.isPresent()) {
//                    arg = readsOptional.get().read(node.getTextContent());
//                } else {
//                    final AbstractXmlParser<?> parser = context.computeParserIfAbsent(member.getType());
//                    arg = parser.read(node);
//                }
//                member.set(result, arg);
//            } else if (node instanceof Text) {
//                // value
//                final XmlMember member = members.get(XmlValue.class).get(0);
//                final String stringValue = ((Text) node).getWholeText();
//                final XmlReads reads = context.getReads(member.getType());
//                final Object value = reads.read(stringValue);
//                member.set(result, value);
//            }
//        }
//        return result;
//    }
}
