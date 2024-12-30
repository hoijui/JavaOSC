// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc.argument.handler.javase;

import com.illposed.osc.argument.ArgumentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Allows to easily register and unregister all types in this package.
 */
public final class Activator {

	private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

	private static final List<ArgumentHandler> TYPES_STATIC_COMMON;
	static {
		final ArrayList<ArgumentHandler> types = new ArrayList<>();
		types.add(AwtColorArgumentHandler.INSTANCE);
		types.trimToSize();
		TYPES_STATIC_COMMON = Collections.unmodifiableList(types);
	}

	private Activator() {}

	public static Map<Character, ArgumentHandler> createParserTypes() {

		final Map<Character, ArgumentHandler> parserTypes
				= new HashMap<>(TYPES_STATIC_COMMON.size() + 1);
		for (final ArgumentHandler type : TYPES_STATIC_COMMON) {
			parserTypes.put(type.getDefaultIdentifier(), type);
		}

		return parserTypes;
	}

	public static List<ArgumentHandler> createSerializerTypes() {

		final List<ArgumentHandler> serializerTypes
				= new ArrayList<>(TYPES_STATIC_COMMON.size() + 2);
		serializerTypes.addAll(TYPES_STATIC_COMMON);

		return serializerTypes;
	}
}
