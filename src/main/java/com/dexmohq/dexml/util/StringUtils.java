package com.dexmohq.dexml.util;

import java.util.StringJoiner;

public final class StringUtils {

    private StringUtils(){}

    public static String decapitalize(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    public static String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static String transformCamelCase(String s, String delimiter) {
        final StringJoiner joiner = new StringJoiner(delimiter);
        int lastIndex = 0;
        final char[] chars = s.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        for (int i = 0; i < chars.length; i++) {
            final char c = chars[i];
            if (Character.isUpperCase(c)) {
                joiner.add(new String(chars, lastIndex, i - lastIndex).toLowerCase());
                lastIndex = i;
            }
        }
        joiner.add(new String(chars, lastIndex, chars.length - lastIndex).toLowerCase());
        return joiner.toString();
    }//todo unit tests

}
