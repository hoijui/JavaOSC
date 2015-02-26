/*
 * Copyright (C) 2015, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.argument.handler;

import com.illposed.osc.OSCParseException;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.argument.ArgumentHandler;
import com.illposed.osc.SizeTrackingOutputStream;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Parses and serializes an OSC boolean FALSE type.
 */
public class BooleanFalseArgumentHandler implements ArgumentHandler<Boolean>, Cloneable {

	public static final ArgumentHandler<Boolean> INSTANCE = new BooleanFalseArgumentHandler();

	/** Allow overriding, but somewhat enforce the ugly singleton. */
	protected BooleanFalseArgumentHandler() {
		// ctor declared only for setting the access level
	}

	@Override
	public char getDefaultIdentifier() {
		return 'F';
	}

	@Override
	public Class<Boolean> getJavaClass() {
		return Boolean.class;
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
	public BooleanFalseArgumentHandler clone() throws CloneNotSupportedException {
		return (BooleanFalseArgumentHandler) super.clone();
	}

	@Override
	public Boolean parse(final ByteBuffer input) throws OSCParseException {
		return Boolean.FALSE;
	}

	@Override
	public void serialize(final SizeTrackingOutputStream stream, final Boolean value)
			throws OSCSerializeException
	{
//		if (!value.equals(Boolean.FALSE)) {
//			throw new OSCSerializeException();
//		}
	}
}
