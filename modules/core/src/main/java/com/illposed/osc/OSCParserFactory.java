/*
 * Copyright (C) 2015-2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import com.illposed.osc.argument.ArgumentHandler;
import com.illposed.osc.argument.handler.Activator;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory class to create parsers.
 */
public final class OSCParserFactory {

	private final Map<String, Object> properties;
	private final Map<Character, ArgumentHandler> identifierToType;

	private OSCParserFactory() {

		this.properties = new HashMap<>();
		this.identifierToType = new HashMap<>();
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public Map<Character, ArgumentHandler> getIdentifierToTypeMapping() {
		return Collections.unmodifiableMap(identifierToType);
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public static OSCParserFactory createEmptyFactory() {
		return new OSCParserFactory();
	}

	public static OSCParserFactory createDefaultFactory() {

		final OSCParserFactory factory = createEmptyFactory();

		final Map<Character, ArgumentHandler> defaultParserTypes = Activator.createParserTypes();
		for (final Map.Entry<Character, ArgumentHandler> parserType : defaultParserTypes.entrySet()) {
			factory.registerArgumentHandler(parserType.getValue(), parserType.getKey());
		}

		return factory;
	}

	public OSCParser create() {

		// first create a shallow copy
		final Map<Character, ArgumentHandler> identifierToTypeCopy
				= new HashMap<>(identifierToType.size());
		// now make it deep & set the properties
		final Map<String, Object> currentProperties = getProperties();
		for (final Map.Entry<Character, ArgumentHandler> typeEntry : identifierToType.entrySet()) {
			final ArgumentHandler<?> typeClone;
			try {
				typeClone = typeEntry.getValue().clone();
			} catch (final CloneNotSupportedException ex) {
				throw new IllegalStateException("Does not support cloning: "
						+ typeEntry.getValue().getClass(), ex);
			}
			identifierToTypeCopy.put(typeEntry.getKey(), typeClone);

			typeClone.setProperties(currentProperties);
		}

		return new OSCParser(identifierToTypeCopy, currentProperties);
	}

	/**
	 * Returns the current set of properties.
	 * These will be propagated to created parsers and to the argument-handlers.
	 * @return the set of properties to adhere to
	 * @see ArgumentHandler#setProperties(java.util.Map)
	 */
	public Map<String, Object> getProperties() {
		return Collections.unmodifiableMap(properties);
	}

	/**
	 * Sets a new set of properties after clearing the current ones.
	 * Properties will be propagated to created parsers
	 * and to the argument-handlers.
	 * This will only have an effect for parsers and argument-handlers
	 * being created in the future.
	 * @param properties the new set of properties to adhere to
	 */
	public void setProperties(final Map<String, Object> properties) {

		clearProperties();
		addProperties(properties);
	}

	// Public API
	/**
	 * Adds a new set of properties, extending,
	 * possibly overriding the current ones.
	 * Properties will be propagated to created parsers
	 * and to the argument-handlers.
	 * This will only have an effect for parsers and argument-handlers
	 * being created in the future.
	 * @param additionalProperties the new set of properties to adhere to
	 */
	@SuppressWarnings("WeakerAccess")
	public void addProperties(final Map<String, Object> additionalProperties) {
		properties.putAll(additionalProperties);
	}

	// Public API
	/**
	 * Clears all currently stored properties.
	 * Properties will be propagated to created parsers
	 * and to the argument-handlers.
	 * This will only have an effect for parsers and argument-handlers
	 * being created in the future.
	 */
	@SuppressWarnings("WeakerAccess")
	public void clearProperties() {
		properties.clear();
	}

	// Public API
	@SuppressWarnings("unused")
	public void registerArgumentHandler(final ArgumentHandler argumentHandler) {
		registerArgumentHandler(argumentHandler, argumentHandler.getDefaultIdentifier());
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public void registerArgumentHandler(
			final ArgumentHandler argumentHandler,
			final char typeIdentifier)
	{
		final ArgumentHandler previousArgumentHandler = identifierToType.get(typeIdentifier);
		if (previousArgumentHandler != null) {
			throw new IllegalStateException("Type identifier '" + typeIdentifier
					+ "' is already used for type "
					+ previousArgumentHandler.getClass().getCanonicalName());
		}
		identifierToType.put(typeIdentifier, argumentHandler);
	}

	// Public API
	@SuppressWarnings("unused")
	public void unregisterArgumentHandler(final ArgumentHandler argumentHandler) {
		unregisterArgumentHandler(argumentHandler.getDefaultIdentifier());
	}

	// Public API
	@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
	public ArgumentHandler unregisterArgumentHandler(final char typeIdentifier) {
		return identifierToType.remove(typeIdentifier);
	}
}
