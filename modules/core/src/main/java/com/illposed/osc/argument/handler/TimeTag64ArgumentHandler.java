// SPDX-FileCopyrightText: 2015-2017 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc.argument.handler;

import com.illposed.osc.BytesReceiver;
import com.illposed.osc.argument.OSCTimeTag64;
import com.illposed.osc.OSCParseException;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.argument.ArgumentHandler;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Parses and serializes an OSC <i>Time-tag</i>.
 */
public class TimeTag64ArgumentHandler implements ArgumentHandler<OSCTimeTag64>, Cloneable {

	public static final ArgumentHandler<OSCTimeTag64> INSTANCE = new TimeTag64ArgumentHandler();

	// Public API
	/** Allow overriding, but somewhat enforce the ugly singleton. */
	@SuppressWarnings("WeakerAccess")
	protected TimeTag64ArgumentHandler() {
		// declared only for setting the access level
	}

	@Override
	public char getDefaultIdentifier() {
		return 't';
	}

	@Override
	public Class<OSCTimeTag64> getJavaClass() {
		return OSCTimeTag64.class;
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
	public TimeTag64ArgumentHandler clone() throws CloneNotSupportedException {
		return (TimeTag64ArgumentHandler) super.clone();
	}

	@Override
	public OSCTimeTag64 parse(final ByteBuffer input) throws OSCParseException {

		final long ntpTime = LongArgumentHandler.INSTANCE.parse(input);
		return OSCTimeTag64.valueOf(ntpTime);
	}

	@Override
	public void serialize(final BytesReceiver output, final OSCTimeTag64 value)
			throws OSCSerializeException
	{
		LongArgumentHandler.INSTANCE.serialize(output, value.getNtpTime());
	}
}
