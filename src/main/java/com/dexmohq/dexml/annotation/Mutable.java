package com.dexmohq.dexml.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
public @interface Mutable {

    /**
     * Whether to use the actual field or a setter to set a value
     *
     * @return
     */
    AccessorType accessorType() default AccessorType.AUTO;

    /**
     * whether the empty constructor or the accessors can be inaccessible
     *
     * @return
     */
    boolean allowInaccessible() default false;


    enum AccessorType {
        AUTO, FIELD, SETTER
    }

}
