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
 * Parses and serializes an OSC null type.
 */
public class NullArgumentHandler implements ArgumentHandler<Object>, Cloneable {

	public static final ArgumentHandler<Object> INSTANCE = new NullArgumentHandler();

	// Public API
	/** Allow overriding, but somewhat enforce the ugly singleton. */
	@SuppressWarnings("WeakerAccess")
	protected NullArgumentHandler() {
		// declared only for setting the access level
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
	public Object parse(final ByteBuffer input) {
		return null;
	}

	@Override
	public void serialize(final BytesReceiver output, final Object value) {

//		if (value != null) {
//			throw new OSCSerializeException();
//		}
	}
}
