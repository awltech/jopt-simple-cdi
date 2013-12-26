package com.worldline.cdi4jopt.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation to declare a field as returning a
 * {@link joptsimple.OptionSet} object or a method returning a
 * {@link joptsimple.OptionSet} instance.
 * 
 * @author mvanbesien
 * @since 0.1
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface JOptOptions {

}
