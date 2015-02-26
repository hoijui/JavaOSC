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
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Parses and serializes an OSC long type (64bit signed integer).
 */
public class LongArgumentHandler implements ArgumentHandler<Long>, Cloneable {

	public static final ArgumentHandler<Long> INSTANCE = new LongArgumentHandler();

	/** Allow overriding, but somewhat enforce the ugly singleton. */
	protected LongArgumentHandler() {
		// ctor declared only for setting the access level
	}

	@Override
	public char getDefaultIdentifier() {
		return 'h';
	}

	@Override
	public Class<Long> getJavaClass() {
		return Long.class;
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
	public LongArgumentHandler clone() throws CloneNotSupportedException {
		return (LongArgumentHandler) super.clone();
	}

	public static BigInteger readBigInteger(final ByteBuffer rawInput, final int numBytes) {
//		final byte[] myBytes = new byte[numBytes];
//		System.arraycopy(rawInput.array(), rawInput.position(), myBytes, 0, numBytes);
//		rawInput.position(rawInput.position() + numBytes);
//		return  new BigInteger(myBytes);
		return BlobArgumentHandler.readBigInteger(rawInput, numBytes);
	}

	@Override
	public Long parse(final ByteBuffer input) throws OSCParseException {
		final BigInteger longIntBytes = readBigInteger(input, 8);
		return longIntBytes.longValue();
	}

	@Override
	public void serialize(final SizeTrackingOutputStream stream, final Long value) throws OSCSerializeException {

		long curValue = value;
		final byte[] longintBytes = new byte[8];
		longintBytes[7] = (byte)curValue; curValue >>>= 8;
		longintBytes[6] = (byte)curValue; curValue >>>= 8;
		longintBytes[5] = (byte)curValue; curValue >>>= 8;
		longintBytes[4] = (byte)curValue; curValue >>>= 8;
		longintBytes[3] = (byte)curValue; curValue >>>= 8;
		longintBytes[2] = (byte)curValue; curValue >>>= 8;
		longintBytes[1] = (byte)curValue; curValue >>>= 8;
		longintBytes[0] = (byte)curValue;

		try {
			stream.write(longintBytes);
		} catch (final IOException ex) {
			throw new OSCSerializeException(ex);
		}
	}
}
