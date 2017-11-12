package com.dexmohq.dexml;

import com.dexmohq.dexml.format.XmlContext;
import com.dexmohq.dexml.format.XmlWrites;
import org.w3c.dom.*;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static com.dexmohq.dexml.StreamUtils.single;

public abstract class XmlParser<T> {

    protected final Class<T> clazz;
    protected final XmlContext context;
    private boolean useFields = true;
    private boolean followTransientModifier = true;
    private boolean useGetters = false;

    protected final Map<Class<? extends Annotation>, List<XmlMember>> members;

    protected XmlParser(Class<T> clazz, XmlContext context) {
        this.clazz = clazz;
        this.context = context;
        this.members = initializeMemberMap();
    }

    public static <T> XmlParser<T> create(Class<T> clazz) {
        return create(clazz, XmlContext.newDefault());
    }

    public static <T> XmlParser<T> create(Class<T> clazz, XmlContext context) {
        return new ImmutableXmlParser<>(clazz, context);
    }

    private Map<Class<? extends Annotation>, List<XmlMember>> initializeMemberMap() {
        if (!useFields && !useGetters)
            throw new XmlParserConfigurationException("Either fields or getters must be enabled");
        final HashMap<Class<? extends Annotation>, List<XmlMember>> map = new HashMap<>();
        map.put(XmlElement.class, new ArrayList<>());
        map.put(XmlAttribute.class, new ArrayList<>());
        int index = 0;
        if (useFields) {
            for (Field field : clazz.getDeclaredFields()) {
                if ((!followTransientModifier || !Modifier.isTransient(field.getModifiers()))
                        && !field.isAnnotationPresent(XmlTransient.class)) {

                    if (field.isAnnotationPresent(XmlElement.class)) {
                        map.get(XmlElement.class).add(new XmlField(field, index++));
                    } else if (field.isAnnotationPresent(XmlAttribute.class)) {
                        map.get(XmlAttribute.class).add(new XmlField(field, index++));
                    } else if (field.isAnnotationPresent(XmlValue.class)) {
                        if (map.containsKey(XmlValue.class)) {
                            throw new XmlConfigurationException("Only a single @XmlValue is allowed.");
                        }
                        map.put(XmlValue.class, Collections.singletonList(new XmlField(field, index++)));
                    }

                }
            }
        }
        if (useGetters) {
            for (Method method : clazz.getMethods()) {
                if (isGetter(method) && !method.isAnnotationPresent(XmlTransient.class)) {//todo or is setter
                    //todo work out to avoid this effective duplication
                    if (method.isAnnotationPresent(XmlElement.class)) {
                        map.get(XmlElement.class).add(new XmlAccessor(clazz, method, index++, true));
                    } else if (method.isAnnotationPresent(XmlAttribute.class)) {
                        map.get(XmlAttribute.class).add(new XmlAccessor(clazz, method, index++, true));
                    } else if (method.isAnnotationPresent(XmlValue.class)) {
                        if (map.containsKey(XmlValue.class)) {
                            throw new XmlConfigurationException("Only a single @XmlValue is allowed.");
                        }
                        map.put(XmlValue.class, Collections.singletonList(new XmlAccessor(clazz, method, index++, true)));
                    }
                }
            }
        }
        return map;
    }

    private static boolean isGetter(Method method) {
        return method.getName().startsWith("get")
                && method.getName().length() > 3
                && method.getParameterTypes().length == 0
                && !method.getName().equals("getClass")
                && !method.getReturnType().equals(Void.TYPE);
    }

    private static final DocumentBuilder documentBuilder = getDocumentBuilder();

    private static DocumentBuilder getDocumentBuilder() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new UnsupportedOperationException(e);
        }
    }


    @SuppressWarnings("unchecked")
    public Node write(T object) {
        final Document document = documentBuilder.newDocument();
        final Element root = document.createElement(clazz.getSimpleName());
        final List<XmlMember> xmlValue = members.get(XmlValue.class);
        if (xmlValue != null) {
            final String textValue = getValue(xmlValue.get(0), object);
            final Text textNode = document.createTextNode(textValue);
            root.appendChild(textNode);
        }
        document.appendChild(root);
        for (XmlMember attribute : members.get(XmlAttribute.class)) {
            final Attr attr = document.createAttribute(attribute.getName(XmlAttribute.class));
            attr.setValue(getValue(attribute, object));
            root.appendChild(attr);
        }
        for (XmlMember element : members.get(XmlElement.class)) {
            final Object value = element.get(object);
            if (element.isList()) {
                final Iterator<?> iterator = element.getAsIterable(value).iterator();
                final Element list = document.createElement(element.getName(XmlElement.class));
                if (iterator.hasNext()) {
                    final Object first = iterator.next();
                    final XmlParser parser = context.computeParserIfAbsent(first.getClass());//todo formatted
                    list.appendChild(parser.write(first));
                    while (iterator.hasNext()) {
                        final Object next = iterator.next();
                        list.appendChild(parser.write(next));
                    }
                }
                root.appendChild(list);
                continue;
            }

            final Class type = element.getType();
            final Optional<? extends XmlWrites> writesOptional = context.getWritesOptional(type);
            if (writesOptional.isPresent()) {
                final Element xmlElement = document.createElement(element.getName(XmlElement.class));
                xmlElement.setTextContent(writesOptional.get().write(value));
                root.appendChild(xmlElement);
            } else {
                final XmlParser parser = context.computeParserIfAbsent(type);
                final Node xmlElement = parser.write(value);
                root.appendChild(xmlElement);
            }
        }
        return document;
    }

    @SuppressWarnings("unchecked")
    private String getValue(XmlMember member, T object) {
        final XmlWrites writes = context.getWrites(member.getType());
        return writes.write(member.get(object));
    }

    protected XmlMember getMemberByName(String name, Class<? extends Annotation> annotationClass) {
        return members.get(annotationClass).stream()
                .filter(m -> m.getName(annotationClass).equals(name))
                .collect(single());

    }

    public abstract T read(Node root);

}
