package com.dexmohq.dexml.test;

import com.dexmohq.dexml.XmlParser;
import com.dexmohq.dexml.XmlParserFactory;
import com.dexmohq.dexml.util.ArrayUtils;
import org.w3c.dom.Node;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.Arrays;

public class Test {

    public static class FooInfo {
        @XmlAttribute
        private final boolean isProtected;
        @XmlAttribute
        private final String awesomeText;
        @XmlElement
        private final BarCounter barCounter;

        public FooInfo(boolean isProtected, String awesomeText, BarCounter barCounter) {
            this.isProtected = isProtected;
            this.awesomeText = awesomeText;
            this.barCounter = barCounter;
        }

        public boolean isProtected() {
            return isProtected;
        }

        public String getAwesomeText() {
            return awesomeText;
        }

        public BarCounter getBarCounter() {
            return barCounter;
        }

        @Override
        public String toString() {
            return "FooInfo{" +
                    "isProtected=" + isProtected +
                    ", awesomeText='" + awesomeText + '\'' +
                    ", barCounter=" + barCounter +
                    '}';
        }
    }

    public static class BarCounter {
        @XmlAttribute
        private final int count;
        @XmlValue
        private final String text;

        public BarCounter(int count, String text) {
            this.count = count;
            this.text = text;
        }

        public int getCount() {
            return count;
        }

        public String getText() {
            return text;
        }

        @Override
        public String toString() {
            return "BarCounter{" +
                    "count=" + count +
                    ", awesomeText='" + text + '\'' +
                    '}';
        }
    }

    private static String nodeToString(Node node) {
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

    public static void main(String[] args) throws Exception {

        final Annotation[] annotations = FooInfo.class.getDeclaredField("awesomeText").getAnnotations();
        System.out.println(Arrays.toString(annotations));
        System.out.println(ArrayUtils.containsType(annotations, XmlTransient.class));
        System.out.println("==========================================");

        final XmlParser parser = XmlParserFactory.createParser();

        final FooInfo fooInfo = new FooInfo(true, "i love my skills", new BarCounter(7, "bartenderness"));

        final Node xml = parser.write(fooInfo);

        System.out.println(nodeToString(xml));


//        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//        final Document document = builder.newDocument();
//        final Element root = document.createElement("fooInfo");
//        document.appendChild(root);
//        root.setAttribute("isProtected", "true");
//        root.setTextContent("This is a foolish op.");
//        final Element barCounter = document.createElement("barCounter");
//        barCounter.setAttribute("count", "50");
//        barCounter.setTextContent("Nice barCounter!");
//        root.appendChild(barCounter);

//        System.out.println(parser.read(root));
    }
}
