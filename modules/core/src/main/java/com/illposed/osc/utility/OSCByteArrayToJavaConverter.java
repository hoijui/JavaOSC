/*
 * Copyright (C) 2004-2006, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.utility;

import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCImpulse;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPacket;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Utility class to convert a byte array,
 * conforming to the OSC byte stream format,
 * into Java objects.
 *
 * @author Chandrasekhar Ramakrishnan
 */
public class OSCByteArrayToJavaConverter {

	private static final String BUNDLE_START = "#bundle";
	private static final char BUNDLE_IDENTIFIER = BUNDLE_START.charAt(0);

	private byte[] bytes;
	/** Used to decode message addresses and string parameters. */
	private Charset charset;
	private int bytesLength;
	private int streamPosition;

	/**
	 * Creates a helper object for converting from a byte array
	 * to an {@link OSCPacket} object.
	 */
	public OSCByteArrayToJavaConverter() {

		this.charset = Charset.defaultCharset();
	}

	/**
	 * Returns the character set used to decode message addresses
	 * and string parameters.
	 */
	public Charset getCharset() {
		return charset;
	}

	/**
	 * Sets the character set used to decode message addresses
	 * and string parameters.
	 */
	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	/**
	 * Converts a byte array into an {@link OSCPacket}
	 * (either an {@link OSCMessage} or {@link OSCBundle}).
	 */
	public OSCPacket convert(byte[] byteArray, int bytesLength) {
		this.bytes = byteArray;
		this.bytesLength = bytesLength;
		this.streamPosition = 0;
		if (isBundle()) {
			return convertBundle();
		} else {
			return convertMessage();
		}
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
	private boolean isBundle() {
		// The shortest valid packet may be no shorter then 4 bytes,
		// thus we may assume to always have a byte at index 0.
		return bytes[0] == BUNDLE_IDENTIFIER;
	}

	/**
	 * Converts the byte array to a bundle.
	 * Assumes that the byte array is a bundle.
	 * @return a bundle containing the data specified in the byte stream
	 */
	private OSCBundle convertBundle() {
		// skip the "#bundle " stuff
		streamPosition = BUNDLE_START.length() + 1;
		Date timestamp = readTimeTag();
		OSCBundle bundle = new OSCBundle(timestamp);
		OSCByteArrayToJavaConverter myConverter
				= new OSCByteArrayToJavaConverter();
		myConverter.setCharset(charset);
		while (streamPosition < bytesLength) {
			// recursively read through the stream and convert packets you find
			final int packetLength = readInteger();
			final byte[] packetBytes = new byte[packetLength];
			System.arraycopy(bytes, streamPosition, packetBytes, 0, packetLength);
			streamPosition += packetLength;
			OSCPacket packet = myConverter.convert(packetBytes, packetLength);
			bundle.addPacket(packet);
		}
		return bundle;
	}

	/**
	 * Converts the byte array to a simple message.
	 * Assumes that the byte array is a message.
	 * @return a message containing the data specified in the byte stream
	 */
	private OSCMessage convertMessage() {
		OSCMessage message = new OSCMessage();
		message.setAddress(readString());
		List<Character> types = readTypes();
		if (null == types) {
			// we are done
			return message;
		}
		moveToFourByteBoundry();
		for (int i = 0; i < types.size(); ++i) {
			if ('[' == types.get(i).charValue()) {
				// we're looking at an array -- read it in
				message.addArgument(readArray(types, ++i));
				// then increment i to the end of the array
				while (types.get(i).charValue() != ']') {
					i++;
				}
			} else {
				message.addArgument(readArgument(types.get(i)));
			}
		}
		return message;
	}

	/**
	 * Reads a string from the byte stream.
	 * @return the next string in the byte stream
	 */
	private String readString() {
		int strLen = lengthOfCurrentString();
		String res = new String(bytes, streamPosition, strLen, charset);
		streamPosition += strLen;
		moveToFourByteBoundry();
		return res;
	}

	/**
	 * Reads a binary blob from the byte stream.
	 * @return the next blob in the byte stream
	 */
	private byte[] readBlob() {
		final int blobLen = readInteger();
		final byte[] res = new byte[blobLen];
		System.arraycopy(bytes, streamPosition, res, 0, blobLen);
		streamPosition += blobLen;
		moveToFourByteBoundry();
		return res;
	}

	/**
	 * Reads the types of the arguments from the byte stream.
	 * @return a char array with the types of the arguments,
	 *   or <code>null</code>, in case of no arguments
	 */
	private List<Character> readTypes() {
		// The next byte should be a ',', but some legacy code may omit it
		// in case of no arguments, refering to "OSC Messages" in:
		// http://opensoundcontrol.org/spec-1_0
		if (bytes.length <= streamPosition) {
			return null; // no arguments
		}
		if (bytes[streamPosition] != ',') {
			// XXX should we not rather fail-fast -> throw exception?
			return null;
		}
		streamPosition++;
		// find out how long the list of types is
		int typesLen = lengthOfCurrentString();
		if (0 == typesLen) {
			return null; // no arguments
		}

		// read in the types
		List<Character> typesChars = new ArrayList<Character>(typesLen);
		for (int i = 0; i < typesLen; i++) {
			typesChars.add((char) bytes[streamPosition++]);
		}
		return typesChars;
	}

	/**
	 * Reads an object of the type specified by the type char.
	 * @param type type of the argument to read
	 * @return a Java representation of the argument
	 */
	private Object readArgument(char type) {
		switch (type) {
			case 'u' :
				return readUnsignedInteger();
			case 'i' :
				return readInteger();
			case 'h' :
				return readLong();
			case 'f' :
				return readFloat();
			case 'd' :
				return readDouble();
			case 's' :
				return readString();
			case 'b' :
				return readBlob();
			case 'c' :
				return readChar();
			case 'N' :
				return null;
			case 'T' :
				return Boolean.TRUE;
			case 'F' :
				return Boolean.FALSE;
			case 'I' :
				return OSCImpulse.INSTANCE;
			case 't' :
				return readTimeTag();
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
	private Character readChar() {
		return (char) bytes[streamPosition++];
	}

	private BigInteger readBigInteger(final int numBytes) {
		final byte[] myBytes = new byte[numBytes];
		System.arraycopy(bytes, streamPosition, myBytes, 0, numBytes);
		streamPosition += numBytes;
		return  new BigInteger(myBytes);
	}

	/**
	 * Reads a double from the byte stream.
	 * @return a 64bit precision floating point value
	 */
	private Object readDouble() {
		final BigInteger doubleBits = readBigInteger(8);
		return Double.longBitsToDouble(doubleBits.longValue());
	}

	/**
	 * Reads a float from the byte stream.
	 * @return a 32bit precision floating point value
	 */
	private Float readFloat() {
		final BigInteger floatBits = readBigInteger(4);
		return Float.intBitsToFloat(floatBits.intValue());
	}

	/**
	 * Reads a double precision integer (64 bit integer) from the byte stream.
	 * @return double precision integer (64 bit)
	 */
	private Long readLong() {
		final BigInteger longintBytes = readBigInteger(8);
		return longintBytes.longValue();
	}

	/**
	 * Reads an Integer (32 bit integer) from the byte stream.
	 * @return an {@link Integer}
	 */
	private Integer readInteger() {
		final BigInteger intBits = readBigInteger(4);
		return intBits.intValue();
	}

	/**
	 * Reads an unsigned integer (32 bit) from the byte stream.
	 * This code is copied from {@see http://darksleep.com/player/JavaAndUnsignedTypes.html},
	 * which is licensed under the Public Domain.
	 * @return single precision, unsigned integer (32 bit) wrapped in a 64 bit integer (long)
	 */
	private Long readUnsignedInteger() {

		int firstByte = (0x000000FF & ((int) bytes[streamPosition++]));
		int secondByte = (0x000000FF & ((int) bytes[streamPosition++]));
		int thirdByte = (0x000000FF & ((int) bytes[streamPosition++]));
		int fourthByte = (0x000000FF & ((int) bytes[streamPosition++]));
		return ((long) (firstByte << 24
				| secondByte << 16
				| thirdByte << 8
				| fourthByte))
				& 0xFFFFFFFFL;
	}

	/**
	 * Reads the time tag and convert it to a Java Date object.
	 * A timestamp is a 64 bit number representing the time in NTP format.
	 * The first 32 bits are seconds since 1900, the second 32 bits are
	 * fractions of a second.
	 * @return a {@link Date}
	 */
	private Date readTimeTag() {
		byte[] secondBytes = new byte[8];
		byte[] fractionBytes = new byte[8];
		for (int i = 0; i < 4; i++) {
			// clear the higher order 4 bytes
			secondBytes[i] = 0;
			fractionBytes[i] = 0;
		}
		// while reading in the seconds & fraction, check if
		// this timetag has immediate semantics
		boolean isImmediate = true;
		for (int i = 4; i < 8; i++) {
			secondBytes[i] = bytes[streamPosition++];
			if (secondBytes[i] > 0) {
				isImmediate = false;
			}
		}
		for (int i = 4; i < 8; i++) {
			fractionBytes[i] = bytes[streamPosition++];
			if (i < 7) {
				if (fractionBytes[i] > 0) {
					isImmediate = false;
				}
			} else {
				if (fractionBytes[i] > 1) {
					isImmediate = false;
				}
			}
		}

		if (isImmediate) {
			return OSCBundle.TIMESTAMP_IMMEDIATE;
		}

		final long secsSince1900 = new BigInteger(secondBytes).longValue();
		long secsSince1970 = secsSince1900 - OSCBundle.SECONDS_FROM_1900_TO_1970;

		// no point maintaining times in the distant past
		if (secsSince1970 < 0) {
			secsSince1970 = 0;
		}
		long fraction = (new BigInteger(fractionBytes).longValue());

		// this line was cribbed from jakarta commons-net's NTP TimeStamp code
		fraction = (fraction * 1000) / 0x100000000L;

		// I do not know where, but I'm losing 1ms somewhere...
		fraction = (fraction > 0) ? fraction + 1 : 0;
		long millisecs = (secsSince1970 * 1000) + fraction;
		return new Date(millisecs);
	}

	/**
	 * Reads an array from the byte stream.
	 * @param types
	 * @param pos at which position to start reading
	 * @return the array that was read
	 */
	private List<Object> readArray(List<Character> types, int pos) {
		int arrayLen = 0;
		while (types.get(pos + arrayLen).charValue() != ']') {
			arrayLen++;
		}
		List<Object> array = new ArrayList<Object>(arrayLen);
		for (int j = 0; j < arrayLen; j++) {
			array.add(readArgument(types.get(pos + j)));
		}
		return array;
	}

	/**
	 * Get the length of the string currently in the byte stream.
	 */
	private int lengthOfCurrentString() {
		int len = 0;
		while (bytes[streamPosition + len] != 0) {
			len++;
		}
		return len;
	}

	/**
	 * Move to the next byte with an index in the byte array
	 * which is dividable by four.
	 */
	private void moveToFourByteBoundry() {
		// If i am already at a 4 byte boundry, I need to move to the next one
		int mod = streamPosition % 4;
		streamPosition += (4 - mod);
	}
}
