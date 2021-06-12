// SPDX-FileCopyrightText: 2015-2017 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc.argument.handler;

import com.illposed.osc.BytesReceiver;
import com.illposed.osc.argument.OSCMidiMessage;
import com.illposed.osc.argument.ArgumentHandler;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Parses and serializes an OSC MIDI message type (4 bytes).
 */
public class MidiMessageArgumentHandler implements ArgumentHandler<OSCMidiMessage>, Cloneable {

	public static final ArgumentHandler<OSCMidiMessage> INSTANCE = new MidiMessageArgumentHandler();

	// Public API
	/** Allow overriding, but somewhat enforce the ugly singleton. */
	@SuppressWarnings("WeakerAccess")
	protected MidiMessageArgumentHandler() {
		// declared only for setting the access level
	}

	@Override
	public char getDefaultIdentifier() {
		return 'm';
	}

	@Override
	public Class<OSCMidiMessage> getJavaClass() {
		return OSCMidiMessage.class;
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
	public MidiMessageArgumentHandler clone() throws CloneNotSupportedException {
		return (MidiMessageArgumentHandler) super.clone();
	}

	@Override
	public OSCMidiMessage parse(final ByteBuffer input) {
		return OSCMidiMessage.valueOf(new byte[] {
				input.get(),
				input.get(),
				input.get(),
				input.get()});
	}

	@Override
	public void serialize(final BytesReceiver output, final OSCMidiMessage value) {
		output.put(value.toContentArray());
	}
}
