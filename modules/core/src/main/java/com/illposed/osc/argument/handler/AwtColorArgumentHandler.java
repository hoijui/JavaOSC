/*
 * Copyright (C) 2015-2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.illposed.osc.argument.handler;

import com.illposed.osc.OSCParseException;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.argument.ArgumentHandler;
import com.illposed.osc.argument.OSCColor;

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

	public static Color toAwt(final OSCColor color) {
		return new Color(
				color.getRedInt(),
				color.getGreenInt(),
				color.getBlueInt(),
				color.getAlphaInt());
	}

	public static OSCColor toOsc(final Color color) {
		return new OSCColor(
				color.getRed(),
				color.getGreen(),
				color.getBlue(),
				color.getAlpha());
	}

	@Override
	public Color parse(final ByteBuffer input) throws OSCParseException {
		return toAwt(ColorArgumentHandler.INSTANCE.parse(input));
	}

	@Override
	public void serialize(final ByteBuffer output, final Color value) throws OSCSerializeException {
		ColorArgumentHandler.INSTANCE.serialize(output, toOsc(value));
	}
}
