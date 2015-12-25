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

		this.properties = new HashMap<String, Object>();
		this.argumentHandlers = new LinkedList<ArgumentHandler>();
	}

	public List<ArgumentHandler> getArgumentHandlers() {
		return Collections.unmodifiableList(argumentHandlers);
	}

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
				= new ArrayList<ArgumentHandler>(argumentHandlers.size());
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
	 */
	public Map<String, Object> getProperties() {
		return Collections.unmodifiableMap(properties);
	}

	/**
	 * Sets a new set of properties, possibly overriding, but not clearing the old ones.
	 * These will be propagated to created serializers and to the argument-handlers.
	 * @param properties the new set of properties to adhere to
	 */
	public void setProperties(final Map<String, Object> properties) {
		this.properties.putAll(properties);
	}

	public void clearProperties() {
		this.properties.clear();
	}

	public void registerArgumentHandler(final ArgumentHandler argumentHandler) {
		argumentHandlers.add(argumentHandler);
	}

	public void unregisterArgumentHandler(final ArgumentHandler argumentHandler) {
		argumentHandlers.remove(argumentHandler);
	}
}
