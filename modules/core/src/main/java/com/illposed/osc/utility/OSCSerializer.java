/*
 * Copyright (C) 2003-2014, C. Ramakrishnan / Illposed Software.
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Date;

/**
 * A helper class that translates from Java types to their byte stream representations
 * according to the OSC spec.
 *
 * The implementation is based on
 * <a href="http://www.emergent.de">Markus Gaelli</a> and
 * Iannis Zannos's OSC implementation in Squeak (a Smalltalk dialect).
 */
public class OSCSerializer {

	private final SizeTrackingOutputStream stream;
	/** Used to encode message addresses and string parameters. */
	private Charset charset;
	/**
	 * A Buffer used to convert an Integer into bytes.
	 * We allocate it globally, so the GC has less work to do.
	 */
	private final byte[] intBytes;
	/**
	 * A Buffer used to convert a Long into bytes.
	 * We allocate it globally, so the GC has less work to do.
	 */
	private final byte[] longintBytes;

	public OSCSerializer(final OutputStream wrappedStream) {

		this.stream = new SizeTrackingOutputStream(wrappedStream);
		this.charset = Charset.defaultCharset();
		this.intBytes = new byte[4];
		this.longintBytes = new byte[8];
	}

	/**
	 * Returns the character set used to encode message addresses
	 * and string parameters.
	 * @return the character-encoding-set used by this converter
	 */
	public Charset getCharset() {
		return charset;
	}

	/**
	 * Sets the character set used to encode message addresses
	 * and string parameters.
	 * @param charset the desired character-encoding-set to be used by this converter
	 */
	public void setCharset(final Charset charset) {
		this.charset = charset;
	}

	/**
	 * Align the stream by padding it with '0's so it has a size divisible by 4.
	 */
	private void alignStream() throws IOException {
		final int alignmentOverlap = stream.size() % 4;
		final int padLen = (4 - alignmentOverlap) % 4;
		for (int pci = 0; pci < padLen; pci++) {
			stream.write(0);
		}
	}

	private byte[] convertToByteArray(final OSCPacket packet) throws IOException {

		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final OSCSerializer packetStream = new OSCSerializer(buffer);
		packetStream.setCharset(getCharset());
		if (packet instanceof OSCBundle) {
			packetStream.write((OSCBundle) packet);
		} else if (packet instanceof OSCMessage) {
			packetStream.write((OSCMessage) packet);
		} else {
			throw new UnsupportedOperationException("We do not support writing packets of type: "
					+ packet.getClass());
		}
		return buffer.toByteArray();
	}

	private void write(final OSCBundle bundle) throws IOException {
		write("#bundle");
		writeBundleTimestamp(bundle.getTimestamp());
		for (final OSCPacket pkg : bundle.getPackets()) {
			writeInternal(pkg);
		}
	}

	/**
	 * Convert the address into a byte array.
	 * Used internally only.
	 * @param stream where to write the address to
	 */
	private void writeAddressByteArray(final OSCMessage message) throws IOException {
		write(message.getAddress());
	}

	/**
	 * Convert the arguments into a byte array.
	 * Used internally only.
	 * @param stream where to write the arguments to
	 */
	private void writeArgumentsByteArray(final OSCMessage message) throws IOException {
		write(',');
		writeTypes(message.getArguments());
		for (final Object argument : message.getArguments()) {
			write(argument);
		}
	}

	private void write(final OSCMessage message) throws IOException {
		writeAddressByteArray(message);
		writeArgumentsByteArray(message);
	}

	private void writeInternal(final OSCPacket packet) throws IOException {

		// HACK NOTE We have to do it in this ugly way,
		//   because we have to know the packets size in bytes
		//   and write it to the stream,
		//   before we can write the packets content to the stream.
		final byte[] packetBytes = convertToByteArray(packet);
		write(packetBytes); // this first writes the #bytes, before the actual bytes
	}

	public void write(final OSCPacket packet) throws IOException {

		stream.reset();
		if (packet instanceof OSCBundle) {
			write((OSCBundle) packet);
		} else if (packet instanceof OSCMessage) {
			write((OSCMessage) packet);
		} else {
			throw new UnsupportedOperationException("We do not support writing packets of type: "
					+ packet.getClass());
		}
	}

	/**
	 * Write bytes into the byte stream.
	 * @param bytes  bytes to be written
	 */
	void write(final byte... bytes) throws IOException {
		writeInteger32ToByteArray(bytes.length);
		stream.write(bytes);
		alignStream();
	}

	/**
	 * Write an integer into the byte stream.
	 * @param anInt the integer to be written
	 */
	void write(final int anInt) throws IOException {
		writeInteger32ToByteArray(anInt);
	}

	/**
	 * Write a float into the byte stream.
	 * @param aFloat floating point number to be written
	 */
	void write(final Float aFloat) throws IOException {
		writeInteger32ToByteArray(Float.floatToIntBits(aFloat));
	}

	/**
	 * Write a double into the byte stream (8 bytes).
	 * @param aDouble double precision floating point number to be written
	 */
	void write(final Double aDouble) throws IOException {
		writeInteger64ToByteArray(Double.doubleToRawLongBits(aDouble));
	}

	/**
	 * @param anInt the integer to be written
	 */
	void write(final Integer anInt) throws IOException {
		writeInteger32ToByteArray(anInt);
	}

	/**
	 * @param aLong the double precision integer to be written
	 */
	void write(final Long aLong) throws IOException {
		writeInteger64ToByteArray(aLong);
	}

	/**
	 * @param timestamp the timestamp to be written
	 */
	void write(final Date timestamp) throws IOException {
		write(OSCTimeStamp.valueOf(timestamp));
	}

	/**
	 * @param timeStamp the timestamp to be written
	 */
	void write(final OSCTimeStamp timeStamp) throws IOException {
		writeInteger64ToByteArray(timeStamp.getNtpTime());
	}

	/**
	 * Convert the time-tag into the OSC byte stream.
	 * Used Internally.
	 * @param stream where to write the time-tag to
	 * @deprecated use {@link #write(OSCTimeStamp)} instead
	 */
	private void writeBundleTimestamp(final OSCTimeStamp timestamp) throws IOException {
		write(timestamp == null ? OSCTimeStamp.IMMEDIATE : timestamp);
	}

	/**
	 * Write a string into the byte stream.
	 * @param aString the string to be written
	 */
	void write(final String aString) throws IOException {
		final byte[] stringBytes = aString.getBytes(charset);
		stream.write(stringBytes);
		stream.write(0);
		alignStream();
	}

	/**
	 * Write a char into the byte stream, and ensure it is 4 byte aligned again.
	 * @param aChar the character to be written
	 */
	void write(final Character aChar) throws IOException {
		stream.write((char) aChar);
		alignStream();
	}

	/**
	 * Write a char into the byte stream.
	 * CAUTION, this does not ensure 4 byte alignment (it actually breaks it)!
	 * @param aChar the character to be written
	 */
	void write(final char aChar) throws IOException {
		stream.write(aChar);
	}

	/**
	 * Checks whether the given object is represented by a type that comes without data.
	 * @param anObject the object to inspect
	 * @return whether the object to check consists of only its type information
	 */
	private boolean isNoDataObject(final Object anObject) {
		return ((anObject instanceof OSCImpulse)
				|| (anObject instanceof Boolean)
				|| (anObject == null));
	}

	/**
	 * Write an object into the byte stream.
	 * @param anObject (usually) one of Float, Double, String, Character, Integer, Long,
	 *   or array of these.
	 */
	void write(final Object anObject) throws IOException {
		// Can't do switch on class
		if (anObject instanceof Collection) {
			@SuppressWarnings("unchecked") final Collection<Object> theArray = (Collection<Object>) anObject;
			for (final Object entry : theArray) {
				write(entry);
			}
		} else if (anObject instanceof Float) {
			write((Float) anObject);
		} else if (anObject instanceof Double) {
			write((Double) anObject);
		} else if (anObject instanceof String) {
			write((String) anObject);
		} else if (anObject instanceof byte[]) {
			write((byte[]) anObject);
		} else if (anObject instanceof Character) {
			write((Character) anObject);
		} else if (anObject instanceof Integer) {
			write((Integer) anObject);
		} else if (anObject instanceof Long) {
			write((Long) anObject);
		} else if (anObject instanceof OSCTimeStamp) {
			write((OSCTimeStamp) anObject);
		} else if (anObject instanceof Date) {
			write((Date) anObject);
		} else if (!isNoDataObject(anObject)) {
			throw new UnsupportedOperationException("Do not know how to write an object of class: "
					+ anObject.getClass());
		}
	}

	/**
	 * Write the OSC specification type tag for the type a certain Java type
	 * converts to.
	 * @param typeClass Class of a Java object in the arguments
	 */
	private void writeType(final Class typeClass) throws IOException {

		// A big ol' else-if chain -- what's polymorphism mean, again?
		// I really wish I could extend the base classes!
		if (Integer.class.equals(typeClass)) {
			stream.write('i');
		} else if (Long.class.equals(typeClass)) {
			stream.write('h');
		} else if (Date.class.equals(typeClass) || OSCTimeStamp.class.equals(typeClass)) {
			stream.write('t');
		} else if (Float.class.equals(typeClass)) {
			stream.write('f');
		} else if (Double.class.equals(typeClass)) {
			stream.write('d');
		} else if (String.class.equals(typeClass)) {
			stream.write('s');
		} else if (byte[].class.equals(typeClass)) {
			stream.write('b');
		} else if (Character.class.equals(typeClass)) {
			stream.write('c');
		} else if (OSCImpulse.class.equals(typeClass)) {
			stream.write('I');
		} else {
			throw new UnsupportedOperationException("Do not know the OSC type for the java class: "
					+ typeClass);
		}
	}

	/**
	 * Write the types for an array element in the arguments.
	 * @param arguments array of base Objects
	 */
	private void writeTypesArray(final Collection<Object> arguments) throws IOException {

		for (final Object argument : arguments) {
			if (null == argument) {
				stream.write('N');
			} else if (argument instanceof Collection) {
				// If the array at i is a type of array, write a '['.
				// This is used for nested arguments.
				stream.write('[');
				// fill the [] with the SuperCollider types corresponding to
				// the object (e.g., Object of type String needs -s).
				@SuppressWarnings("unchecked") Collection<Object> collArg = (Collection<Object>) argument;
				writeTypesArray(collArg);
				// close the array
				stream.write(']');
			} else if (Boolean.TRUE.equals(argument)) {
				stream.write('T');
			} else if (Boolean.FALSE.equals(argument)) {
				stream.write('F');
			} else {
				// go through the array and write the superCollider types as shown
				// in the above method.
				// The classes derived here are used as the arg to the above method.
				writeType(argument.getClass());
			}
		}
	}

	/**
	 * Write types for the arguments.
	 * @param arguments  the arguments to an OSCMessage
	 */
	public void writeTypes(final Collection<Object> arguments) throws IOException {

		writeTypesArray(arguments);
		// we always need to terminate with a zero,
		// even if (especially when) the stream is already aligned.
		stream.write((byte) 0);
		// align the stream with padded bytes
		alignStream();
	}

	/**
	 * Write a 32 bit integer to the byte array without allocating memory.
	 * @param value a 32 bit integer.
	 */
	private void writeInteger32ToByteArray(final int value) throws IOException {

		int curValue = value;
		intBytes[3] = (byte)curValue; curValue >>>= 8;
		intBytes[2] = (byte)curValue; curValue >>>= 8;
		intBytes[1] = (byte)curValue; curValue >>>= 8;
		intBytes[0] = (byte)curValue;

		stream.write(intBytes);
	}

	/**
	 * Write a 64 bit integer to the byte array without allocating memory.
	 * @param value a 64 bit integer.
	 */
	private void writeInteger64ToByteArray(final long value) throws IOException {

		long curValue = value;
		longintBytes[7] = (byte)curValue; curValue >>>= 8;
		longintBytes[6] = (byte)curValue; curValue >>>= 8;
		longintBytes[5] = (byte)curValue; curValue >>>= 8;
		longintBytes[4] = (byte)curValue; curValue >>>= 8;
		longintBytes[3] = (byte)curValue; curValue >>>= 8;
		longintBytes[2] = (byte)curValue; curValue >>>= 8;
		longintBytes[1] = (byte)curValue; curValue >>>= 8;
		longintBytes[0] = (byte)curValue;

		stream.write(longintBytes);
	}
}
