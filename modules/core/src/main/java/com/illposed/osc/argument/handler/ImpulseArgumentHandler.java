/*
 * Copyright (C) 2015, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.argument.handler;

import com.illposed.osc.argument.OSCImpulse;
import com.illposed.osc.OSCParseException;
import com.illposed.osc.argument.ArgumentHandler;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Parses and serializes an OSC impulse type.
 */
public class ImpulseArgumentHandler implements ArgumentHandler<OSCImpulse>, Cloneable {

	public static final ArgumentHandler<OSCImpulse> INSTANCE = new ImpulseArgumentHandler();

	/** Allow overriding, but somewhat enforce the ugly singleton. */
	protected ImpulseArgumentHandler() {
		// ctor declared only for setting the access level
	}

	@Override
	public char getDefaultIdentifier() {
		return 'I';
	}

	@Override
	public Class<OSCImpulse> getJavaClass() {
		return OSCImpulse.class;
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
	public ImpulseArgumentHandler clone() throws CloneNotSupportedException {
		return (ImpulseArgumentHandler) super.clone();
	}

	@Override
	public OSCImpulse parse(final ByteBuffer input) throws OSCParseException {
		return OSCImpulse.INSTANCE;
	}

	@Override
	public void serialize(final ByteBuffer output, final OSCImpulse value) {

//		if (value != OSCImpulse.INSTANCE) {
//			throw new OSCSerializeException();
//		}
	}
}
