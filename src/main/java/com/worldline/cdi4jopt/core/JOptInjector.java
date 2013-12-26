package com.worldline.cdi4jopt.core;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

import com.worldline.cdi4jopt.annotations.JOptArgument;
import com.worldline.cdi4jopt.annotations.JOptOptions;
import com.worldline.cdi4jopt.annotations.JOptParser;

/**
 * Class, to be linked to a pojo, that loads a parser for it, and injects the
 * resulting values on demand.
 * 
 * @author mvanbesien
 * @since 0.1
 * 
 */
public class JOptInjector {

	/**
	 * Object being configured and injected
	 */
	private Object pojo = null;

	/**
	 * Option Parser instance, for this object
	 */
	private OptionParser parser = null;

	/**
	 * Map of the fields to inject, mapped with the corresponding option name.
	 */
	private final Map<Field, String> argumentFields = new HashMap<Field, String>();

	/**
	 * Creates new JOpt Injector instance for the provided pojo.
	 * 
	 * @param pojo
	 */
	public JOptInjector(final Object pojo) {
		this.pojo = pojo;
		this.parser = getOptionParser(this.pojo);
		this.configureParser();
	}

	/**
	 * Returns true if the object underneath is injectable. This means that a
	 * {@link joptsimple.OptionSet} and a {@link joptsimple.OptionParser} could
	 * be retrieved for it.
	 * 
	 * @return
	 */
	public boolean isInjectable() {
		return this.parser != null;
	}

	/**
	 * Generic method that uses reflection to invoke a method of a class, and
	 * return the object if type matches with class.
	 * 
	 * @param o
	 * @param method
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static <T> T invoke(final Object o, final Method method, final Class<T> clazz) {

		if (method == null) {
			return null;
		}

		if (!method.isAccessible()) {
			method.setAccessible(true);
		}
		try {
			final Object result = method.invoke(o);

			if (result != null && clazz.isAssignableFrom(result.getClass())) {
				return (T) result;
			}

		} catch (final IllegalAccessException e) {
			e.printStackTrace();
		} catch (final IllegalArgumentException e) {
			e.printStackTrace();
		} catch (final InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Configures the parser by retrieving all the fields annotated with
	 * JOptArgument, and setting them as valid parameters
	 */
	private void configureParser() {

		if (!this.isInjectable()) {
			return;
		}

		this.parser.allowsUnrecognizedOptions();
		this.argumentFields.clear();
		final Field[] declaredFields = this.pojo.getClass().getDeclaredFields();
		for (final Field declaredField : declaredFields) {
			if (declaredField.getAnnotation(JOptArgument.class) != null) {
				final JOptArgument annotation = declaredField.getAnnotation(JOptArgument.class);
				try {
					// Load the corresponding annotation values
					final String name = invoke(annotation, JOptArgument.class.getDeclaredMethod("name"), String.class);
					final boolean isRequired = invoke(annotation, JOptArgument.class.getDeclaredMethod("required"),
							Boolean.class);
					final String description = invoke(annotation, JOptArgument.class.getDeclaredMethod("description"),
							String.class);
					final Class<?> type = declaredField.getType();

					// Make the field accessible if it is not
					if (!declaredField.isAccessible()) {
						declaredField.setAccessible(true);
					}

					// With them, init the parser.
					final OptionSpecBuilder accepts = this.parser.accepts(name);
					if (isRequired) {
						accepts.withRequiredArg().ofType(type).describedAs(description);
					} else {
						accepts.withOptionalArg().ofType(type).describedAs(description);
					}

					this.argumentFields.put(declaredField, name);
				} catch (final IllegalArgumentException e) {
					e.printStackTrace();
				} catch (final NoSuchMethodException e) {
					e.printStackTrace();
				} catch (final SecurityException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * Performs the injection to the fields annotated with JOptArgument
	 */
	public void inject() {

		if (!this.isInjectable()) {
			return;
		}

		final OptionSet optionSet = getOptionSet(this.pojo);
		for (final Field field : this.argumentFields.keySet()) {
			final String optionName = this.argumentFields.get(field);
			if (optionSet.has(optionName)) {
				try {
					field.set(this.pojo, optionSet.valueOf(optionName));
				} catch (final IllegalArgumentException e) {
					e.printStackTrace();
				} catch (final IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Returns the OptionSet handled by this object.
	 * 
	 * @param o
	 * @return
	 */
	private static OptionSet getOptionSet(final Object o) {
		final Class<?> clazz = o.getClass();
		final Method[] declaredMethods = clazz.getDeclaredMethods();
		Field optionsField = null;

		final Field[] declaredFields = clazz.getDeclaredFields();
		Method optionsMethod = null;

		for (int i = 0; i < declaredFields.length && optionsField == null; i++) {
			final Field declaredField = declaredFields[i];
			if (declaredField.getAnnotation(JOptOptions.class) != null) {
				optionsField = declaredField;
			}
		}

		if (optionsField != null) {
			if (!optionsField.isAccessible()) {
				optionsField.setAccessible(true);
			}
			try {
				return (OptionSet) optionsField.get(o);
			} catch (final IllegalArgumentException e) {
				e.printStackTrace();
			} catch (final IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		for (int i = 0; i < declaredMethods.length && optionsMethod == null; i++) {
			final Method declaredMethod = declaredMethods[i];
			if (declaredMethod.getAnnotation(JOptOptions.class) != null) {
				optionsMethod = declaredMethod;
			}
		}

		if (optionsMethod != null && optionsMethod.getParameterTypes().length == 0) {
			return invoke(o, optionsMethod, OptionSet.class);
		}
		return null;

	}

	/**
	 * Returns the Option Parser handled by this object.
	 * 
	 * @param o
	 * @return
	 */
	private static OptionParser getOptionParser(final Object o) {
		final Class<?> clazz = o.getClass();
		final Method[] declaredMethods = clazz.getDeclaredMethods();
		Field parserField = null;

		final Field[] declaredFields = clazz.getDeclaredFields();
		Method parserMethod = null;

		for (int i = 0; i < declaredFields.length && parserField == null; i++) {
			final Field declaredField = declaredFields[i];
			if (declaredField.getAnnotation(JOptParser.class) != null) {
				parserField = declaredField;
			}
		}

		if (parserField != null) {
			if (!parserField.isAccessible()) {
				parserField.setAccessible(true);
			}
			try {
				return (OptionParser) parserField.get(o);
			} catch (final IllegalArgumentException e) {
				e.printStackTrace();
			} catch (final IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		for (int i = 0; i < declaredMethods.length && parserMethod == null; i++) {
			final Method declaredMethod = declaredMethods[i];
			if (declaredMethod.getAnnotation(JOptParser.class) != null) {
				parserMethod = declaredMethod;
			}
		}
		if (parserMethod != null && parserMethod.getParameterTypes().length == 0) {
			return invoke(o, parserMethod, OptionParser.class);
		}
		return null;

	}

}
