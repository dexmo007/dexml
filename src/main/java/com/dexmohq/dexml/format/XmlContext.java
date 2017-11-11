package com.dexmohq.dexml.format;

import com.dexmohq.dexml.XmlFormatException;
import com.dexmohq.dexml.XmlParseException;
import com.dexmohq.dexml.XmlParser;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;


/**
 * Context of predefined reads and writes
 *
 * @author Henrik Drefs
 */
public class XmlContext {

    private static final int INTERFACE_HIERARCHY_DISTANCE = Integer.MAX_VALUE - 1;

    private final boolean climbHierarchy;

    private final Map<Class<?>, XmlWrites<?>> writesMap = new HashMap<>();
    private final Map<Class<?>, XmlReads<?>> readsMap = new HashMap<>();

    private final Map<Class<?>, XmlParser<?>> cache = new HashMap<>();

    private XmlContext(boolean climbHierarchy) {
        this.climbHierarchy = climbHierarchy;
        // register primitives
        registerFormat(Integer.class, PrimitiveFormats.INT_FORMAT);
        registerFormat(Integer.TYPE, PrimitiveFormats.INT_FORMAT);
        registerFormat(Long.class, PrimitiveFormats.LONG_FORMAT);
        registerFormat(Long.TYPE, PrimitiveFormats.LONG_FORMAT);
        registerFormat(Boolean.class, PrimitiveFormats.BOOLEAN_FORMAT);
        registerFormat(Boolean.TYPE, PrimitiveFormats.BOOLEAN_FORMAT);
        registerFormat(Double.class, PrimitiveFormats.DOUBLE_FORMAT);
        registerFormat(Double.TYPE, PrimitiveFormats.DOUBLE_FORMAT);
        registerFormat(Float.class, PrimitiveFormats.FLOAT_FORMAT);
        registerFormat(Float.TYPE, PrimitiveFormats.FLOAT_FORMAT);
        registerFormat(Character.class, PrimitiveFormats.CHARACTER_FORMAT);
        registerFormat(Character.TYPE, PrimitiveFormats.CHARACTER_FORMAT);
        registerFormat(Byte.class, PrimitiveFormats.BYTE_FORMAT);
        registerFormat(Byte.TYPE, PrimitiveFormats.BYTE_FORMAT);
        registerFormat(Short.class, PrimitiveFormats.SHORT_FORMAT);
        registerFormat(Short.TYPE, PrimitiveFormats.SHORT_FORMAT);
        //register default formats
        registerFormat(String.class, STRING_FORMAT);
        registerFormat(BigDecimal.class, BIG_DECIMAL_FORMAT);
        registerFormat(BigInteger.class, BIG_INTEGER_FORMAT);
        registerFormat(Date.class, UTIL_DATE_MILLIS_FORMAT);
        registerFormat(LocalDateTime.class, LOCAL_DATE_TIME_MILLIS_FORMAT);
        registerFormat(java.sql.Timestamp.class, SQL_TIMESTAMP_MILLIS_FORMAT);
        registerFormat(java.sql.Date.class, SQL_DATE_MILLIS_FORMAT);
        registerFormat(java.sql.Time.class, SQL_TIME_MILLIS_FORMAT);
    }

    public static XmlContext newDefault() {
        return new XmlContext(false);
    }

    @SuppressWarnings("unchecked")
    public <T> XmlParser<T> computeParserIfAbsent(Class<T> clazz) {
        return (XmlParser<T>) cache.computeIfAbsent(clazz, aClass -> XmlParser.create(clazz, this));
    }

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

    private static final XmlFormat<Date> UTIL_DATE_MILLIS_FORMAT = new XmlFormat<Date>() {
        @Override
        public Date read(String s) {
            return new Date(Long.parseLong(s));
        }

        @Override
        public String write(Date date) {
            return Long.toString(date.getTime());
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

    private static final XmlFormat<LocalDateTime> LOCAL_DATE_TIME_MILLIS_FORMAT = new XmlFormat<LocalDateTime>() {
        @Override
        public LocalDateTime read(String s) {
            return Instant.ofEpochMilli(Long.parseLong(s)).atZone(UTC_ZONE_ID).toLocalDateTime();
        }

        @Override
        public String write(LocalDateTime localDateTime) {
            return Long.toString(localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli());
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

    //java time Date & Time

    private static final XmlFormat<java.sql.Timestamp> SQL_TIMESTAMP_MILLIS_FORMAT = new XmlFormat<Timestamp>() {
        @Override
        public Timestamp read(String s) {
            return new Timestamp(Long.parseLong(s));
        }

        @Override
        public String write(Timestamp timestamp) {
            return Long.toString(timestamp.getTime());
        }
    };

    private static final XmlFormat<java.sql.Date> SQL_DATE_MILLIS_FORMAT = new XmlFormat<java.sql.Date>() {
        @Override
        public java.sql.Date read(String s) {
            return new java.sql.Date(Long.parseLong(s));
        }

        @Override
        public String write(java.sql.Date date) {
            return Long.toString(date.getTime());
        }
    };

    private static final XmlFormat<java.sql.Time> SQL_TIME_MILLIS_FORMAT = new XmlFormat<java.sql.Time>() {
        @Override
        public java.sql.Time read(String s) {
            return new java.sql.Time(Long.parseLong(s));
        }

        @Override
        public String write(java.sql.Time time) {
            return Long.toString(time.getTime());
        }
    };

}
