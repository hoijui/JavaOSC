/*
 * Copyright (C) 2015, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.argument.handler;

import com.illposed.osc.argument.OSCTimeStamp;
import com.illposed.osc.OSCParseException;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.argument.ArgumentHandler;
import com.illposed.osc.SizeTrackingOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Parses and serializes an OSC compliant time-stamp.
 */
public class TimeStampArgumentHandler implements ArgumentHandler<OSCTimeStamp>, Cloneable {

	public static final ArgumentHandler<OSCTimeStamp> INSTANCE = new TimeStampArgumentHandler();

	/** Allow overriding, but somewhat enforce the ugly singleton. */
	protected TimeStampArgumentHandler() {
		// ctor declared only for setting the access level
	}

	@Override
	public char getDefaultIdentifier() {
		return 't';
	}

	@Override
	public Class<OSCTimeStamp> getJavaClass() {
		return OSCTimeStamp.class;
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
	public TimeStampArgumentHandler clone() throws CloneNotSupportedException {
		return (TimeStampArgumentHandler) super.clone();
	}

	@Override
	public OSCTimeStamp parse(final ByteBuffer input) throws OSCParseException {
		final long ntpTime = LongArgumentHandler.INSTANCE.parse(input);
		return new OSCTimeStamp(ntpTime);
	}

	@Override
	public void serialize(final SizeTrackingOutputStream stream, final OSCTimeStamp value)
			throws IOException, OSCSerializeException
	{
		LongArgumentHandler.INSTANCE.serialize(stream, value.getNtpTime());
	}
}
