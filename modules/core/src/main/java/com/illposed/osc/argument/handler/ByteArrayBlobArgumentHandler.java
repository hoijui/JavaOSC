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
import com.illposed.osc.SizeTrackingOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Parses and serializes an OSC binary-blob type.
 */
public class ByteArrayBlobArgumentHandler implements ArgumentHandler<byte[]>, Cloneable {

	public static final ArgumentHandler<byte[]> INSTANCE = new ByteArrayBlobArgumentHandler();

	/** Allow overriding, but somewhat enforce the ugly singleton. */
	protected ByteArrayBlobArgumentHandler() {
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
	public ByteArrayBlobArgumentHandler clone() throws CloneNotSupportedException {
		return (ByteArrayBlobArgumentHandler) super.clone();
	}

	@Override
	public byte[] parse(final ByteBuffer input) throws OSCParseException {

		final ByteBuffer bufferValue = BlobArgumentHandler.INSTANCE.parse(input);
		final byte[] value = new byte[bufferValue.remaining()];
		bufferValue.get(value);
		return value;
	}

	@Override
	public void serialize(final SizeTrackingOutputStream stream, final byte[] value)
			throws IOException, OSCSerializeException
	{
		// NOTE This would be cleaner, code wise, but it (currently) creates performance overhead:
		//   byte[] -> ByteBuffer -> byte[] -> OutputStream
//		final ByteBuffer bufferValue = ByteBuffer.wrap(value);
//		BlobArgumentHandler.INSTANCE.serialize(stream, bufferValue);

		IntegerArgumentHandler.INSTANCE.serialize(stream, value.length);
		stream.write(value);
		OSCSerializer.align(stream);
	}
}
