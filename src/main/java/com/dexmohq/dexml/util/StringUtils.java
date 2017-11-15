package com.dexmohq.dexml.util;

import org.w3c.dom.Node;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
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

    public static String nodeToString(Node node) {
        StringWriter sw = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
        } catch (TransformerException te) {
            System.out.println("nodeToString Transformer Exception");
        }
        return sw.toString();
    }

}
