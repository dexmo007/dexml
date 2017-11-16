package com.dexmohq.dexml.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
public @interface Immutable {

    /**
     * if true an exception is thrown if the target type is not actually immutable (i.e. not all fields are final)
     */
    boolean strict() default true;

}
