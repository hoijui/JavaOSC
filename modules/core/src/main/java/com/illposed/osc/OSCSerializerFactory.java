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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Factory class to create serializers.
 */
public class OSCSerializerFactory {

	private final Map<String, Object> properties;
	private final List<ArgumentHandler> argumentHandlers;

	protected OSCSerializerFactory() {

		this.properties = new HashMap<>();
		this.argumentHandlers = new LinkedList<>();
	}

	@SuppressWarnings("WeakerAccess") // Public API
	public List<ArgumentHandler> getArgumentHandlers() {
		return Collections.unmodifiableList(argumentHandlers);
	}

	@SuppressWarnings("WeakerAccess") // Public API
	public static OSCSerializerFactory createEmptyFactory() {
		return new OSCSerializerFactory();
	}

	public static OSCSerializerFactory createDefaultFactory() {

		final OSCSerializerFactory factory = createEmptyFactory();

		final List<ArgumentHandler> defaultSerializerTypes = Activator.createSerializerTypes();
		for (final ArgumentHandler serializerType : defaultSerializerTypes) {
			factory.registerArgumentHandler(serializerType);
		}

		return factory;
	}

	public OSCSerializer create(final ByteBuffer output) {

		final Map<String, Object> currentProperties = getProperties();
		final List<ArgumentHandler> typeCopies
				= new ArrayList<>(argumentHandlers.size());
		for (final ArgumentHandler<?> type : argumentHandlers) {
			final ArgumentHandler<?> typeClone;
			try {
				typeClone = type.clone();
			} catch (final CloneNotSupportedException ex) {
				throw new IllegalStateException("Does not support cloning: " + type.getClass(), ex);
			}
			typeClone.setProperties(currentProperties);

			typeCopies.add(typeClone);
		}

		return new OSCSerializer(typeCopies, currentProperties, output);
	}

	/**
	 * Returns the current set of properties.
	 * These will be propagated to created serializers and to the argument-handlers.
	 * @return the set of properties to adhere to
	 * @see ArgumentHandler#setProperties(java.util.Map)
	 */
	public Map<String, Object> getProperties() {
		return Collections.unmodifiableMap(properties);
	}
	/**
	 * Sets a new set of properties after clearing the current ones.
	 * Properties will be propagated to created serializers
	 * and to the argument-handlers.
	 * This will only have an effect for serializers and argument-handlers
	 * being created in the future.
	 * @param properties the new set of properties to adhere to
	 */
	public void setProperties(final Map<String, Object> properties) {

		clearProperties();
		addProperties(properties);
	}

	/**
	 * Adds a new set of properties, extending,
	 * possibly overriding the current ones.
	 * Properties will be propagated to created serializers
	 * and to the argument-handlers.
	 * This will only have an effect for serializers and argument-handlers
	 * being created in the future.
	 * @param additionalProperties the new set of properties to adhere to
	 */
	@SuppressWarnings("WeakerAccess") // Public API
	public void addProperties(final Map<String, Object> additionalProperties) {
		properties.putAll(additionalProperties);
	}

	/**
	 * Clears all currently stored properties.
	 * Properties will be propagated to created serializers
	 * and to the argument-handlers.
	 * This will only have an effect for serializers and argument-handlers
	 * being created in the future.
	 */
	@SuppressWarnings("WeakerAccess") // Public API
	public void clearProperties() {
		properties.clear();
	}

	@SuppressWarnings("WeakerAccess") // Public API
	public void registerArgumentHandler(final ArgumentHandler argumentHandler) {
		argumentHandlers.add(argumentHandler);
	}

	@SuppressWarnings({"WeakerAccess", "unused"}) // Public API
	public void unregisterArgumentHandler(final ArgumentHandler argumentHandler) {
		argumentHandlers.remove(argumentHandler);
	}
}
