/*
 * Copyright (C) 2015, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.argument.handler;

import com.illposed.osc.argument.OSCMidiMessage;
import com.illposed.osc.argument.ArgumentHandler;
import com.illposed.osc.OSCParseException;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.SizeTrackingOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Parses and serializes an OSC MIDI message type (4 bytes).
 */
public class MidiMessageArgumentHandler implements ArgumentHandler<OSCMidiMessage>, Cloneable {

	public static final ArgumentHandler<OSCMidiMessage> INSTANCE = new MidiMessageArgumentHandler();

	/** Allow overriding, but somewhat enforce the ugly singleton. */
	protected MidiMessageArgumentHandler() {
		// ctor declared only for setting the access level
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
	public OSCMidiMessage parse(final ByteBuffer input) throws OSCParseException {
		return OSCMidiMessage.valueOf(new byte[] {
				input.get(),
				input.get(),
				input.get(),
				input.get()});
	}

	@Override
	public void serialize(final SizeTrackingOutputStream stream, final OSCMidiMessage value)
			throws IOException, OSCSerializeException
	{
		stream.write(value.toContentArray());
	}
}
