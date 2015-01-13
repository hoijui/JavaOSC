/*
 * Copyright (C) 2015, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.utility;

/**
 * Represents a raw, low-level OSC input, being processed.
 */
public class OSCInput {

	private final byte[] bytes;
	private final int bytesLength;
	private int streamPosition;

	public OSCInput(final byte[] bytes, final int bytesLength) {

		this.bytes = bytes;
		this.bytesLength = bytesLength;
		this.streamPosition = 0;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public int getBytesLength() {
		return bytesLength;
	}

	public int getAndIncreaseStreamPositionByOne() {
		return streamPosition++;
	}

	public void addToStreamPosition(final int toAdd) {
		streamPosition += toAdd;
	}

	public int getStreamPosition() {
		return streamPosition;
	}
}
