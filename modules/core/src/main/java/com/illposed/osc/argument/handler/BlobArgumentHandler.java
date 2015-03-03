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
import com.illposed.osc.OSCSerializer;
import com.illposed.osc.argument.ArgumentHandler;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Parses and serializes an OSC binary-blob type.
 */
public class BlobArgumentHandler implements ArgumentHandler<ByteBuffer>, Cloneable {

	public static final ArgumentHandler<ByteBuffer> INSTANCE = new BlobArgumentHandler();

	/** Allow overriding, but somewhat enforce the ugly singleton. */
	protected BlobArgumentHandler() {
		// ctor declared only for setting the access level
	}

	@Override
	public char getDefaultIdentifier() {
		return 'b';
	}

	@Override
	public Class<ByteBuffer> getJavaClass() {
		return ByteBuffer.class;
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
	public BlobArgumentHandler clone() throws CloneNotSupportedException {
		return (BlobArgumentHandler) super.clone();
	}

	/** NOTE Might be used in other places too! */
	private static byte[] readByteArray(final ByteBuffer rawInput, final int numBytes) {
		final byte[] res = new byte[numBytes];
		// XXX Crude copying from the buffer to the array. This can only be avoided if we change the return type to ByteBuffer.
		rawInput.get(res);
		return res;
	}

	public static BigInteger readBigInteger(final ByteBuffer rawInput, final int numBytes) {
		final byte[] myBytes = readByteArray(rawInput, numBytes);
		return new BigInteger(myBytes);
	}

	@Override
	public ByteBuffer parse(final ByteBuffer input) throws OSCParseException {
		final int blobLen = IntegerArgumentHandler.INSTANCE.parse(input);
		final int previousLimit = input.limit();
		input.limit(input.position() + blobLen);
		final ByteBuffer value = input.slice();
		input.limit(previousLimit);
		return value;
	}

	@Override
	public void serialize(final ByteBuffer output, final ByteBuffer value)
			throws OSCSerializeException
	{
		final int numBytes = value.remaining();
		IntegerArgumentHandler.INSTANCE.serialize(output, numBytes);
		output.put(value);
		OSCSerializer.align(output);
	}
}
