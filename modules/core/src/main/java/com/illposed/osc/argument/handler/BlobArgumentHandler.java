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
 * Parses and serializes an OSC binary-blob type.
 */
public class BlobArgumentHandler implements ArgumentHandler<byte[]>, Cloneable {

	public static final ArgumentHandler<byte[]> INSTANCE = new BlobArgumentHandler();

	/** Allow overriding, but somewhat enforce the ugly singleton. */
	protected BlobArgumentHandler() {
		// ctor declared only for setting the access level
	}

	@Override
	public char getDefaultIdentifier() {
		return 'b';
	}

	@Override
	public Class<byte[]> getJavaClass() {
		return byte[].class;
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

	/**
	 * If not yet aligned, move to the next byte with an index in the byte array
	 * which is dividable by four.
	 * @param rawInput to be aligned
	 */
	public static void moveToFourByteBoundry(final ByteBuffer rawInput) {
		final int mod = rawInput.position() % 4;
		final int padding = (4 - mod) % 4;
		rawInput.position(rawInput.position() + padding);
	}

	/**
	 * Align a stream by padding it with '0's so it has a size divisible by 4.
	 * @param stream to be aligned
	 * @throws IOException if there is a problem writing the padding zeros
	 */
	public static void align(final SizeTrackingOutputStream stream) throws IOException {
		final int alignmentOverlap = stream.size() % 4;
		final int padLen = (4 - alignmentOverlap) % 4;
		for (int pci = 0; pci < padLen; pci++) {
			stream.write(0);
		}
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
	public byte[] parse(final ByteBuffer input) throws OSCParseException {
		final int blobLen = IntegerArgumentHandler.INSTANCE.parse(input);
		final byte[] res = readByteArray(input, blobLen);
		moveToFourByteBoundry(input);
		return res;
	}

	@Override
	public void serialize(final SizeTrackingOutputStream stream, final byte[] value)
			throws OSCSerializeException
	{
		IntegerArgumentHandler.INSTANCE.serialize(stream, value.length);
		try {
			stream.write(value);
			align(stream);
		} catch (final IOException ex) {
			throw new OSCSerializeException(ex);
		}
	}
}
