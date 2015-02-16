/*
 * Copyright (C) 2003-2014, C. Ramakrishnan / Illposed Software.
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
import com.illposed.osc.argument.handler.StringArgumentHandler;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A helper class that translates OSC packets from Java to their byte stream representations,
 * as specified in the OSC spec.
 *
 * The implementation is based on
 * <a href="http://www.emergent.de">Markus Gaelli</a> and
 * Iannis Zannos's OSC implementation in Squeak (a Smalltalk dialect).
 */
public class OSCSerializer {

	private final SizeTrackingOutputStream stream;

	private final Map<Class, Boolean> classToMarker;
	private final Map<Class, ArgumentHandler> classToType;
	private final Map<Object, ArgumentHandler> markerValueToType;
	private final StringArgumentHandler stringOSCType;

	public OSCSerializer(final List<ArgumentHandler> types, final OutputStream wrappedStream) {

		final Map<Class, Boolean> classToMarkerTmp = new HashMap<Class, Boolean>(types.size());
		final Map<Class, ArgumentHandler> classToTypeTmp = new HashMap<Class, ArgumentHandler>();
		final Map<Object, ArgumentHandler> markerValueToTypeTmp = new HashMap<Object, ArgumentHandler>();
		for (final ArgumentHandler type : types) {
			final Class typeJava = type.getJavaClass();
			final Boolean registeredIsMarker = classToMarkerTmp.get(typeJava);
			if ((registeredIsMarker != null) && (registeredIsMarker != type.isMarkerOnly())) {
				throw new IllegalStateException(ArgumentHandler.class.getSimpleName()
						+ " implementations disagree on the marker nature of their class: "
						+ typeJava);
			}
			classToMarkerTmp.put(typeJava, type.isMarkerOnly());

			if (type.isMarkerOnly()) {
				try {
					final Object markerValue = type.parse(null);
					final ArgumentHandler previousType = markerValueToTypeTmp.get(markerValue);
					if (previousType != null) {
						throw new IllegalStateException("Marker value \"" + markerValue
								+ "\" is already used for type "
								+ previousType.getClass().getCanonicalName());
					}
					markerValueToTypeTmp.put(markerValue, type);
				} catch (final OSCParseException ex) {
					throw new IllegalStateException("Developper error; this should never happen",
							ex);
				}
			} else {
				final ArgumentHandler previousType = classToTypeTmp.get(typeJava);
				if (previousType != null) {
					throw new IllegalStateException("Java argument type "
							+ typeJava.getCanonicalName() + " is already used for type "
							+ previousType.getClass().getCanonicalName());
				}
				classToTypeTmp.put(typeJava, type);
			}
		}

		this.classToMarker = Collections.unmodifiableMap(classToMarkerTmp);
		this.classToType = Collections.unmodifiableMap(classToTypeTmp);
		this.markerValueToType = Collections.unmodifiableMap(markerValueToTypeTmp);
		this.stream = new SizeTrackingOutputStream(wrappedStream);
		this.stringOSCType = (StringArgumentHandler) classToType.get(String.class);
	}

	public Map<Class, ArgumentHandler> getClassToTypeMapping() {
		return classToType;
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
		final OSCSerializer packetStream = OSCSerializerFactory.createDefaultFactory().create(buffer); // HACK this should not use the default one, but the one that created us! but in the end, this part should be externalized anyway, as it is not part of the core serialization, but rather a requirement for the TCP transport convention (see OSC spec. 1.1)
//		packetStream.setCharset(getCharset()); // see HACK of the previous line
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
		stringOSCType.serialize(stream, message.getAddress());
	}

	/**
	 * Convert the arguments into a byte array.
	 * Used internally only.
	 * @param stream where to write the arguments to
	 */
	private void writeArgumentsByteArray(final OSCMessage message) throws IOException {
		stream.write(OSCParser.TYPES_VALUES_SEPARATOR);
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
	 * Convert the time-tag into the OSC byte stream.
	 * Used Internally.
	 * @param stream where to write the time-tag to
	 * @deprecated use {@link #write(OSCTimeStamp)} instead
	 */
	private void writeBundleTimestamp(final OSCTimeStamp timestamp) throws IOException {
		write(timestamp == null ? OSCTimeStamp.IMMEDIATE : timestamp);
	}

	private ArgumentHandler findType(final Object argumentValue) {

		final ArgumentHandler type;
		final Class argumentClass = extractTypeClass(argumentValue);
		final Boolean isMarkerType = classToMarker.get(argumentClass);
		if (isMarkerType) {
			type = markerValueToType.get(argumentValue);
		} else {
			type = classToType.get(argumentClass);
		}

		return type;
	}

	/**
	 * Write an object into the byte stream.
	 * @param anObject (usually) one of Float, Double, String, Character, Integer, Long,
	 *   or array of these.
	 */
	void write(final Object anObject) throws IOException {

		if (anObject instanceof Collection) {
			@SuppressWarnings("unchecked") final Collection<Object> theArray = (Collection<Object>) anObject;
			for (final Object entry : theArray) {
				write(entry);
			}
		} else {
			final ArgumentHandler type = findType(anObject);
			if (type == null) {
				throw new UnsupportedOperationException(
						"Do not know how to write an object of class: " + anObject.getClass());
			}
			type.serialize(stream, anObject);
		}
	}

	private static Class extractTypeClass(final Object value) {
		return (value == null) ? Object.class : value.getClass();
	}

	/**
	 * Write the OSC specification type tag for the type a certain Java type
	 * converts to.
	 * @param typeClass Class of a Java object in the arguments
	 */
	private void writeType(final Object value) throws IOException {

		final ArgumentHandler type = findType(value);
		stream.write(type.getDefaultIdentifier());
	}

	/**
	 * Write the types for an array element in the arguments.
	 * @param arguments array of base Objects
	 * @throws IOException if the underlying stream produces an exception when written to
	 */
	private void writeTypesArray(final Collection<Object> arguments) throws IOException {

		for (final Object argument : arguments) {
			if (argument instanceof Collection) {
				// If the array at i is a type of array, write a '['.
				// This is used for nested arguments.
				stream.write('[');
				// fill the [] with the SuperCollider types corresponding to
				// the object (e.g., Object of type String needs -s).
				@SuppressWarnings("unchecked") Collection<Object> collArg = (Collection<Object>) argument;
				writeTypesArray(collArg);
				// close the array
				stream.write(']');
			} else {
				// go through the array and write the superCollider types as shown
				// in the above method.
				// The classes derived here are used as the arg to the above method.
				writeType(argument);
			}
		}
	}

	/**
	 * Write types for the arguments.
	 * @param arguments  the arguments to an OSCMessage
	 * @throws IOException if the underlying stream produces an exception when written to
	 */
	public void writeTypes(final Collection<Object> arguments) throws IOException {

		writeTypesArray(arguments);
		// we always need to terminate with a zero,
		// even if (especially when) the stream is already aligned.
		stream.write((byte) 0);
		// align the stream with padded bytes
		alignStream();
	}
}
