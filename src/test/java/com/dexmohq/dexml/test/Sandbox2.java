package com.dexmohq.dexml.test;

import com.dexmohq.dexml.annotation.Immutable;
import org.w3c.dom.Node;

public class Sandbox2 {

    @Immutable
    public static class Tracking {
        private final int status;
        private final String id;
        private final String carrier;

        public Tracking(int status, String id, String carrier) {
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
    }

    private static <T> void readImmutable(Class<T> clazz, Node node) {
        final Immutable annotation = clazz.getAnnotation(Immutable.class);
        final boolean strict = annotation != null && annotation.strict();//non-strict mode if not explicitly annotated

    }


}
