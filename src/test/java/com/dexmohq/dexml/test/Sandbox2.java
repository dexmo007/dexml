package com.dexmohq.dexml.test;

import com.dexmohq.dexml.DefaultWriter;
import com.dexmohq.dexml.NodeReader;
import com.dexmohq.dexml.NodeWriter;
import com.dexmohq.dexml.annotation.Immutable;
import com.dexmohq.dexml.annotation.Mutable;
import com.dexmohq.dexml.format.XmlContext;
import com.dexmohq.dexml.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.bind.annotation.XmlElement;

public class Sandbox2 {

    @Immutable
    public static class Tracking {
        private final int status;
        private final String id;
        private final String carrier;

        public Tracking(String id, String carrier, int status) {
            this.status = status;
            this.id = id;
            this.carrier = carrier;
        }

        public int getStatus() {
            return status;
        }

        public String getId() {
            return id;
        }

        public String getCarrier() {
            return carrier;
        }

        @Override
        public String toString() {
            return "Tracking{" +
                    "status=" + status +
                    ", id='" + id + '\'' +
                    ", carrier='" + carrier + '\'' +
                    '}';
        }
    }

    @Mutable
    public static class Foo {
        private int i;
        private String s;

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public String getS() {
            return s;
        }

        public void setS(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return "Foo{" +
                    "i=" + i +
                    ", s='" + s + '\'' +
                    '}';
        }
    }

    public static void main(String[] args) {
        final XmlContext context = XmlContext.newDefault();
        final DefaultWriter defaultWriter = new DefaultWriter(context);
//        final Node node = defaultWriter.write(new Tracking("uahsdiuasd", "DHL",4));
//        System.out.println(StringUtils.nodeToString(node));
//        final NodeReader<Tracking> reader = context.computeElementReaderIfAbsent(Tracking.class);
//        final Tracking read = reader.read(node.getFirstChild());
//        System.out.println(read);

        final Foo foo = new Foo();
        foo.setI(6);
        foo.setS("iuhausdh");
        final Node node = defaultWriter.write(foo);
        System.out.println(StringUtils.nodeToString(node));
        final Foo read = context.computeElementReaderIfAbsent(Foo.class).read(node.getFirstChild());
        System.out.println(read);

    }


}
