/*
 * Copyright (C) 2015-2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.argument.handler;

import com.illposed.osc.OSCParseException;
import com.illposed.osc.argument.ArgumentHandler;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Parses and serializes an OSC null type.
 */
public class NullArgumentHandler implements ArgumentHandler<Object>, Cloneable {

	public static final ArgumentHandler<Object> INSTANCE = new NullArgumentHandler();

	/** Allow overriding, but somewhat enforce the ugly singleton. */
	protected NullArgumentHandler() {
		// ctor declared only for setting the access level
	}

	@Override
	public char getDefaultIdentifier() {
		return 'N';
	}

	@Override
	public Class<Object> getJavaClass() {
		return Object.class;
	}

	@Override
	public void setProperties(final Map<String, Object> properties) {
		// we make no use of any properties
	}

	@Override
	public boolean isMarkerOnly() {
		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	public NullArgumentHandler clone() throws CloneNotSupportedException {
		return (NullArgumentHandler) super.clone();
	}

	@Override
	public Object parse(final ByteBuffer input) throws OSCParseException {
		return null;
	}

	@Override
	public void serialize(final ByteBuffer output, final Object value) {

//		if (value != null) {
//			throw new OSCSerializeException();
//		}
	}
}
