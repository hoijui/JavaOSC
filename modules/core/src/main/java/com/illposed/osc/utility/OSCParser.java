/*
 * Copyright (C) 2004-2014, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.utility;

import com.illposed.osc.OSCTimeStamp;
import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCImpulse;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPacket;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to convert a byte array,
 * conforming to the OSC byte stream format,
 * into Java objects.
 */
public class OSCParser {

	private static final String BUNDLE_START = "#bundle";
	private static final char BUNDLE_IDENTIFIER = BUNDLE_START.charAt(0);
	private static final String NO_ARGUMENT_TYPES = "";


	/** Used to decode message addresses and string parameters. */
	private Charset charset;

	/**
	 * Creates a helper object for converting from a byte array
	 * to an {@link OSCPacket} object.
	 */
	public OSCParser() {

		this.charset = Charset.defaultCharset();
	}

	/**
	 * Returns the character set used to decode message addresses
	 * and string parameters.
	 * @return the character-encoding-set used by this converter
	 */
	public Charset getCharset() {
		return charset;
	}

	/**
	 * Sets the character set used to decode message addresses
	 * and string parameters.
	 * @param charset the desired character-encoding-set to be used by this converter
	 */
	public void setCharset(final Charset charset) {
		this.charset = charset;
	}

	/**
	 * Converts a byte array into an {@link OSCPacket}
	 * (either an {@link OSCMessage} or {@link OSCBundle}).
	 * @param bytes the storage containing the raw OSC packet
	 * @param bytesLength indicates how many bytes the package consists of (<code>&lt;= bytes.length</code>)
	 * @return the successfully parsed OSC packet; in case of a problem,
	 *   a <code>RuntimeException</code> is thrown
	 */
	public OSCPacket convert(final byte[] bytes, final int bytesLength) {

		final OSCInput rawInput = new OSCInput(bytes, bytesLength);
		final OSCPacket packet;
		if (isBundle(rawInput)) {
			packet = convertBundle(rawInput);
		} else {
			packet = convertMessage(rawInput);
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
	private boolean isBundle(final OSCInput rawInput) {
		// The shortest valid packet may be no shorter then 4 bytes,
		// thus we may assume to always have a byte at index 0.
		return rawInput.getBytes()[0] == BUNDLE_IDENTIFIER;
	}

	/**
	 * Converts the byte array to a bundle.
	 * Assumes that the byte array is a bundle.
	 * @return a bundle containing the data specified in the byte stream
	 */
	private OSCBundle convertBundle(final OSCInput rawInput) {
		// skip the "#bundle " stuff
		rawInput.addToStreamPosition(BUNDLE_START.length() + 1);
		final OSCTimeStamp timestamp = readTimeTag(rawInput);
		final OSCBundle bundle = new OSCBundle(timestamp);
		final OSCParser myConverter = new OSCParser();
		myConverter.setCharset(charset);
		while (rawInput.getStreamPosition() < rawInput.getBytesLength()) {
			// recursively read through the stream and convert packets you find
			final int packetLength = readInteger(rawInput);
			if (packetLength == 0) {
				throw new IllegalArgumentException("Packet length may not be 0");
			} else if ((packetLength % 4) != 0) {
				throw new IllegalArgumentException("Packet length has to be a multiple of 4, is:"
						+ packetLength);
			}
			final byte[] packetBytes = new byte[packetLength];
			System.arraycopy(rawInput.getBytes(), rawInput.getStreamPosition(), packetBytes, 0, packetLength);
			rawInput.addToStreamPosition(packetLength);
			final OSCPacket packet = myConverter.convert(packetBytes, packetLength);
			bundle.addPacket(packet);
		}
		return bundle;
	}

	/**
	 * Converts the byte array to a simple message.
	 * Assumes that the byte array is a message.
	 * @return a message containing the data specified in the byte stream
	 */
	private OSCMessage convertMessage(final OSCInput rawInput) {
		final OSCMessage message = new OSCMessage();
		message.setAddress(readString(rawInput));
		final CharSequence types = readTypes(rawInput);
		for (int ti = 0; ti < types.length(); ++ti) {
			if ('[' == types.charAt(ti)) {
				// we're looking at an array -- read it in
				message.addArgument(readArray(rawInput, types, ++ti));
				// then increment i to the end of the array
				while (types.charAt(ti) != ']') {
					ti++;
				}
			} else {
				message.addArgument(readArgument(rawInput, types.charAt(ti)));
			}
		}
		return message;
	}

	/**
	 * Reads a string from the byte stream.
	 * @return the next string in the byte stream
	 */
	private String readString(final OSCInput rawInput) {
		final int strLen = lengthOfCurrentString(rawInput);
		final String res = new String(rawInput.getBytes(), rawInput.getStreamPosition(), strLen, charset);
		rawInput.addToStreamPosition(strLen);
		moveToFourByteBoundry(rawInput);
		return res;
	}

	/**
	 * Reads a binary blob from the byte stream.
	 * @return the next blob in the byte stream
	 */
	private byte[] readBlob(final OSCInput rawInput) {
		final int blobLen = readInteger(rawInput);
		final byte[] res = new byte[blobLen];
		System.arraycopy(rawInput.getBytes(), rawInput.getStreamPosition(), res, 0, blobLen);
		rawInput.addToStreamPosition(blobLen);
		moveToFourByteBoundry(rawInput);
		return res;
	}

	/**
	 * Reads the types of the arguments from the byte stream.
	 * @return a char array with the types of the arguments,
	 *   or <code>null</code>, in case of no arguments
	 */
	private CharSequence readTypes(final OSCInput rawInput) {
		final String typesStr;

		// The next byte should be a ',', but some legacy code may omit it
		// in case of no arguments, refering to "OSC Messages" in:
		// http://opensoundcontrol.org/spec-1_0
		if (rawInput.getBytes().length <= rawInput.getStreamPosition()) {
			typesStr = NO_ARGUMENT_TYPES;
		} else if (rawInput.getBytes()[rawInput.getStreamPosition()] == ',') {
			rawInput.getAndIncreaseStreamPositionByOne();
			typesStr = readString(rawInput);
		} else {
			// XXX should we not rather fail-fast -> throw exception?
			typesStr = NO_ARGUMENT_TYPES;
		}

		return typesStr;
	}

	/**
	 * Reads an object of the type specified by the type char.
	 * @param type type of the argument to read
	 * @return a Java representation of the argument
	 */
	private Object readArgument(final OSCInput rawInput, final char type) {
		switch (type) {
			case 'u' :
				return readUnsignedInteger(rawInput);
			case 'i' :
				return readInteger(rawInput);
			case 'h' :
				return readLong(rawInput);
			case 'f' :
				return readFloat(rawInput);
			case 'd' :
				return readDouble(rawInput);
			case 's' :
				return readString(rawInput);
			case 'b' :
				return readBlob(rawInput);
			case 'c' :
				return readChar(rawInput);
			case 'N' :
				return null;
			case 'T' :
				return Boolean.TRUE;
			case 'F' :
				return Boolean.FALSE;
			case 'I' :
				return OSCImpulse.INSTANCE;
			case 't' :
				return readTimeTag(rawInput);
			default:
				// XXX Maybe we should let the user choose what to do in this
				//   case (we encountered an unknown argument type in an
				//   incomming message):
				//   just ignore (return null), or throw an exception?
//				throw new UnsupportedOperationException(
//						"Invalid or not yet supported OSC type: '" + type + "'");
				return null;
		}
	}

	/**
	 * Reads a char from the byte stream.
	 * @return a {@link Character}
	 */
	private Character readChar(final OSCInput rawInput) {
		return (char) rawInput.getBytes()[rawInput.getAndIncreaseStreamPositionByOne()];
	}

	private BigInteger readBigInteger(final OSCInput rawInput, final int numBytes) {
		final byte[] myBytes = new byte[numBytes];
		System.arraycopy(rawInput.getBytes(), rawInput.getStreamPosition(), myBytes, 0, numBytes);
		rawInput.addToStreamPosition(numBytes);
		return  new BigInteger(myBytes);
	}

	/**
	 * Reads a double from the byte stream.
	 * @return a 64bit precision floating point value
	 */
	private Object readDouble(final OSCInput rawInput) {
		final BigInteger doubleBits = readBigInteger(rawInput, 8);
		return Double.longBitsToDouble(doubleBits.longValue());
	}

	/**
	 * Reads a float from the byte stream.
	 * @return a 32bit precision floating point value
	 */
	private Float readFloat(final OSCInput rawInput) {
		final BigInteger floatBits = readBigInteger(rawInput, 4);
		return Float.intBitsToFloat(floatBits.intValue());
	}

	/**
	 * Reads a double precision integer (64 bit integer) from the byte stream.
	 * @return double precision integer (64 bit)
	 */
	private Long readLong(final OSCInput rawInput) {
		final BigInteger longintBytes = readBigInteger(rawInput, 8);
		return longintBytes.longValue();
	}

	/**
	 * Reads an Integer (32 bit integer) from the byte stream.
	 * @return an {@link Integer}
	 */
	private Integer readInteger(final OSCInput rawInput) {
		final BigInteger intBits = readBigInteger(rawInput, 4);
		return intBits.intValue();
	}

	/**
	 * Reads an unsigned integer (32 bit) from the byte stream.
	 * This code is copied from {@see http://darksleep.com/player/JavaAndUnsignedTypes.html},
	 * which is licensed under the Public Domain.
	 * @return single precision, unsigned integer (32 bit) wrapped in a 64 bit integer (long)
	 */
	private Long readUnsignedInteger(final OSCInput rawInput) {

		final int firstByte = (0x000000FF & ((int) rawInput.getBytes()[rawInput.getAndIncreaseStreamPositionByOne()]));
		final int secondByte = (0x000000FF & ((int) rawInput.getBytes()[rawInput.getAndIncreaseStreamPositionByOne()]));
		final int thirdByte = (0x000000FF & ((int) rawInput.getBytes()[rawInput.getAndIncreaseStreamPositionByOne()]));
		final int fourthByte = (0x000000FF & ((int) rawInput.getBytes()[rawInput.getAndIncreaseStreamPositionByOne()]));
		return ((long) (firstByte << 24
				| secondByte << 16
				| thirdByte << 8
				| fourthByte))
				& 0xFFFFFFFFL;
	}

	/**
	 * Reads the time tag and convert it to an OSCTimeStamp, which can be converted to
	 * a Java Date object.
	 * A timestamp is a 64 bit number representing the time in NTP format.
	 * The first 32 bits are seconds since 1900, the second 32 bits are
	 * fractions of a second.
	 * @return
	 */
	private OSCTimeStamp readTimeTag(final OSCInput rawInput) {
		final long ntpTime = readLong(rawInput);
		return OSCTimeStamp.valueOf(ntpTime);
	}

	/**
	 * Reads an array from the byte stream.
	 * @param types
	 * @param pos at which position to start reading
	 * @return the array that was read
	 */
	private List<Object> readArray(final OSCInput rawInput, final CharSequence types, final int pos) {
		int arrayLen = 0;
		while (types.charAt(pos + arrayLen) != ']') {
			arrayLen++;
		}
		final List<Object> array = new ArrayList<Object>(arrayLen);
		for (int ai = 0; ai < arrayLen; ai++) {
			array.add(readArgument(rawInput, types.charAt(pos + ai)));
		}
		return array;
	}

	/**
	 * Get the length of the string currently in the byte stream.
	 */
	private int lengthOfCurrentString(final OSCInput rawInput) {
		int len = 0;
		while (rawInput.getBytes()[rawInput.getStreamPosition() + len] != 0) {
			len++;
		}
		return len;
	}

	/**
	 * Move to the next byte with an index in the byte array
	 * which is dividable by four.
	 */
	private void moveToFourByteBoundry(final OSCInput rawInput) {
		// If i am already at a 4 byte boundry, I need to move to the next one
		final int mod = rawInput.getStreamPosition() % 4;
		rawInput.addToStreamPosition(4 - mod);
	}
}
