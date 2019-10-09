/*
 * Copyright (C) 2015-2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.argument.handler;

import com.illposed.osc.argument.ArgumentHandler;
import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Parses and serializes an OSC 1.1 optional <i>32bit RGBA color</i> type.
 */
public class AwtColorArgumentHandler implements ArgumentHandler<Color>, Cloneable {

	public static final ArgumentHandler<Color> INSTANCE = new AwtColorArgumentHandler();

	// Public API
	/** Allow overriding, but somewhat enforce the ugly singleton. */
	@SuppressWarnings("WeakerAccess")
	protected AwtColorArgumentHandler() {
		// declared only for setting the access level
	}

	@Override
	public char getDefaultIdentifier() {
		return 'r';
	}

	@Override
	public Class<Color> getJavaClass() {
		return Color.class;
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
	public AwtColorArgumentHandler clone() throws CloneNotSupportedException {
		return (AwtColorArgumentHandler) super.clone();
	}

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

	@Override
	public Color parse(final ByteBuffer input) {
		return new Color(
				toUnsignedInt(input.get()),
				toUnsignedInt(input.get()),
				toUnsignedInt(input.get()),
				toUnsignedInt(input.get()));
	}

	@Override
	public void serialize(final ByteBuffer output, final Color value) {

		output.put(toSignedByte(value.getRed()));
		output.put(toSignedByte(value.getGreen()));
		output.put(toSignedByte(value.getBlue()));
		output.put(toSignedByte(value.getAlpha()));
	}
}
