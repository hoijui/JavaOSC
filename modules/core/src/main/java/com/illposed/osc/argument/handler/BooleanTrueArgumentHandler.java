/*
 * Copyright (C) 2015, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.argument.handler;

import com.illposed.osc.OSCParseException;
import com.illposed.osc.argument.ArgumentHandler;
import com.illposed.osc.SizeTrackingOutputStream;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Parses and serializes an OSC boolean TRUE type.
 */
public class BooleanTrueArgumentHandler implements ArgumentHandler<Boolean>, Cloneable {

	public static final ArgumentHandler<Boolean> INSTANCE = new BooleanTrueArgumentHandler();

	/** Allow overriding, but somewhat enforce the ugly singleton. */
	protected BooleanTrueArgumentHandler() {
		// ctor declared only for setting the access level
	}

	@Override
	public char getDefaultIdentifier() {
		return 'T';
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
	public BooleanTrueArgumentHandler clone() throws CloneNotSupportedException {
		return (BooleanTrueArgumentHandler) super.clone();
	}

	@Override
	public Boolean parse(final ByteBuffer input) throws OSCParseException {
		return Boolean.TRUE;
	}

	@Override
	public void serialize(final SizeTrackingOutputStream stream, final Boolean value) {

//		if (!value.equals(Boolean.TRUE)) {
//			throw new OSCSerializeException();
//		}
	}
}
