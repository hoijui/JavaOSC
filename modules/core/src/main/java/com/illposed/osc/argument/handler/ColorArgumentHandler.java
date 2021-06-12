// SPDX-FileCopyrightText: 2015-2017 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc.argument.handler;

import com.illposed.osc.BytesReceiver;
import com.illposed.osc.argument.ArgumentHandler;
import com.illposed.osc.argument.OSCColor;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Parses and serializes an OSC 1.1 optional <i>32bit RGBA color</i> type.
 */
public class ColorArgumentHandler implements ArgumentHandler<OSCColor>, Cloneable {

	public static final ArgumentHandler<OSCColor> INSTANCE = new ColorArgumentHandler();

	// Public API
	/** Allow overriding, but somewhat enforce the ugly singleton. */
	@SuppressWarnings("WeakerAccess")
	protected ColorArgumentHandler() {
		// declared only for setting the access level
	}

	@Override
	public char getDefaultIdentifier() {
		return 'r';
	}

	@Override
	public Class<OSCColor> getJavaClass() {
		return OSCColor.class;
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
	public ColorArgumentHandler clone() throws CloneNotSupportedException {
		return (ColorArgumentHandler) super.clone();
	}

	@Override
	public OSCColor parse(final ByteBuffer input) {
		return new OSCColor(
				input.get(),
				input.get(),
				input.get(),
				input.get());
	}

	@Override
	public void serialize(final BytesReceiver output, final OSCColor value) {

		output.put(value.getRed());
		output.put(value.getGreen());
		output.put(value.getBlue());
		output.put(value.getAlpha());
	}
}
