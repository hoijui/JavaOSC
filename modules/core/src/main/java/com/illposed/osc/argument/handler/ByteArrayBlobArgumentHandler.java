// SPDX-FileCopyrightText: 2015-2017 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc.argument.handler;

import com.illposed.osc.BytesReceiver;
import com.illposed.osc.OSCParseException;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.argument.ArgumentHandler;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Parses and serializes an OSC binary-blob type.
 */
public class ByteArrayBlobArgumentHandler implements ArgumentHandler<byte[]>, Cloneable {

	public static final ArgumentHandler<byte[]> INSTANCE = new ByteArrayBlobArgumentHandler();

	// Public API
	/** Allow overriding, but somewhat enforce the ugly singleton. */
	@SuppressWarnings("WeakerAccess")
	protected ByteArrayBlobArgumentHandler() {
		// declared only for setting the access level
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
	public void serialize(final BytesReceiver output, final byte[] value) throws OSCSerializeException {

		final ByteBuffer bufferValue = ByteBuffer.wrap(value).asReadOnlyBuffer();
		BlobArgumentHandler.INSTANCE.serialize(output, bufferValue);
	}
}
