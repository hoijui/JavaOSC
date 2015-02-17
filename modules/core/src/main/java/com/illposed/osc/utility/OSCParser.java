/*
 * Copyright (C) 2004-2014, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.utility;

import com.illposed.osc.argument.OSCTimeStamp;
import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPacket;
import com.illposed.osc.argument.ArgumentHandler;
import com.illposed.osc.argument.handler.IntegerArgumentHandler;
import com.illposed.osc.argument.handler.TimeStampArgumentHandler;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utility class to convert a byte array,
 * conforming to the OSC byte stream format,
 * into Java objects.
 */
public class OSCParser {

	private static final String BUNDLE_START = "#bundle";
	private static final char BUNDLE_IDENTIFIER = BUNDLE_START.charAt(0);
	private static final String NO_ARGUMENT_TYPES = "";
	public static final byte TYPES_VALUES_SEPARATOR = (byte) ',';

	private final Map<Character, ArgumentHandler> identifierToType;

	public OSCParser(final Map<Character, ArgumentHandler> identifierToType) {

		this.identifierToType = Collections.unmodifiableMap(identifierToType);
	}

	public Map<Character, ArgumentHandler> getIdentifierToTypeMapping() {
		return identifierToType;
	}

	/**
	 * Converts a byte array into an {@link OSCPacket}
	 * (either an {@link OSCMessage} or {@link OSCBundle}).
	 * @param bytes the storage containing the raw OSC packet
	 * @param bytesLength indicates how many bytes the package consists of (<code>&lt;= bytes.length</code>)
	 * @return the successfully parsed OSC packet; in case of a problem,
	 *   a <code>RuntimeException</code> is thrown
	 * @deprecated use {@link #convert(ByteBuffer)} instead
	 * @throws OSCParseException if the input has an invalid format
	 */
	public OSCPacket convert(final byte[] bytes, final int bytesLength) throws OSCParseException {
		return convert(ByteBuffer.wrap(bytes, 0, bytesLength).asReadOnlyBuffer());
	}
	public OSCPacket convert(final ByteBuffer rawInput) throws OSCParseException {

		final ByteBuffer readOnlyInput = rawInput.asReadOnlyBuffer();
		final OSCPacket packet;
		if (isBundle(readOnlyInput)) {
			packet = convertBundle(readOnlyInput);
		} else {
			packet = convertMessage(readOnlyInput);
		}

		return packet;
	}

	/**
	 * Checks whether my byte array is a bundle.
	 * From the OSC 1.0 specifications:
	 * <quote>
	 * The contents of an OSC packet must be either an OSC Message
	 * or an OSC Bundle. The first byte of the packet's contents unambiguously
	 * distinguishes between these two alternatives.
	 * </quote>
	 * @return true if it the byte array is a bundle, false o.w.
	 */
	private boolean isBundle(final ByteBuffer rawInput) {
		// The shortest valid packet may be no shorter then 4 bytes,
		// thus we may assume to always have a byte at index 0.
		return rawInput.get(0) == BUNDLE_IDENTIFIER;
	}

	/**
	 * Converts the byte array to a bundle.
	 * Assumes that the byte array is a bundle.
	 * @return a bundle containing the data specified in the byte stream
	 */
	private OSCBundle convertBundle(final ByteBuffer rawInput) throws OSCParseException {
		// skip the "#bundle " stuff
		rawInput.position(BUNDLE_START.length() + 1);
		final OSCTimeStamp timestamp = TimeStampArgumentHandler.INSTANCE.parse(rawInput);
		final OSCBundle bundle = new OSCBundle(timestamp);
		while (rawInput.hasRemaining()) {
			// recursively read through the stream and convert packets you find
			final int packetLength = IntegerArgumentHandler.INSTANCE.parse(rawInput);
			if (packetLength == 0) {
				throw new IllegalArgumentException("Packet length may not be 0");
			} else if ((packetLength % 4) != 0) {
				throw new IllegalArgumentException("Packet length has to be a multiple of 4, is:"
						+ packetLength);
			}
			final ByteBuffer packetBytes = rawInput.slice();
			packetBytes.limit(packetLength);
			rawInput.position(rawInput.position() + packetLength);
			final OSCPacket packet = convert(packetBytes);
			bundle.addPacket(packet);
		}
		return bundle;
	}

	/**
	 * Converts the byte array to a simple message.
	 * Assumes that the byte array is a message.
	 * @return a message containing the data specified in the byte stream
	 */
	private OSCMessage convertMessage(final ByteBuffer rawInput) throws OSCParseException {
		final OSCMessage message = new OSCMessage();
		message.setAddress(readString(rawInput));
		final CharSequence typeIdentifiers = readTypes(rawInput);
		for (int ti = 0; ti < typeIdentifiers.length(); ++ti) {
			if ('[' == typeIdentifiers.charAt(ti)) {
				// we're looking at an array -- read it in
				message.addArgument(readArray(rawInput, typeIdentifiers, ++ti));
				// then increment i to the end of the array
				while (typeIdentifiers.charAt(ti) != ']') {
					ti++;
				}
			} else {
				message.addArgument(readArgument(rawInput, typeIdentifiers.charAt(ti)));
			}
		}
		return message;
	}

	/**
	 * Reads a string from the byte stream.
	 * @return the next string in the byte stream
	 */
	private String readString(final ByteBuffer rawInput) throws OSCParseException {
		return (String) identifierToType.get('s').parse(rawInput);
	}

	/**
	 * Reads the types of the arguments from the byte stream.
	 * @return a char array with the types of the arguments,
	 *   or <code>null</code>, in case of no arguments
	 */
	private CharSequence readTypes(final ByteBuffer rawInput) throws OSCParseException {
		final String typesStr;

		// The next byte should be a TYPES_VALUES_SEPARATOR, but some legacy code may omit it
		// in case of no arguments, refering to "OSC Messages" in:
		// http://opensoundcontrol.org/spec-1_0
		if (rawInput.hasRemaining()) {
			if (rawInput.get(rawInput.position()) == TYPES_VALUES_SEPARATOR) {
				rawInput.get(); // position++
				typesStr = readString(rawInput);
			} else {
				// the message format is invalid
				// XXX should we not rather fail-fast -> throw exception?
				typesStr = NO_ARGUMENT_TYPES;
			}
		} else {
			// there are no arguments
			typesStr = NO_ARGUMENT_TYPES;
		}

		return typesStr;
	}

	/**
	 * Reads an object of the type specified by the type char.
	 * @param typeIdentifier type of the argument to read
	 * @return a Java representation of the argument
	 */
	private Object readArgument(final ByteBuffer rawInput, final char typeIdentifier)
			throws OSCParseException
	{
		final Object argumentValue;

		final ArgumentHandler type = identifierToType.get(typeIdentifier);
		if (type == null) {
			// XXX Maybe we should let the user choose what to do in this
			//   case (we encountered an unknown argument type in an
			//   incomming message):
			//   just ignore (return null), or throw an exception?
//			throw new UnsupportedOperationException(
//					"Invalid or not yet supported OSC type: '" + type + "'");
			argumentValue = null;
		} else {
			argumentValue = type.parse(rawInput);
		}

		return argumentValue;
	}

	/**
	 * Reads an array from the byte stream.
	 * @param typeIdentifiers
	 * @param pos at which position to start reading
	 * @return the array that was read
	 */
	private List<Object> readArray(
			final ByteBuffer rawInput, final CharSequence typeIdentifiers, final int pos)
			throws OSCParseException
	{
		int arrayLen = 0;
		while (typeIdentifiers.charAt(pos + arrayLen) != ']') {
			arrayLen++;
		}
		final List<Object> array = new ArrayList<Object>(arrayLen);
		for (int ai = 0; ai < arrayLen; ai++) {
			array.add(readArgument(rawInput, typeIdentifiers.charAt(pos + ai)));
		}
		return array;
	}
}
