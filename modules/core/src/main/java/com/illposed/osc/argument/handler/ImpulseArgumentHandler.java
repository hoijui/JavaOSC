// SPDX-FileCopyrightText: 2015-2017 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc.argument.handler;

import com.illposed.osc.BytesReceiver;
import com.illposed.osc.argument.OSCImpulse;
import com.illposed.osc.argument.ArgumentHandler;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Parses and serializes an OSC <i>Impulse</i> type.
 */
public class ImpulseArgumentHandler implements ArgumentHandler<OSCImpulse>, Cloneable {

	public static final ArgumentHandler<OSCImpulse> INSTANCE = new ImpulseArgumentHandler();

	// Public API
	/** Allow overriding, but somewhat enforce the ugly singleton. */
	@SuppressWarnings("WeakerAccess")
	protected ImpulseArgumentHandler() {
		// declared only for setting the access level
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
	public OSCImpulse parse(final ByteBuffer input) {
		return OSCImpulse.INSTANCE;
	}

	@Override
	public void serialize(final BytesReceiver output, final OSCImpulse value) {

//		if (value != OSCImpulse.INSTANCE) {
//			throw new OSCSerializeException();
//		}
	}
}
