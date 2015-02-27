/*
 * Copyright (C) 2015, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.argument.handler;

import com.illposed.osc.OSCParseException;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.OSCSerializer;
import com.illposed.osc.argument.ArgumentHandler;
import com.illposed.osc.SizeTrackingOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Parses and serializes an OSC character type.
 */
public class CharArgumentHandler implements ArgumentHandler<Character>, Cloneable {

	public static final ArgumentHandler<Character> INSTANCE = new CharArgumentHandler();

	/** Allow overriding, but somewhat enforce the ugly singleton. */
	protected CharArgumentHandler() {
		// ctor declared only for setting the access level
	}

	@Override
	public char getDefaultIdentifier() {
		return 'c';
	}

	@Override
	public Class<Character> getJavaClass() {
		return Character.class;
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
	public CharArgumentHandler clone() throws CloneNotSupportedException {
		return (CharArgumentHandler) super.clone();
	}

	@Override
	public Character parse(final ByteBuffer input) throws OSCParseException {
		return (char) input.get();
	}

	@Override
	public void serialize(final SizeTrackingOutputStream stream, final Character value)
			throws IOException, OSCSerializeException
	{
		stream.write((char) value);
		OSCSerializer.align(stream);
	}
}
