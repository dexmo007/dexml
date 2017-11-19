package com.dexmohq.dexml.test;

import com.dexmohq.dexml.exception.XmlConfigurationException;
import com.dexmohq.dexml.XmlReader;
import com.dexmohq.dexml.annotation.Implementations;
import com.dexmohq.dexml.annotation.MappedTypeName;
import com.dexmohq.dexml.annotation.Transient;
import com.dexmohq.dexml.format.XmlContext;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.Modifier;

public class Sandbox {

    @Implementations(value = {KepTrackingData.class, ForwarderTrackingData.class},
            mapping = Implementations.Mapping.BY_TYPE)
    public static abstract class TrackingData {

        private String cons;

        public String getCons() {
            return cons;
        }

        public void setCons(String cons) {
            this.cons = cons;
        }

        @Transient
        public abstract String getType();
    }

    @XmlRootElement
    @MappedTypeName(KepTrackingData.TYPE)
    public static class KepTrackingData extends TrackingData {

        private static final String TYPE = "KEP";

        private String trackingNumber;

        public String getTrackingNumber() {
            return trackingNumber;
        }

        public void setTrackingNumber(String trackingNumber) {
            this.trackingNumber = trackingNumber;
        }

        @Override
        public String getType() {
            return TYPE;
        }
    }

    @XmlRootElement
    @MappedTypeName(ForwarderTrackingData.TYPE)
    public static class ForwarderTrackingData extends TrackingData {

        private static final String TYPE = "FORW";

        private String licenceNumber;

        public String getLicenceNumber() {
            return licenceNumber;
        }

        public void setLicenceNumber(String licenceNumber) {
            this.licenceNumber = licenceNumber;
        }

        @Override
        public String getType() {
            return TYPE;
        }
    }

    static class AbstractReader implements XmlReader {

        private final XmlContext context = XmlContext.newDefault();

        @SuppressWarnings("unchecked")
        @Override
        public <T> T read(Class<T> superClass, Node root) {

            if (Modifier.isAbstract(superClass.getModifiers()) || superClass.isInterface()) {
                final Implementations implementations = superClass.getAnnotation(Implementations.class);
                if (implementations == null || implementations.value().length == 0) {
                    throw new XmlConfigurationException("Abstract type must specify implementations by @Implementations");
                }
                final Class[] classes = implementations.value();
                final BiMap<String, Class<? extends T>> mapping = HashBiMap.create(classes.length);
                for (Class<?> impl : classes) {
                    if (!superClass.isAssignableFrom(impl)) {
                        throw new XmlConfigurationException(impl + " is no subclass of " + superClass);
                    }
                    final String name = extractTypeIdentifier(implementations.mapping(), superClass, impl);
                    mapping.put(name, (Class<? extends T>) impl);
                }
                final NodeList children = root.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    System.out.println(children.item(i).getNodeName());
                }
                final NamedNodeMap attributes = root.getFirstChild().getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    System.out.println(attributes.item(i).getNodeName());
                }
                final String type = attributes.getNamedItem(implementations.xmlMemberName()).getNodeValue();
//                final NodeParser<? extends T> implParser = getContext().computeElementWriterIfAbsent(mapping.get(type));
//                implParser.appendChild(getContext().);
                throw new InternalError("Found out the type is: " + type);
//                return JAXB.unmarshal(StringUtils.nodeToString(root), mapping.get(type));
            }

            return null;
        }

        private String extractTypeIdentifier(Implementations.Mapping mapping, Class<?> superClass, Class<?> impl) {
            final String name;
            switch (mapping) {
                case BY_TYPE:
                    return impl.getSimpleName();
                case BY_FULL_TYPE:
                    return impl.getName();
                case BY_STRIPPED_NAME:
                    final String simpleName = impl.getSimpleName();
                    final String superClassName = superClass.getSimpleName();
                    name = simpleName.endsWith(superClassName) ? simpleName.substring(0, simpleName.length() - superClassName.length()) : simpleName;
                    break;
                case BY_MAPPED_TYPE_NAME:
                    final MappedTypeName annotation = impl.getAnnotation(MappedTypeName.class);
                    name = annotation != null ? annotation.value() : impl.getSimpleName();
                    break;
                default:
                    throw new InternalError();
            }
            if (name.isEmpty()) {
                throw new XmlConfigurationException("Mapped type name cannot be empty: " + impl);
            }
            return name;
        }

        @Override
        public XmlContext getContext() {
            return context;
        }
    }

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, NoSuchMethodException {
//        final AbstractReader reader = new AbstractReader();
//        final KepTrackingData trackingData = new KepTrackingData();
//        trackingData.setCons("78z319782631");
//        trackingData.setTrackingNumber("78123612736187");
//
//        final Method getTypeMethod = trackingData.getClass().getMethod("getType");
//        System.out.println(Arrays.toString(getTypeMethod.getAnnotations()));
//        System.out.println(getTypeMethod.isAnnotationPresent(Transient.class));
//
//        final Node node = XmlParserFactory.createParser().write(trackingData);
//        System.out.println(StringUtils.nodeToString(node));
//
//        final TrackingData read = reader.read(TrackingData.class, node);
//        System.out.println(read);
        String s = "";
        System.out.println(s instanceof CharSequence);


    }

}
