/*
 * Copyright (C) 2015, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.utility;

import com.illposed.osc.argument.ArgumentHandler;
import com.illposed.osc.argument.handler.Activator;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Factory class to create serializers.
 */
public final class OSCSerializerFactory {

	private final Map<String, Object> properties;
	private final List<ArgumentHandler> argumentHandlers;

	private OSCSerializerFactory() {

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

	public OSCSerializer create(final OutputStream wrappedStream) {

		final Map<String, Object> currentProperties = getProperties();
		final List<ArgumentHandler> typeClopies
				= new ArrayList<ArgumentHandler>(argumentHandlers.size());
		for (final ArgumentHandler<?> type : argumentHandlers) {
			final ArgumentHandler<?> typeClone;
			try {
				typeClone = type.clone();
			} catch (final CloneNotSupportedException ex) {
				throw new IllegalStateException("Does not support cloning: " + type.getClass(), ex);
			}
			typeClone.setProperties(currentProperties);

			typeClopies.add(typeClone);
		}

		return new OSCSerializer(typeClopies, wrappedStream);
	}

	/**
	 * Returns the character set used to decode message addresses
	 * and string parameters.
	 * @return the character-encoding-set used by this converter
	 */
	public Map<String, Object> getProperties() {
		return Collections.unmodifiableMap(properties);
	}

	/**
	 * Sets a new set of properties, possibly overriding, but not clearing the old ones.
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
