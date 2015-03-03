/*
 * Copyright (C) 2015, C. Ramakrishnan / Illposed Software.
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

		this.properties = new HashMap<String, Object>();
		this.identifierToType = new HashMap<Character, ArgumentHandler>();
	}

	public Map<Character, ArgumentHandler> getIdentifierToTypeMapping() {
		return Collections.unmodifiableMap(identifierToType);
	}

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
				= new HashMap<Character, ArgumentHandler>(identifierToType.size());
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

		return new OSCParser(identifierToTypeCopy);
	}

	/**
	 * Returns the current set of properties.
	 * These will be propagated to created parsers and to the argument-handlers.
	 * @return the set of properties to adhere to
	 */
	public Map<String, Object> getProperties() {
		return Collections.unmodifiableMap(properties);
	}

	/**
	 * Sets a new set of properties, possibly overriding, but not clearing the old ones.
	 * These will be propagated to created parsers and to the argument-handlers.
	 * @param properties the new set of properties to adhere to
	 */
	public void setProperties(final Map<String, Object> properties) {
		this.properties.putAll(properties);
	}

	public void clearProperties() {
		this.properties.clear();
	}

	public void registerArgumentHandler(final ArgumentHandler argumentHandler) {
		registerArgumentHandler(argumentHandler, argumentHandler.getDefaultIdentifier());
	}

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

	public void unregisterArgumentHandler(final ArgumentHandler argumentHandler) {
		unregisterArgumentHandler(argumentHandler.getDefaultIdentifier());
	}

	public ArgumentHandler unregisterArgumentHandler(final char typeIdentifier) {
		return identifierToType.remove(typeIdentifier);
	}
}
