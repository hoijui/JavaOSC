/*
 * Copyright (C) 2015, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.argument.handler;

import com.illposed.osc.argument.OSCTimeStamp;
import com.illposed.osc.utility.OSCParseException;
import com.illposed.osc.utility.OSCSerializeException;
import com.illposed.osc.argument.ArgumentHandler;
import com.illposed.osc.utility.SizeTrackingOutputStream;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Map;

/**
 * Parses and serializes an OSC compliant time-stamp, using <code>Date</code> as a wrapper,
 * and thus loosing out on the resolution.
 */
public class DateTimeStampArgumentHandler implements ArgumentHandler<Date>, Cloneable {

	public static final ArgumentHandler<Date> INSTANCE = new DateTimeStampArgumentHandler();

	/** Allow overriding, but somewhat enforce the ugly singleton. */
	protected DateTimeStampArgumentHandler() {
		// ctor declared only for setting the access level
	}

	@Override
	public char getDefaultIdentifier() {
		return 't';
	}

	@Override
	public Class<Date> getJavaClass() {
		return Date.class;
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
	public DateTimeStampArgumentHandler clone() throws CloneNotSupportedException {
		return (DateTimeStampArgumentHandler) super.clone();
	}

	@Override
	public Date parse(final ByteBuffer input) throws OSCParseException {
		return TimeStampArgumentHandler.INSTANCE.parse(input).toDate();
	}

	@Override
	public void serialize(final SizeTrackingOutputStream stream, final Date value)
			throws OSCSerializeException
	{
		TimeStampArgumentHandler.INSTANCE.serialize(stream, OSCTimeStamp.valueOf(value));
	}
}
