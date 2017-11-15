package com.dexmohq.dexml.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Targets a public static method to read an object of a class annotated with @ReadsString from a string,
 * therefore the target method must return a type that is assignable to the type to be read
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface ReadsMethod {
}
