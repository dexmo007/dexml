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
import java.time.*;
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
