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
import com.illposed.osc.OSCUnsigned;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
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
	 * @deprecated use {@link #convert(ByteBuffer)} instead
	 */
	public OSCPacket convert(final byte[] bytes, final int bytesLength) {
		return convert(ByteBuffer.wrap(bytes, 0, bytesLength).asReadOnlyBuffer());
	}
	public OSCPacket convert(final ByteBuffer rawInput) {

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
	private OSCBundle convertBundle(final ByteBuffer rawInput) {
		// skip the "#bundle " stuff
		rawInput.position(BUNDLE_START.length() + 1);
		final OSCTimeStamp timestamp = readTimeTag(rawInput);
		final OSCBundle bundle = new OSCBundle(timestamp);
		final OSCParser myConverter = new OSCParser();
		myConverter.setCharset(getCharset());
		while (rawInput.hasRemaining()) {
			// recursively read through the stream and convert packets you find
			final int packetLength = readInteger(rawInput);
			if (packetLength == 0) {
				throw new IllegalArgumentException("Packet length may not be 0");
			} else if ((packetLength % 4) != 0) {
				throw new IllegalArgumentException("Packet length has to be a multiple of 4, is:"
						+ packetLength);
			}
			final ByteBuffer packetBytes = rawInput.slice();
			packetBytes.limit(packetLength);
			rawInput.position(rawInput.position() + packetLength);
			final OSCPacket packet = myConverter.convert(packetBytes);
			bundle.addPacket(packet);
		}
		return bundle;
	}

	/**
	 * Converts the byte array to a simple message.
	 * Assumes that the byte array is a message.
	 * @return a message containing the data specified in the byte stream
	 */
	private OSCMessage convertMessage(final ByteBuffer rawInput) {
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
	private String readString(final ByteBuffer rawInput) {
		final int strLen = lengthOfCurrentString(rawInput);
		final ByteBuffer strBuffer = rawInput.slice();
		strBuffer.limit(strLen);
		final String res;
		try {
			res = charset.newDecoder().decode(strBuffer).toString();
		} catch (final CharacterCodingException ex) {
			throw new IllegalStateException(ex);
		}
		rawInput.position(rawInput.position() + strLen);
		// because strings are always padded with at least one zero,
		// as their length is not given in advance, as is the case with blobs
		rawInput.get(); // position++
		moveToFourByteBoundry(rawInput);
		return res;
	}

	private byte[] readByteArray(final ByteBuffer rawInput, final int numBytes) {
		final byte[] res = new byte[numBytes];
		// XXX Crude copying from the buffer to the array. This can only be avoided if we change the return type to ByteBuffer.
		rawInput.get(res);
		return res;
	}

	/**
	 * Reads a binary blob from the byte stream.
	 * @return the next blob in the byte stream
	 */
	private byte[] readBlob(final ByteBuffer rawInput) {
		final int blobLen = readInteger(rawInput);
		final byte[] res = readByteArray(rawInput, blobLen);
		moveToFourByteBoundry(rawInput);
		return res;
	}

	/**
	 * Reads the types of the arguments from the byte stream.
	 * @return a char array with the types of the arguments,
	 *   or <code>null</code>, in case of no arguments
	 */
	private CharSequence readTypes(final ByteBuffer rawInput) {
		final String typesStr;

		// The next byte should be a ',', but some legacy code may omit it
		// in case of no arguments, refering to "OSC Messages" in:
		// http://opensoundcontrol.org/spec-1_0
		if (!rawInput.hasRemaining()) {
			typesStr = NO_ARGUMENT_TYPES;
		} else if (rawInput.get(rawInput.position()) == ',') {
			rawInput.get(); // position++
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
	private Object readArgument(final ByteBuffer rawInput, final char type) {
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
	private Character readChar(final ByteBuffer rawInput) {
		return (char) rawInput.get();
	}

	private BigInteger readBigInteger(final ByteBuffer rawInput, final int numBytes) {
		final byte[] myBytes = readByteArray(rawInput, numBytes);
		return new BigInteger(myBytes);
	}

	/**
	 * Reads a double from the byte stream.
	 * @return a 64bit precision floating point value
	 */
	private Object readDouble(final ByteBuffer rawInput) {
		final BigInteger doubleBits = readBigInteger(rawInput, 8);
		return Double.longBitsToDouble(doubleBits.longValue());
	}

	/**
	 * Reads a float from the byte stream.
	 * @return a 32bit precision floating point value
	 */
	private Float readFloat(final ByteBuffer rawInput) {
		final BigInteger floatBits = readBigInteger(rawInput, 4);
		return Float.intBitsToFloat(floatBits.intValue());
	}

	/**
	 * Reads a double precision integer (64 bit integer) from the byte stream.
	 * @return double precision integer (64 bit)
	 */
	private Long readLong(final ByteBuffer rawInput) {
		final BigInteger longintBytes = readBigInteger(rawInput, 8);
		return longintBytes.longValue();
	}

	/**
	 * Reads an Integer (32 bit integer) from the byte stream.
	 * @return an {@link Integer}
	 */
	private Integer readInteger(final ByteBuffer rawInput) {
		final BigInteger intBits = readBigInteger(rawInput, 4);
		return intBits.intValue();
	}

	/**
	 * Reads an unsigned integer (32 bit) from the byte stream.
	 * This code is copied from {@see http://darksleep.com/player/JavaAndUnsignedTypes.html},
	 * which is licensed under the Public Domain.
	 * @return single precision, unsigned integer (32 bit) wrapped in a 64 bit integer (long)
	 */
	private OSCUnsigned readUnsignedInteger(final ByteBuffer rawInput) {
		return OSCUnsigned.valueOf(new byte[] {
				rawInput.get(),
				rawInput.get(),
				rawInput.get(),
				rawInput.get()});
	}

	/**
	 * Reads the time tag and convert it to an OSCTimeStamp, which can be converted to
	 * a Java Date object.
	 * A timestamp is a 64 bit number representing the time in NTP format.
	 * The first 32 bits are seconds since 1900, the second 32 bits are
	 * fractions of a second.
	 * @return
	 */
	private OSCTimeStamp readTimeTag(final ByteBuffer rawInput) {
		final long ntpTime = readLong(rawInput);
		return OSCTimeStamp.valueOf(ntpTime);
	}

	/**
	 * Reads an array from the byte stream.
	 * @param types
	 * @param pos at which position to start reading
	 * @return the array that was read
	 */
	private List<Object> readArray(final ByteBuffer rawInput, final CharSequence types, final int pos) {
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
	private int lengthOfCurrentString(final ByteBuffer rawInput) {
		int len = 0;
		while (rawInput.get(rawInput.position() + len) != 0) {
			len++;
		}
		return len;
	}

	/**
	 * If not yet aligned, move to the next byte with an index in the byte array
	 * which is dividable by four.
	 */
	private void moveToFourByteBoundry(final ByteBuffer rawInput) {
		final int mod = rawInput.position() % 4;
		final int padding = (4 - mod) % 4;
		rawInput.position(rawInput.position() + padding);
	}
}
