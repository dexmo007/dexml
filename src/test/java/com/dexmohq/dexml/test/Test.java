package com.dexmohq.dexml.test;

import com.dexmohq.dexml.XmlParser;
import com.dexmohq.dexml.annotation.XmlUnwrapInConstruct;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class Test {

    static class Foo {
        @XmlAttribute
        private final boolean isProtected;
        @XmlValue
        private final String text;
        @XmlElement
        @XmlUnwrapInConstruct
        private final Bar bar;

        public Foo(boolean isProtected, String text, Bar bar) {
            this.isProtected = isProtected;
            this.text = text;
            this.bar = bar;
        }

        @Override
        public String toString() {
            return "Foo{" +
                    "isProtected=" + isProtected +
                    ", text='" + text + '\'' +
                    ", bar=" + bar +
                    '}';
        }
    }

    static class Bar {
        @XmlAttribute
        private final int count;
        @XmlValue
        private final String text;

        public Bar(int count, String text) {
            this.count = count;
            this.text = text;
        }

        @Override
        public String toString() {
            return "Bar{" +
                    "count=" + count +
                    ", text='" + text + '\'' +
                    '}';
        }
    }

    public static void main(String[] args) throws Exception {
        final XmlParser<Foo> parser = XmlParser.create(Foo.class);
        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final Document document = builder.newDocument();
        final Element root = document.createElement("foo");
        document.appendChild(root);
        root.setAttribute("isProtected", "true");
        root.setTextContent("This is a foolish op.");
        final Element bar = document.createElement("bar");
        bar.setAttribute("count", "50");
        bar.setTextContent("Nice bar!");
        root.appendChild(bar);

        System.out.println(parser.read(root));
    }
}
