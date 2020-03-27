/*
 * Copyright (C) 2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of a {@link BytesReceiver} using a {@code List<byte[]>}
 * as the internal buffer.
 * This is useful if we do not now initially know the size
 * of the expected data in total, and at the same time want to prevent
 * re-allocating {@code ByteBuffer} or {@code byte[]}
 * whenever the buffer gets too small.
 * It is thus an ideal candidate for asynchronous data receiving,
 * as is the case in UDP.
 */
public class ByteArrayListBytesReceiver implements BytesReceiver {

	private final List<byte[]> buffer;
	private int position;

	public ByteArrayListBytesReceiver() {

		this.buffer = new LinkedList<>();
		this.position = 0;
	}

	@Override
	public BytesReceiver put(final byte b) {

		buffer.add(new byte[] {b});
		position += 1;
		return this;
	}

	@Override
	public BytesReceiver put(final byte[] src) {

		buffer.add(src);
		position += src.length;
		return this;
	}

	@Override
	public BytesReceiver put(final ByteBuffer src) {
		return put(src.array()); // HACK better get rid of this method altogether!
	}

	@Override
	public BytesReceiver clear() {

		buffer.clear();
		return this;
	}

	@Override
	public int position() {
		return position;
	}

	private class PlaceHolderImpl implements PlaceHolder {

		private final byte[] part;

		PlaceHolderImpl(final byte[] part) {

			this.part = part;
		}

		@Override
		public void replace(final byte[] src) throws OSCSerializeException {

			if (src.length != part.length) {
				throw new OSCSerializeException(String.format(
						"Trying to replace placeholder of size %d with data of size %d",
						part.length, src.length));
			}
			System.arraycopy(src, 0, part, 0, src.length);
		}
	}

	@Override
	public PlaceHolder putPlaceHolder(final byte[] src) {

		final PlaceHolderImpl placeHolder = new PlaceHolderImpl(src);
		put(src);
		return placeHolder;
	}

	@Override
	public byte[] toByteArray() {

		final byte[] bytes = new byte[position];
		int curPos = 0;
		for (byte[] curPart : buffer) {
			System.arraycopy(curPart, 0, bytes, curPos, curPart.length);
			curPos += curPart.length;
		}
		return bytes;
	}
}
