package com.dexmohq.dexml;

public class Test {

//    static class AbstractXmlParser<T> {
//
//        private final Class<T> clazz;
//        private final XmlContext context;
//        private boolean useFields = false;
//        private boolean followTransientModifier = true;
//        private boolean useGetters = true;
//
//        private final Map<Class<? extends Annotation>, List<XmlMember>> fields;
//
//        private AbstractXmlParser(Class<T> clazz, XmlContext context) {
//            this.clazz = clazz;
//            this.context = context;
//            this.fields = fields();
//        }
//
//        static <T> AbstractXmlParser<T> appendChild(Class<T> clazz) {
//            return new AbstractXmlParser<>(clazz, XmlContext.newDefault());
//        }
//
//        private static void checkConfiguration() {
//            // only a single XmlValue
//            // lower case unique XmlElement names
//            // unique attribute names
//            // all non void
//        }
//
//        private static final DocumentBuilder documentBuilder = getDocumentBuilder();
//
//        private static DocumentBuilder getDocumentBuilder() {
//            try {
//                return DocumentBuilderFactory.newInstance().newDocumentBuilder();
//            } catch (ParserConfigurationException e) {
//                throw new UnsupportedOperationException(e);
//            }
//        }
//
//        @SuppressWarnings("unchecked")
//        private String getValue(XmlMember member, T object) {
//            final XmlWrites writes = context.getWrites(member.getType());
//            return writes.write(member.get(object));
//        }
//
////        public Document write(T object) {
////            final Document document = documentBuilder.newDocument();
////            final Element root = document.createElement(clazz.getSimpleName());
////            final List<XmlMember> value = fields.get(XmlValue.class);
////            if (value != null) {
////                final String textValue = getValue(value.get(0), object);
////                final Text textNode = document.createTextNode(textValue);
////                root.appendChild(textNode);
////            }
////            document.appendChild(root);
////            final List<XmlMember> attributes = fields.get(XmlAttribute.class);
////            if (attributes != null) {
////                for (XmlMember attribute : attributes) {
////                    final Attr attr = document.createAttribute(attribute.getName(XmlAttribute.class));
////                    attr.setValue(getValue(attribute, object));
////                    root.appendChild(attr);
////                }
////            }
////            final List<XmlMember> elements = fields.get(XmlElement.class);
////            //todo configuration to decide what to do in case no exact writes but matching are existing:
////            // use matching to provide value as string OR build new parser
////            // register newly build parser in the context for it is created only once
////            if (elements != null) {
////                for (XmlMember element : elements) {
////                    final XmlWrites writes = context.getWrites(element.getType());
////                    final Element xmlElement = document.createElement(element.getName(XmlElement.class));
////
////                }
////            }
////            return document;
////        }
//
//        private static String getAttributeName(Member member) {
//            AnnotatedElement a = (AnnotatedElement) member;
//            final XmlAttribute annotation = a.getAnnotation(XmlAttribute.class);
//            if (annotation.name().equals("##default")) {
//                return member instanceof Field ? member.getName() : fieldNameOfGetter(member.getName());
//            } else {
//                return annotation.name();
//            }
//        }
//
//        private static String getElementName(Member member) {
//            AnnotatedElement a = (AnnotatedElement) member;
//            final XmlElement annotation = a.getAnnotation(XmlElement.class);
//            if (annotation.name().equals("##default")) {
//                return member instanceof Field ? member.getName() : fieldNameOfGetter(member.getName());
//            } else {
//                return annotation.name();
//            }
//        }
//
//
//
//
//
//        private static String fieldNameOfGetter(String name) {
//            return name.substring(3);//todo configure if decapitalize
//        }
//
//        private static <K, V> void updateMultiMap(Map<K, List<V>> map, K key, V value) {
//            List<V> vs = map.get(key);
//            if (vs == null) {
//                vs = new ArrayList<>();
//            }
//            vs.add(value);
//            map.put(key, vs);
//        }
//
//    }

}