package com.dexmohq.dexml.format;

import com.dexmohq.dexml.XmlParseException;

final class PrimitiveFormats {

    static final XmlFormat<Integer> INT_FORMAT = new XmlFormat<Integer>() {
        @Override
        public Integer read(String s) {
            return Integer.parseInt(s);
        }

        @Override
        public String write(Integer integer) {
            return Integer.toString(integer);
        }
    };

    static final XmlFormat<Long> LONG_FORMAT = new XmlFormat<Long>() {
        @Override
        public Long read(String s) {
            return Long.parseLong(s);
        }

        @Override
        public String write(Long aLong) {
            return Long.toString(aLong);
        }
    };

    static final XmlFormat<Boolean> BOOLEAN_FORMAT = new XmlFormat<Boolean>() {
        @Override
        public Boolean read(String s) {
            return Boolean.parseBoolean(s);
        }

        @Override
        public String write(Boolean aBoolean) {
            return Boolean.toString(aBoolean);
        }
    };

    static final XmlFormat<Double> DOUBLE_FORMAT = new XmlFormat<Double>() {
        @Override
        public Double read(String s) {
            return Double.parseDouble(s);
        }

        @Override
        public String write(Double aDouble) {
            return Double.toString(aDouble);
        }
    };

    static final XmlFormat<Byte> BYTE_FORMAT = new XmlFormat<Byte>() {
        @Override
        public Byte read(String s) {
            return Byte.parseByte(s);
        }

        @Override
        public String write(Byte aByte) {
            return Byte.toString(aByte);
        }
    };

    static final XmlFormat<Short> SHORT_FORMAT = new XmlFormat<Short>() {
        @Override
        public Short read(String s) {
            return Short.parseShort(s);
        }

        @Override
        public String write(Short aShort) {
            return Short.toString(aShort);
        }
    };

    static final XmlFormat<Float> FLOAT_FORMAT = new XmlFormat<Float>() {
        @Override
        public Float read(String s) {
            return Float.parseFloat(s);
        }

        @Override
        public String write(Float aFloat) {
            return Float.toString(aFloat);
        }
    };

    static final XmlFormat<Character> CHARACTER_FORMAT = new XmlFormat<Character>() {
        @Override
        public Character read(String s) {
            if (s.length() == 1)
                return s.charAt(0);
            throw new XmlParseException("too long for character");
        }

        @Override
        public String write(Character character) {
            return Character.toString(character);
        }
    };

}
