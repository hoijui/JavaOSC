/*
 * Copyright (C) 2014, C. Ramakrishnan / Auracle.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.utility;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Wraps an other stream, while keeping track of the number of bytes already written to the stream.
 */
public class SizeTrackingOutputStream extends OutputStream {

	private final OutputStream wrappedStream;
	private int writtenBytes;

	public SizeTrackingOutputStream(final OutputStream wrappedStream) {

		this.wrappedStream = wrappedStream;
		this.writtenBytes = 0;
	}

	public int size() {
		return writtenBytes;
	}

	public void reset() {
		writtenBytes = 0;
	}

	@Override
	public void write(final int bty) throws IOException {
		wrappedStream.write(bty);
		writtenBytes++;
	}

	@Override
	public void write(final byte[] bytes) throws IOException {
		wrappedStream.write(bytes);
		writtenBytes += bytes.length;
	}

	@Override
	public void write(final byte[] bytes, final int off, final int len) throws IOException {
		wrappedStream.write(bytes, off, len);
		writtenBytes += len - off;
	}
}
