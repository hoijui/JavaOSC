// SPDX-FileCopyrightText: 2015-2017 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc.argument.handler;

import com.illposed.osc.BytesReceiver;
import com.illposed.osc.OSCParseException;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.argument.ArgumentHandler;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Parses and serializes an OSC double type (64bit floating point number).
 */
public class DoubleArgumentHandler implements ArgumentHandler<Double>, Cloneable {

	public static final ArgumentHandler<Double> INSTANCE = new DoubleArgumentHandler();

	// Public API
	/** Allow overriding, but somewhat enforce the ugly singleton. */
	@SuppressWarnings("WeakerAccess")
	protected DoubleArgumentHandler() {
		// declared only for setting the access level
	}

	@Override
	public char getDefaultIdentifier() {
		return 'd';
	}

	@Override
	public Class<Double> getJavaClass() {
		return Double.class;
	}

	@Override
	public void setProperties(final Map<String, Object> properties) {
		// we make no use of any properties
	}

	@Override
	public boolean isMarkerOnly() {
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public DoubleArgumentHandler clone() throws CloneNotSupportedException {
		return (DoubleArgumentHandler) super.clone();
	}

	@Override
	public Double parse(final ByteBuffer input) throws OSCParseException {
		return Double.longBitsToDouble(LongArgumentHandler.INSTANCE.parse(input));
	}

	@Override
	public void serialize(final BytesReceiver output, final Double value) throws OSCSerializeException {
		LongArgumentHandler.INSTANCE.serialize(output, Double.doubleToRawLongBits(value));
	}
}
