// SPDX-FileCopyrightText: 2015-2017 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc.argument;

import java.io.Serializable;

/**
 * This represents an OSC 1.1 optional <i>32bit RGBA color</i> type.
 */
public class OSCColor implements Cloneable, Serializable, Comparable<OSCColor> {

	public static final int NUM_CONTENT_BYTES = 4;
	/**
	 * Full opacity/no transparency.
	 */
	public static final byte DEFAULT_ALPHA = 1;
	public static final OSCColor BLACK = new OSCColor((byte) 0, (byte) 0, (byte) 0);
	public static final OSCColor BLUE = new OSCColor((byte) 0, (byte) 0, (byte) 255);
	public static final OSCColor CYAN = new OSCColor((byte) 0, (byte) 255, (byte) 255);
	public static final OSCColor DARK_GRAY = new OSCColor((byte) 64, (byte) 64, (byte) 64);
	public static final OSCColor GRAY = new OSCColor((byte) 128, (byte) 128, (byte) 128);
	public static final OSCColor GREEN = new OSCColor((byte) 0, (byte) 255, (byte) 0);
	public static final OSCColor LIGHT_GRAY = new OSCColor((byte) 192, (byte) 192, (byte) 192);
	public static final OSCColor MAGENTA = new OSCColor((byte) 255, (byte) 0, (byte) 255);
	public static final OSCColor ORANGE = new OSCColor((byte) 255, (byte) 200, (byte) 0);
	public static final OSCColor PINK = new OSCColor((byte) 255, (byte) 175, (byte) 175);
	public static final OSCColor RED = new OSCColor((byte) 255, (byte) 0, (byte) 0);
	public static final OSCColor WHITE = new OSCColor((byte) 255, (byte) 255, (byte) 255);
	public static final OSCColor YELLOW = new OSCColor((byte) 255, (byte) 255, (byte) 0);
	private static final long serialVersionUID = 1L;
	private final byte red;
	private final byte green;
	private final byte blue;
	private final byte alpha;

	// Public API
	/**
	 * Converts the argument to an {@code int} by an unsigned conversion.
	 *
	 * @param signedByte the value to convert to an unsigned {@code int}
	 * @return the argument converted to {@code int} by an unsigned conversion NOTE Since Java 8,
	 * one could use Byte#toUnsignedInt
	 */
	@SuppressWarnings("WeakerAccess")
	public static int toUnsignedInt(final byte signedByte) {
		return ((int) signedByte) & 0xff;
	}

	// Public API
	/**
	 * Converts the argument to an {@code byte} by a sign introducing conversion.
	 *
	 * @param unsignedInt the value to convert to a signed {@code byte}; has to be in range [0, 255]
	 * @return the argument converted to {@code byte} by a sign introducing conversion
	 */
	@SuppressWarnings("WeakerAccess")
	public static byte toSignedByte(final int unsignedInt) {
		return (byte) unsignedInt;
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public OSCColor(final byte red, final byte green, final byte blue, final byte alpha) {

		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public OSCColor(final byte red, final byte green, final byte blue) {
		this(red, green, blue, DEFAULT_ALPHA);
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public OSCColor(final int red, final int green, final int blue, final int alpha) {
		this(toSignedByte(red), toSignedByte(green), toSignedByte(blue), toSignedByte(alpha));
	}

	public byte[] toContentArray() {
		return new byte[] {getRed(), getGreen(), getBlue(), getAlpha()};
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public byte getRed() {
		return red;
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public byte getGreen() {
		return green;
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public byte getBlue() {
		return blue;
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public byte getAlpha() {
		return alpha;
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public int getRedInt() {
		return toUnsignedInt(getRed());
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public int getGreenInt() {
		return toUnsignedInt(getGreen());
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public int getBlueInt() {
		return toUnsignedInt(getBlue());
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public int getAlphaInt() {
		return toUnsignedInt(getAlpha());
	}

	@Override
	public boolean equals(final Object other) {

		boolean equal = false;
		if (other instanceof OSCColor) {
			final OSCColor otherColor = (OSCColor) other;
			if (
					(this.getRed() == otherColor.getRed())
					&& (this.getGreen() == otherColor.getGreen())
					&& (this.getBlue() == otherColor.getBlue())
					&& (this.getAlpha() == otherColor.getAlpha()))
			{
				equal = true;
			}
		}

		return equal;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = (97 * hash) + this.getRed();
		hash = (97 * hash) + this.getGreen();
		hash = (97 * hash) + this.getBlue();
		hash = (97 * hash) + this.getAlpha();
		return hash;
	}

	@Override
	public int compareTo(final OSCColor other) {
		return Integer.compare(hashCode(), other.hashCode());
	}

	@Override
	public OSCColor clone() throws CloneNotSupportedException {
		return (OSCColor) super.clone();
	}

	public static OSCColor valueOf(final byte[] content) {

		if (content.length != NUM_CONTENT_BYTES) {
			throw new IllegalArgumentException("The content has to be exactly " + NUM_CONTENT_BYTES
					+ " bytes");
		}
		int contentByteIndex = 0;
		return new OSCColor(
				content[contentByteIndex++],
				content[contentByteIndex++],
				content[contentByteIndex++],
				content[contentByteIndex]);
	}
}
