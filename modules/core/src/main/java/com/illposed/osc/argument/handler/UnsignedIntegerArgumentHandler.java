/*
 * Copyright (C) 2015, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.argument.handler;

import com.illposed.osc.argument.OSCUnsigned;
import com.illposed.osc.OSCParseException;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.argument.ArgumentHandler;
import com.illposed.osc.SizeTrackingOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Parses and serializes an OSC unsigned type (32bit unsigned integer).
 */
public class UnsignedIntegerArgumentHandler implements ArgumentHandler<OSCUnsigned>, Cloneable {

	public static final ArgumentHandler<OSCUnsigned> INSTANCE = new UnsignedIntegerArgumentHandler();

	/** Allow overriding, but somewhat enforce the ugly singleton. */
	protected UnsignedIntegerArgumentHandler() {
		// ctor declared only for setting the access level
	}

	@Override
	public char getDefaultIdentifier() {
		return 'u';
	}

	@Override
	public Class<OSCUnsigned> getJavaClass() {
		return OSCUnsigned.class;
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
	public UnsignedIntegerArgumentHandler clone() throws CloneNotSupportedException {
		return (UnsignedIntegerArgumentHandler) super.clone();
	}

	/**
	 * Reads an unsigned integer (32 bit) from the byte stream.
	 * This code is copied from <a href="http://darksleep.com/player/JavaAndUnsignedTypes.html">
	 * here</a>, and it is licensed under the Public Domain.
	 * @return single precision, unsigned integer (32 bit) wrapped in a 64 bit integer (long)
	 */
	@Override
	public OSCUnsigned parse(final ByteBuffer input) throws OSCParseException {
		return OSCUnsigned.valueOf(new byte[] {
				input.get(),
				input.get(),
				input.get(),
				input.get()});
	}

	@Override
	public void serialize(final SizeTrackingOutputStream stream, final OSCUnsigned value)
			throws OSCSerializeException
	{
		final long asLong = value.toLong();
		try {
			stream.write((byte) (asLong >> 24 & 0xFFL));
			stream.write((byte) (asLong >> 16 & 0xFFL));
			stream.write((byte) (asLong >>  8 & 0xFFL));
			stream.write((byte) (asLong       & 0xFFL));
		} catch (final IOException ex) {
			throw new OSCSerializeException(ex);
		}
	}
}
