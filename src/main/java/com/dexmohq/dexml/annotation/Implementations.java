package com.dexmohq.dexml.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
public @interface Implementations {

    Class[] value();

    Mapping mapping() default Mapping.BY_TYPE;

    String xmlMemberName() default "type";

    enum Mapping {
        /**
         * Uses the type's simple name
         */
        BY_TYPE,
        /**
         * Uses the type's fully qualified name (package.class)
         */
        BY_FULL_TYPE,
        /**
         * Takes the simple class name, but strip the name of the abstract superclass if suffixed
         */
        BY_STRIPPED_NAME,
        /**
         *
         * Uses the type name specified by an annotation
         * @see MappedTypeName
         */
        BY_MAPPED_TYPE_NAME
    }

}
