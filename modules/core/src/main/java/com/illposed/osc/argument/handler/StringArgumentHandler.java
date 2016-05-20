/*
 * Copyright (C) 2015, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.argument.handler;

import com.illposed.osc.OSCParseException;
import com.illposed.osc.OSCParser;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.OSCSerializer;
import com.illposed.osc.argument.ArgumentHandler;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Parses and serializes an OSC string type.
 */
public class StringArgumentHandler implements ArgumentHandler<String>, Cloneable {

	public static final char DEFAULT_IDENTIFIER = 's';
	public static final String PROP_NAME_CHARSET = "charset";

	private Charset charset;

	public StringArgumentHandler(final Charset charset) {
		this.charset = charset;
	}
	public StringArgumentHandler() {
		this(Charset.defaultCharset());
	}

	/**
	 * Returns the character-set used to encode and decode string arguments.
	 * @return the currently used character-encoding-set
	 */
	public Charset getCharset() {
		return charset;
	}

	/**
	 * Sets the character-set used to encode and decode string arguments.
	 * @param charset the new character-encoding-set
	 */
	public void setCharset(final Charset charset) {
		this.charset = charset;
	}


	@Override
	public char getDefaultIdentifier() {
		return DEFAULT_IDENTIFIER;
	}

	@Override
	public Class<String> getJavaClass() {
		return String.class;
	}

	@Override
	public void setProperties(final Map<String, Object> properties) {

		final Charset newCharset = (Charset) properties.get(PROP_NAME_CHARSET);
		if (newCharset != null) {
			setCharset(newCharset);
		}
	}

	@Override
	public boolean isMarkerOnly() {
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public StringArgumentHandler clone() throws CloneNotSupportedException {
		return (StringArgumentHandler) super.clone();
	}

	/**
	 * Get the length of the string currently in the byte stream.
	 */
	private int lengthOfCurrentString(final ByteBuffer rawInput) {
		int len = 0;
		while (rawInput.get(rawInput.position() + len) != 0) {
			len++;
		}
		return len;
	}

	@Override
	public String parse(final ByteBuffer input) throws OSCParseException {

		final int strLen = lengthOfCurrentString(input);
		final ByteBuffer strBuffer = input.slice();
		strBuffer.limit(strLen);
		final String res;
		try {
			res = charset.newDecoder().decode(strBuffer).toString();
		} catch (final CharacterCodingException ex) {
			throw new OSCParseException("Failed decoding a string argument", ex);
		}
		input.position(input.position() + strLen);
		// because strings are always padded with at least one zero,
		// as their length is not given in advance, as is the case with blobs
		input.get(); // position++ to skip the terminating {@code (byte) 0}
		OSCParser.align(input);
		return res;
	}

	@Override
	public void serialize(final ByteBuffer output, final String value) throws OSCSerializeException {

		final byte[] stringBytes = value.getBytes(charset);
		output.put(stringBytes);
		OSCSerializer.terminateAndAlign(output);
	}
}
