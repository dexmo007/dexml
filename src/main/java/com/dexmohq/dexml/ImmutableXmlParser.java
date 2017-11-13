package com.dexmohq.dexml;

import com.dexmohq.dexml.format.XmlContext;
import com.dexmohq.dexml.format.XmlReads;
import org.w3c.dom.*;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlValue;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ImmutableXmlParser extends AbstractXmlParser {
    protected ImmutableXmlParser(XmlContext context) {
        super(context);
    }

    @Override
    public <T> T read(Class<T> clazz, Node root) {
        throw new UnsupportedOperationException();
    }

//    private final Constructor<T> constructor;
//
//    protected ImmutableXmlParser(Class<T> clazz, XmlContext context) {
//        super(clazz, context);
//        this.constructor = findMatchingConstructor();
//    }
//
//    @Override
//    public T read(Node root) {
//
//        final Object[] args = new Object[members.values().stream().mapToInt(List::size).sum()];
//
//        //attributes
//        final NamedNodeMap attributes = root.getAttributes();
//        for (int i = 0; i < attributes.getLength(); i++) {
//            final Node attribute = attributes.item(i);
//            final XmlMember member = getMemberByName(attribute.getNodeName(), XmlAttribute.class);
//            final XmlReads reads = context.getReads(member.getType());
//            args[member.getIndex()] = reads.read(attribute.getNodeValue());
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
//                args[member.getIndex()] = arg;
//            } else if (node instanceof Text) {
//                // value
//                final XmlMember value = members.get(XmlValue.class).get(0);
//                final String stringValue = ((Text) node).getWholeText();
//                final XmlReads reads = context.getReads(value.getType());
//                args[value.getIndex()] = reads.read(stringValue);
//            }
//        }
//
//        try {
//            return constructor.newInstance(args);
//        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
//            throw new XmlParseException(e);
//        }
//    }
//
//    private Constructor<T> findMatchingConstructor() {
//        final Class<?>[] args = members.values().stream().flatMap(List::stream)
//                .sorted(Comparator.comparingInt(XmlMember::getIndex))
//                .map(XmlMember::getType)
//                .toArray(Class<?>[]::new);
//        try {
//            return clazz.getConstructor(args);
//        } catch (NoSuchMethodException e) {
//            throw new XmlConfigurationException("no matching constructor found");
//        }
//    }
}
