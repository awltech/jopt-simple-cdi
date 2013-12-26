package com.worldline.cdi4jopt.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to declare a field of a class, to be an argument of the command
 * line.
 * 
 * @author mvanbesien
 * @since 0.1
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface JOptArgument {

	/**
	 * Argument Name
	 */
	public String name();

	/**
	 * True if argument is required, false otherwise
	 * 
	 * @return
	 */
	public boolean required() default false;
	
	/**
	 * Argument description
	 */
	public String description() default "";

}