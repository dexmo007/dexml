package com.dexmohq.dexml.format;

import com.dexmohq.dexml.*;
import com.dexmohq.dexml.annotation.Immutable;
import com.dexmohq.dexml.annotation.Mutable;
import com.dexmohq.dexml.annotation.Transient;
import com.dexmohq.dexml.util.ArrayUtils;
import com.dexmohq.dexml.util.IndexedProperties;
import com.dexmohq.dexml.util.Property;
import com.dexmohq.dexml.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.dexmohq.dexml.util.ReflectUtils.isDerivedAnnotationPresent;


/**
 * Provides the context for parsing to and from XML, reveals configurability
 *
 * @author Henrik Drefs
 */
public class XmlContext {

    private static final int INTERFACE_HIERARCHY_DISTANCE = Integer.MAX_VALUE - 1;
    public static final Comparator<String> ALPHABETIC = String::compareTo;

    private final boolean climbHierarchy;

    private final Map<Class<?>, XmlWrites<?>> writesMap = new HashMap<>();
    private final Map<Class<?>, XmlReads<?>> readsMap = new HashMap<>();

    private final Map<Class<?>, NodeWriter<?>> cache = new HashMap<>();

    private final Map<Class<?>, NodeReader<?>> readerCache = new HashMap<>();

    protected XmlContext(boolean climbHierarchy) {
        this.climbHierarchy = climbHierarchy;
        // register primitives
        registerFormat(Integer.class, PrimitiveFormats.INT_FORMAT);
        registerFormat(int.class, PrimitiveFormats.INT_FORMAT);
        registerFormat(Long.class, PrimitiveFormats.LONG_FORMAT);
        registerFormat(long.class, PrimitiveFormats.LONG_FORMAT);
        registerFormat(Boolean.class, PrimitiveFormats.BOOLEAN_FORMAT);
        registerFormat(boolean.class, PrimitiveFormats.BOOLEAN_FORMAT);
        registerFormat(Double.class, PrimitiveFormats.DOUBLE_FORMAT);
        registerFormat(double.class, PrimitiveFormats.DOUBLE_FORMAT);
        registerFormat(Float.class, PrimitiveFormats.FLOAT_FORMAT);
        registerFormat(float.class, PrimitiveFormats.FLOAT_FORMAT);
        registerFormat(Character.class, PrimitiveFormats.CHARACTER_FORMAT);
        registerFormat(char.class, PrimitiveFormats.CHARACTER_FORMAT);
        registerFormat(Byte.class, PrimitiveFormats.BYTE_FORMAT);
        registerFormat(byte.class, PrimitiveFormats.BYTE_FORMAT);
        registerFormat(Short.class, PrimitiveFormats.SHORT_FORMAT);
        registerFormat(short.class, PrimitiveFormats.SHORT_FORMAT);
        //register default formats
        registerFormat(String.class, STRING_FORMAT);
        registerFormat(BigDecimal.class, BIG_DECIMAL_FORMAT);
        registerFormat(BigInteger.class, BIG_INTEGER_FORMAT);
        registerFormat(Date.class, UTIL_DATE_MILLIS_FORMAT);
        registerFormat(LocalDateTime.class, LOCAL_DATE_TIME_MILLIS_FORMAT);//todo nano time?
        registerFormat(LocalDate.class, LOCAL_DATE_MILLIS_FORMAT);
        registerFormat(LocalTime.class, LOCAL_TIME_MILLIS_FORMAT);
        registerFormat(java.sql.Timestamp.class, SQL_TIMESTAMP_MILLIS_FORMAT);
        registerFormat(java.sql.Date.class, SQL_DATE_MILLIS_FORMAT);
        registerFormat(java.sql.Time.class, SQL_TIME_MILLIS_FORMAT);
    }

    public static XmlContext newDefault(boolean climbHierarchy) {
        return new XmlContext(climbHierarchy);
    }

    public static XmlContext newDefault() {
        return newDefault(false);
    }

    private final DocumentBuilder documentBuilder = getDocumentBuilder();

    private static DocumentBuilder getDocumentBuilder() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public Document newDocument() {
        return documentBuilder.newDocument();
    }

    public XmlContext isoDateTimeFormatting() {
        registerFormat(Date.class, UTIL_DATE_ISO_FORMAT);
        registerFormat(LocalDateTime.class, LOCAL_DATE_TIME_ISO_FORMAT);
        registerFormat(LocalDate.class, LOCAL_DATE_ISO_FORMAT);
        registerFormat(LocalTime.class, LOCAL_TIME_ISO_FORMAT);
        registerFormat(java.sql.Timestamp.class, SQL_TIMESTAMP_ISO_FORMAT);
        registerFormat(java.sql.Date.class, SQL_DATE_ISO_FORMAT);
        registerFormat(java.sql.Time.class, SQL_TIME_ISO_FORMAT);
        return this;
    }

    public String toTagName(String name) {
        return StringUtils.transformCamelCase(name, "-");
    }

    public String toAttributeName(String name) {
        return StringUtils.transformCamelCase(name, "_");
    }

    @SuppressWarnings("unchecked")
    public <T> XmlWrites<T> computeAnnotatedWritesIfAbsent(Class<T> clazz) {
        return (XmlWrites<T>) writesMap.computeIfAbsent(clazz, AnnotatedValueWrites::create);
    }

    @SuppressWarnings("unchecked")
    public <T> XmlReads<T> computeAnnotatedReadsIfAbsent(Class<T> clazz) {
        return (XmlReads<T>) readsMap.computeIfAbsent(clazz, AnnotatedValueReads::new);
    }

    public <T> XmlFormat<T> computeAnnotatedFormatIfAbsent(Class<T> clazz) {
        final XmlReads<?> reads = readsMap.computeIfAbsent(clazz, AnnotatedFormat::new);
//        if (reads)
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> NodeWriter<T> computeElementWriterIfAbsent(Class<T> type) {
        return (NodeWriter<T>) cache.computeIfAbsent(type, aClass -> new ElementWriter<>(type, this));
    }

    @SuppressWarnings("unchecked")
    public <T> NodeReader<T> computeNodeReaderIfAbsent(short nodeType, Class<T> type) {
        switch (nodeType) {
            case Node.ATTRIBUTE_NODE:
            case Node.TEXT_NODE:
                final XmlReads<?> xmlReads = readsMap.get(type);
                if (xmlReads == null) {
                    throw new XmlParseException("No reads found for type: " + type);
                }
                return node -> (T) xmlReads.read(node.getNodeValue());
            case Node.ELEMENT_NODE:
                return (NodeReader<T>) readerCache.computeIfAbsent(type,
                        this::newElementReader);
            default:
                throw new IllegalArgumentException("Node type (" + nodeType + ") not supported while reading");

        }
    }

    @SuppressWarnings("unchecked")
    public <T> NodeReader<T> computeElementReaderIfAbsent(Class<T> type) {
        return (NodeReader<T>) readerCache.computeIfAbsent(type, this::newElementReader);
    }

    private <T> NodeReader<T> newElementReader(Class<T> type) {
        if (type.isAnnotationPresent(Immutable.class)) {
            return new ImmutableNodeReader<>(type, this);
        }
        if (type.isAnnotationPresent(Mutable.class)) {
            return MutableNodeReader.create(type, this);
        }

        return new ImmutableNodeReader<>(type, this);//todo decide mutability
    }

//    /**
//     * If a format exists for the given type, an attribute parser is returned;
//     * otherwise an element parser is returned
//     *
//     * @param type class of the type to be parsed
//     * @param <T>  type to be parsed
//     * @return an attribute or element parser
//     */
//    public <T> NodeWriter<T> getArbitraryWriter(Class<T> type) {
//        final Optional<XmlFormat<T>> formatOptional = getFormatOptional(type);
//        if (formatOptional.isPresent()) {
//            final XmlFormat<T> format = formatOptional.get();
//            return new AttributeWriter<>(format, this);
//        }
//        return computeElementWriterIfAbsent(type);
//    }

    /**
     * Registers writes to the given type
     *
     * @param clazz  class of the type of the writes to register
     * @param writes the instance of writes to register
     * @param <T>    type of the writes to register
     */
    public <T> XmlContext registerWrites(Class<T> clazz, XmlWrites<T> writes) {
        writesMap.put(clazz, writes);
        return this;
    }

    /**
     * Register reads to the given type
     *
     * @param clazz class of the type of the reads to register
     * @param reads the instance of reads to register
     * @param <T>   type of the reads to register
     */
    public <T> XmlContext registerReads(Class<T> clazz, XmlReads<T> reads) {
        readsMap.put(clazz, reads);
        return this;
    }

    /**
     * Registers a given format as reads and writes
     *
     * @param clazz  class of the type of the format to register
     * @param format the instance of the format to register
     * @param <T>    type of the format to register
     */
    public <T> XmlContext registerFormat(Class<T> clazz, XmlFormat<T> format) {
        writesMap.put(clazz, format);
        readsMap.put(clazz, format);
        return this;
    }

    /**
     * If exactly matching reads to the given type are register, those are returned;
     * otherwise it is looked for reads of a subclass of the given type.
     * If none or multiple are found, an exception is thrown.
     *
     * @param clazz class of the type of the wanted reads
     * @param <T>   type of the wanted reads
     * @return matching reads
     * @throws XmlFormatException if no matching reads are found
     */
    @SuppressWarnings("unchecked")
    public <T> XmlReads<T> getReads(Class<T> clazz) {
        final XmlReads<T> v = (XmlReads<T>) readsMap.get(clazz);
        if (v != null) {
            return v;
        }
        if (climbHierarchy) {
            XmlReads<?> match = null;
            int best = Integer.MAX_VALUE;
            boolean multipleMatches = false;
            for (Map.Entry<Class<?>, XmlReads<?>> binding : readsMap.entrySet()) {
                final Class<?> currentClass = binding.getKey();
                if (clazz.isAssignableFrom(currentClass)) {
                    int distance = 0;
                    for (Class<?> c = currentClass; c != clazz; c = c.getSuperclass()) {
                        distance++;
                    }

                    //noinspection Duplicates: Removal of duplicity would include introduction of new class which is definitely not effective in this case
                    if (distance < best) {
                        best = distance;
                        match = binding.getValue();
                        multipleMatches = false;
                    } else if (distance == best) {
                        multipleMatches = true;
                    }
                }
            }
            if (match == null)
                throw new XmlFormatException("No matching reads found for type " + clazz);
            if (multipleMatches) {
                throw new XmlFormatException("Multiple matching reads found for type " + clazz);
            }
            final XmlReads<?> reads = match;
            return s -> (T) reads.read(s);
        }
        throw new XmlFormatException("No reads founds for type " + clazz);
    }

    public <T> Optional<XmlReads<T>> getReadsOptional(Class<T> clazz) {
        try {
            return Optional.of(getReads(clazz));
        } catch (XmlFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * If exactly matching writes to the given type are register, those are returned;
     * otherwise it is looked for a writes of the closest superclass or interface.
     * Ff none or multiple are found, an exception is thrown.
     *
     * @param clazz class of the type of the wanted writes
     * @param <T>   type of the wanted writes
     * @return matching writes
     * @throws XmlFormatException if no matching writes are found
     */
    @SuppressWarnings("unchecked")
    public <T> XmlWrites<T> getWrites(Class<T> clazz) {
        final XmlWrites<T> v = (XmlWrites<T>) writesMap.get(clazz);
        if (v != null) {
            return v;
        }
        if (climbHierarchy) {
            XmlWrites<?> match = null;
            int best = Integer.MAX_VALUE;
            boolean multipleMatches = false;
            for (Map.Entry<Class<?>, XmlWrites<?>> binding : writesMap.entrySet()) {
                final Class<?> currentClass = binding.getKey();
                if (currentClass.isAssignableFrom(clazz)) {
                    int distance = 0;
                    if (currentClass.isInterface()) {
                        distance = INTERFACE_HIERARCHY_DISTANCE;
                    } else {
                        for (Class<?> c = clazz; c != currentClass; c = c.getSuperclass()) {
                            distance++;
                        }
                    }
                    //noinspection Duplicates: Removal of duplicity would include introduction of new class which is definitely not effective in this case
                    if (distance < best) {
                        best = distance;
                        match = binding.getValue();
                        multipleMatches = false;
                    } else if (distance == best) {
                        multipleMatches = true;
                    }
                }
            }
            if (match == null)
                throw new XmlFormatException("No matching writes found for type " + clazz);
            if (multipleMatches) {
                throw new XmlFormatException("Multiple matching writes found for type " + clazz);
            }
            return (XmlWrites) match;
        }
        throw new XmlFormatException("No writes found for type " + clazz);
    }

    public <T> Optional<XmlWrites<T>> getWritesOptional(Class<T> clazz) {
        try {
            return Optional.of(getWrites(clazz));
        } catch (XmlFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Looks for matching reads and writes and combines them to a format
     *
     * @param clazz class of the type of the wanted format
     * @param <T>   type of the wanted format
     * @return matching format
     * @throws XmlFormatException if no matching format is found
     */
    public <T> XmlFormat<T> getFormat(Class<T> clazz) {
        final XmlReads<T> reads = getReads(clazz);
        // if a format was registered, the reads will be instance of format;
        // otherwise writes cannot be instanceof format either
        if (reads instanceof XmlFormat)
            return (XmlFormat<T>) reads;
        final XmlWrites<T> writes = getWrites(clazz);
        return XmlFormat.fromReadsAndWrites(reads, writes);
    }

    /**
     * Safe tries to get a format for a given type
     *
     * @param clazz class of the type of the wanted format
     * @param <T>   type of the wanted format
     * @return if format exists, an optional containing the format, otherwise an empty optional
     */
    public <T> Optional<XmlFormat<T>> getFormatOptional(Class<T> clazz) {
        try {
            return Optional.of(getFormat(clazz));
        } catch (XmlFormatException e) {
            return Optional.empty();
        }
    }

    /*
    Default format implementations
     */

    private static final XmlFormat<String> STRING_FORMAT = new XmlFormat<String>() {
        @Override
        public String read(String s) {
            return s;
        }

        @Override
        public String write(String s) {
            return s;
        }
    };

    private static final XmlFormat<BigDecimal> BIG_DECIMAL_FORMAT = new XmlFormat<BigDecimal>() {
        @Override
        public BigDecimal read(String s) {
            return new BigDecimal(s);
        }

        @Override
        public String write(BigDecimal bigDecimal) {
            return bigDecimal.toString();
        }
    };

    private static final XmlFormat<BigInteger> BIG_INTEGER_FORMAT = new XmlFormat<BigInteger>() {
        @Override
        public BigInteger read(String s) {
            return new BigInteger(s);
        }

        @Override
        public String write(BigInteger bigInteger) {
            return bigInteger.toString();
        }
    };

    private final Set<Class<? extends Annotation>> supportedTransientAnnotations = new HashSet<>(
            Arrays.asList(Transient.class, XmlTransient.class));

    public XmlContext supportTransientAnnotation(Class<? extends Annotation> transientAnnotation) {
        supportedTransientAnnotations.add(transientAnnotation);
        return this;
    }

    public XmlContext supportJAXB() {
        supportTransientAnnotation(XmlTransient.class);
        return this;
    }

    public XmlContext disableJAXBSupport() {
        supportedTransientAnnotations.remove(XmlTransient.class);
        return this;
    }

    //todo jackson support
    //todo gson support

    /**
     * Returns an ordered list of all relevant (i.e. non-transient) properties
     *
     * @param type
     * @return
     */
    public Map<String, Property> getProperties(Class<?> type) {//todo follow annotated ordering
        final BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(type, Object.class);
        } catch (IntrospectionException e) {
            throw new XmlConfigurationException("Could not get bean info");
        }
        final Map<String, Property> map;
//        map = new TreeMap<>(ALPHABETIC); todo configurable
        final PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        map = new IndexedProperties(propertyDescriptors.length);//todo by given order
        int index = 0;
        for (PropertyDescriptor descriptor : propertyDescriptors) {//todo scan fields and allow node info to work with a field as well
            final Method getter = descriptor.getReadMethod();
            final Method setter = descriptor.getWriteMethod();
            Field field = null;
            try {
                field = type.getDeclaredField(descriptor.getName());
            } catch (NoSuchFieldException e) {
                // fall
            }
            // skip properties marked as transient
            if (!isTransient(getter, setter, field, field != null ? field.getAnnotations() : new Annotation[0])) {
                //todo throw exception if also annotated with xml type
                final String name = descriptor.getName();
                Annotation xmlType = tryGetXmlType(getter.getAnnotations(), null, name);
                if (setter != null)
                    xmlType = tryGetXmlType(setter.getAnnotations(), xmlType, name);
                if (field != null)
                    xmlType = tryGetXmlType(field.getAnnotations(), xmlType, name);
                final Class<?> propertyType = descriptor.getPropertyType();
                final short nodeType;
                final String xmlName;
                if (xmlType == null) {
                    if (writesMap.containsKey(propertyType)) {
                        nodeType = Node.ATTRIBUTE_NODE;
                        xmlName = toAttributeName(descriptor.getName());
                    } else {
                        nodeType = Node.ELEMENT_NODE;
                        xmlName = toTagName(descriptor.getName());
                    }
                } else {
                    if (xmlType.annotationType() == XmlValue.class) {
                        nodeType = Node.TEXT_NODE;
                        xmlName = VALUE_IDENTIFIER;
                    } else {
                        xmlName = extractName(xmlType, name);
                        if (xmlType.annotationType() == XmlAttribute.class) {
                            nodeType = Node.ATTRIBUTE_NODE;
                        } else { // @XmlElement
                            nodeType = Node.ELEMENT_NODE;
                        }
                    }
                }
                if (map.put(descriptor.getName(), new Property(xmlName, descriptor, field, nodeType, index++)) != null) {
                    throw new XmlConfigurationException("Duplicate names defined: " + descriptor.getName());
                }
            }
        }
        return map;
    }

    private static final String DEFAULT_NAME_IDENTIFIER = "##default";
    private static final String VALUE_IDENTIFIER = "#text";

    private String extractName(Annotation a, String fieldName) {
        if (a instanceof XmlAttribute) {
            final String name = ((XmlAttribute) a).name();
            return name.equals(DEFAULT_NAME_IDENTIFIER) ? toAttributeName(fieldName) : name;
        } else {
            final String name = ((XmlElement) a).name();
            return name.equals(DEFAULT_NAME_IDENTIFIER) ? toTagName(fieldName) : name;
        }
    }

    private static Annotation tryGetXmlType(Annotation[] annotations, Annotation previous, String name) {
        for (Annotation a : annotations) {
            final Class<? extends Annotation> annotationType = a.annotationType();
            if (annotationType == XmlElement.class
                    || annotationType == XmlAttribute.class
                    || annotationType == XmlValue.class) {
                if (previous != null) {
                    throw new XmlConfigurationException("Ambiguous xml type annotations for property: " + name);
                }
                previous = a;
            }
        }
        return previous;
    }

    public boolean isTransient(Method getter, Method setter, Field field, Annotation[] fieldAnnotations) {
        return field != null && Modifier.isTransient(field.getModifiers())
                || supportedTransientAnnotations.stream().anyMatch(a ->
                isDerivedAnnotationPresent(getter, a)
                        || isDerivedAnnotationPresent(setter, a)
                        || ArrayUtils.containsType(fieldAnnotations, a));
    }

    public static abstract class XmlFormatAsLong<T> implements XmlFormat<T> {
        public abstract long toLong(T t);

        public abstract T fromLong(long l);

        @Override
        public T read(String s) {
            return fromLong(Long.parseLong(s));
        }

        @Override
        public String write(T t) {
            return Long.toString(toLong(t));
        }
    }

    private static final XmlFormat<Date> UTIL_DATE_MILLIS_FORMAT = new XmlFormatAsLong<Date>() {
        @Override
        public long toLong(Date date) {
            return date.getTime();
        }

        @Override
        public Date fromLong(long l) {
            return new Date(l);
        }
    };

    private static final XmlFormat<Date> UTIL_DATE_ISO_FORMAT = new UtilDateIsoFormat();

    private static class UtilDateIsoFormat implements XmlFormat<Date> {
        private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
        private static final DateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

        static {
            ISO_DATE_FORMAT.setTimeZone(UTC);
        }

        @Override
        public Date read(String s) {
            try {
                return ISO_DATE_FORMAT.parse(s);
            } catch (ParseException e) {
                throw new XmlParseException(e);
            }
        }

        @Override
        public String write(Date date) {
            return ISO_DATE_FORMAT.format(date);
        }
    }

    private static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");

    private static final XmlFormat<LocalDateTime> LOCAL_DATE_TIME_MILLIS_FORMAT = new XmlFormatAsLong<LocalDateTime>() {
        @Override
        public LocalDateTime fromLong(long l) {
            return Instant.ofEpochMilli(l).atZone(UTC_ZONE_ID).toLocalDateTime();
        }

        @Override
        public long toLong(LocalDateTime localDateTime) {
            return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
        }
    };

    private static final XmlFormat<LocalDateTime> LOCAL_DATE_TIME_ISO_FORMAT = new XmlFormat<LocalDateTime>() {
        @Override
        public LocalDateTime read(String s) {
            return LocalDateTime.parse(s);
        }

        @Override
        public String write(LocalDateTime localDateTime) {
            return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime);
        }
    };

    private static final XmlFormat<java.time.LocalDate> LOCAL_DATE_MILLIS_FORMAT = new XmlFormatAsLong<LocalDate>() {
        @Override
        public long toLong(LocalDate localDate) {
            return localDate.toEpochDay();
        }

        @Override
        public LocalDate fromLong(long l) {
            return LocalDate.ofEpochDay(l);
        }
    };

    private static final XmlFormat<LocalDate> LOCAL_DATE_ISO_FORMAT = new XmlFormat<LocalDate>() {
        @Override
        public LocalDate read(String s) {
            return LocalDate.parse(s);
        }

        @Override
        public String write(LocalDate localDate) {
            return DateTimeFormatter.ISO_LOCAL_DATE.format(localDate);
        }
    };

    private static final XmlFormat<java.time.LocalTime> LOCAL_TIME_MILLIS_FORMAT = new XmlFormatAsLong<LocalTime>() {
        @Override
        public long toLong(LocalTime localTime) {
            return localTime.toNanoOfDay();
        }

        @Override
        public LocalTime fromLong(long l) {
            return LocalTime.ofNanoOfDay(l);
        }
    };

    private static final XmlFormat<LocalTime> LOCAL_TIME_ISO_FORMAT = new XmlFormat<LocalTime>() {
        @Override
        public LocalTime read(String s) {
            return LocalTime.parse(s);
        }

        @Override
        public String write(LocalTime localTime) {
            return DateTimeFormatter.ISO_LOCAL_TIME.format(localTime);
        }
    };

    private static final XmlFormat<java.sql.Timestamp> SQL_TIMESTAMP_MILLIS_FORMAT = new XmlFormatAsLong<Timestamp>() {
        @Override
        public long toLong(Timestamp timestamp) {
            return timestamp.getTime();
        }

        @Override
        public Timestamp fromLong(long l) {
            return new Timestamp(l);
        }
    };

    private static final XmlFormat<java.sql.Timestamp> SQL_TIMESTAMP_ISO_FORMAT = new XmlFormat<java.sql.Timestamp>() {

        @Override
        public String write(java.sql.Timestamp timestamp) {
            return LOCAL_DATE_TIME_ISO_FORMAT.write(timestamp.toLocalDateTime());
        }

        @Override
        public java.sql.Timestamp read(String s) {
            return java.sql.Timestamp.valueOf(LOCAL_DATE_TIME_ISO_FORMAT.read(s));
        }
    };

    private static final XmlFormat<java.sql.Date> SQL_DATE_MILLIS_FORMAT = new XmlFormatAsLong<java.sql.Date>() {
        @Override
        public long toLong(java.sql.Date date) {
            return date.getTime();
        }

        @Override
        public java.sql.Date fromLong(long l) {
            return new java.sql.Date(l);
        }
    };

    private static final XmlFormat<java.sql.Date> SQL_DATE_ISO_FORMAT = new XmlFormat<java.sql.Date>() {
        @Override
        public java.sql.Date read(String s) {
            return java.sql.Date.valueOf(LOCAL_DATE_ISO_FORMAT.read(s));
        }

        @Override
        public String write(java.sql.Date date) {
            return LOCAL_DATE_ISO_FORMAT.write(date.toLocalDate());
        }
    };

    private static final XmlFormat<java.sql.Time> SQL_TIME_MILLIS_FORMAT = new XmlFormatAsLong<java.sql.Time>() {
        @Override
        public long toLong(java.sql.Time time) {
            return time.getTime();
        }

        @Override
        public java.sql.Time fromLong(long l) {
            return new java.sql.Time(l);
        }
    };

    private static final XmlFormat<java.sql.Time> SQL_TIME_ISO_FORMAT = new XmlFormat<java.sql.Time>() {
        @Override
        public java.sql.Time read(String s) {
            return java.sql.Time.valueOf(LOCAL_TIME_ISO_FORMAT.read(s));
        }

        @Override
        public String write(java.sql.Time time) {
            return LOCAL_TIME_ISO_FORMAT.write(time.toLocalTime());
        }
    };

}
