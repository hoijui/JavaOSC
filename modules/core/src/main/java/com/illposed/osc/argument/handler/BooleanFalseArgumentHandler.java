// SPDX-FileCopyrightText: 2015-2017 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc.argument.handler;

import com.illposed.osc.BytesReceiver;
import com.illposed.osc.argument.ArgumentHandler;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Parses and serializes an OSC boolean FALSE type.
 */
public class BooleanFalseArgumentHandler implements ArgumentHandler<Boolean>, Cloneable {

	public static final ArgumentHandler<Boolean> INSTANCE = new BooleanFalseArgumentHandler();

	// Public API
	/** Allow overriding, but somewhat enforce the ugly singleton. */
	@SuppressWarnings("WeakerAccess")
	protected BooleanFalseArgumentHandler() {
		// declared only for setting the access level
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
	public Boolean parse(final ByteBuffer input) {
		return Boolean.FALSE;
	}

	@Override
	public void serialize(final BytesReceiver output, final Boolean value) {

//		if (!value.equals(Boolean.FALSE)) {
//			throw new OSCSerializeException();
//		}
	}
}
