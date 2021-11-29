// SPDX-FileCopyrightText: 2015-2017 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc.argument.handler;

import com.illposed.osc.BytesReceiver;
import com.illposed.osc.OSCParseException;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.argument.ArgumentHandler;
import com.illposed.osc.argument.OSCColor;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Parses and serializes an OSC 1.1 optional <i>32bit RGBA color</i> type.
 *
 * Note That this class is not in javaosc-core,
 * because it uses <code>java.awt.Color</code>,
 * which is part of Java SE,
 * but not part of the Java implementation on Android (Dalvik),
 * and thus would make JavaOSC unusable on Android,
 * if it were not separated.
 * It was part of javaosc-core until version 0.8.
 *
 * As an alternative on Android,
 * see <code>com.illposed.osc.argument.handler.ColorArgumentHandler</code>
 * in javaosc-core.
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
	public void serialize(final BytesReceiver output, final Color value) throws OSCSerializeException {
		ColorArgumentHandler.INSTANCE.serialize(output, toOsc(value));
	}
}
