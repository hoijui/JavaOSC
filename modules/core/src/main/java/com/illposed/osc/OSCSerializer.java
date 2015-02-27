/*
 * Copyright (C) 2003-2014, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import com.illposed.osc.argument.ArgumentHandler;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts OSC packet Java objects to their byte stream representations,
 * conforming to the OSC specification.
 * This class is NOT thread-save, and will produce invalid results and errors
 * if used by multiple threads simultaneously.
 * Please use a separate instance per thread.
 *
 * The implementation was originally based on
 * <a href="http://www.emergent.de">Markus Gaelli</a> and
 * Iannis Zannos's OSC implementation in Squeak (a Smalltalk dialect).
 */
public class OSCSerializer {

	/**
	 * Our stack (LIFO) of streams.
	 * The last entry is always used as the current stream to write to.
	 */
	private final Deque<SizeTrackingOutputStream> streams;
	private final Map<Class, Boolean> classToMarker;
	private final Map<Class, ArgumentHandler> classToType;
	private final Map<Object, ArgumentHandler> markerValueToType;

	public OSCSerializer(final List<ArgumentHandler> types, final OutputStream mainStream) {

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

		// usually, we should not need a stack size of 16, which is the default initial size
		this.streams = new ArrayDeque<SizeTrackingOutputStream>(4);
		this.classToMarker = Collections.unmodifiableMap(classToMarkerTmp);
		this.classToType = Collections.unmodifiableMap(classToTypeTmp);
		this.markerValueToType = Collections.unmodifiableMap(markerValueToTypeTmp);
		pushStream(mainStream);
	}

	public Map<Class, ArgumentHandler> getClassToTypeMapping() {
		return classToType;
	}

	/**
	 * Adds a stream on top of the stack (LIFO), and thus sets it as the one that will be used
	 * for writing from now on, until it is popped or a new one is added.
	 * @param stream the stream to be added on top of the stack (LIFO) of streams
	 *   for this serializer.
	 */
	private void pushStream(final OutputStream stream) {

		// wrap the stream, if necessary
		final SizeTrackingOutputStream sizeTrackingStream;
		if (stream instanceof SizeTrackingOutputStream) {
			sizeTrackingStream = (SizeTrackingOutputStream) stream;
		} else {
			sizeTrackingStream = new SizeTrackingOutputStream(stream);
		}

		// push it on the stack
		streams.addLast(sizeTrackingStream);
	}

	/**
	 * Removes the stream currently in use for writing.
	 * @return the removed stream, which is the last entry on the stack (LIFO) of streams
	 *   for this serializer.
	 */
	private SizeTrackingOutputStream popStream() {
		return streams.removeLast();
	}

	/**
	 * Returns the stream currently in use for writing.
	 * @return the last entry on the stack (LIFO) of streams for this serializer.
	 */
	private SizeTrackingOutputStream getStream() {
		return streams.peekLast();
	}

	/**
	 * Terminates the previously written piece of data with a single {@code (byte) '0'}.
	 * We always need to terminate with a zero, especially when the stream is already aligned.
	 * @param stream to receive the data-piece termination
	 * @throws IOException if there is a problem writing the termination zero
	 */
	public static void terminate(final SizeTrackingOutputStream stream) throws IOException {
		stream.write(0); // this int is interpreted as and written as a single byte
	}

	/**
	 * Align a stream by padding it with {@code (byte) '0'}s so it has a size divisible by 4.
	 * @param stream to be aligned
	 * @throws IOException if there is a problem writing the padding zeros
	 */
	public static void align(final SizeTrackingOutputStream stream) throws IOException {
		final int alignmentOverlap = stream.size() % 4;
		final int padLen = (4 - alignmentOverlap) % 4;
		for (int pci = 0; pci < padLen; pci++) {
			stream.write(0); // this int is interpreted as and written as a single byte
		}
	}

	/**
	 * Terminates the previously written piece of data with a single {@code (byte) '0'},
	 * and then aligns the stream by padding it with {@code (byte) '0'}s so it has a size
	 * divisible by 4.
	 * We always need to terminate with a zero, especially when the stream is already aligned.
	 * @param stream to receive the data-piece termination and alignment
	 * @throws IOException if there is a problem writing the termination and padding zeros
	 */
	public static void terminateAndAlign(final SizeTrackingOutputStream stream) throws IOException {
		terminate(stream);
		align(stream);
	}

	/**
	 * Converts a packet into an OSC compliant byte array,
	 * leaving the current stream untouched.
	 * @param packet to be converted into an OSC compliant byte array
	 * @return the content of the supplied packet as OSC compliant byte array
	 * @throws IOException in case of not enough free memory for the buffer
	 * @throws OSCSerializeException if the packet failed to serialize
	 */
	private byte[] writeToBuffer(final OSCPacket packet)
			throws IOException, OSCSerializeException
	{
		// create the temporary buffer where our packet will be written to
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		pushStream(buffer);
		write(packet);
		popStream();

		return buffer.toByteArray();
	}

	private void write(final OSCBundle bundle) throws IOException, OSCSerializeException {
		write("#bundle");
		write(bundle.getTimestamp());
		for (final OSCPacket pkg : bundle.getPackets()) {
			writeSizeAndData(pkg);
		}
	}

	/**
	 * Serializes a messages address.
	 * @param message the address of this message will be serialized
	 * @throws IOException if there is a problem with the currently used stream
	 * @throws OSCSerializeException if the message failed to serialize
	 */
	private void writeAddress(final OSCMessage message) throws IOException, OSCSerializeException {

		final String address = message.getAddress();
		if (!OSCMessage.isValidAddress(address)) {
			throw new OSCSerializeException("Can not serialize a message with invalid address: \""
					+ address + "\"");
		}
		write(address);
	}

	/**
	 * Serializes the arguments of a message.
	 * @param message the arguments of this message will be serialized
	 * @throws IOException if there is a problem with the currently used stream
	 * @throws OSCSerializeException if the message arguments failed to serialize
	 */
	private void writeArguments(final OSCMessage message) throws IOException, OSCSerializeException
	{
		getStream().write(OSCParser.TYPES_VALUES_SEPARATOR);
		writeTypeTags(message.getArguments());
		for (final Object argument : message.getArguments()) {
			write(argument);
		}
	}

	private void write(final OSCMessage message) throws IOException, OSCSerializeException {
		writeAddress(message);
		writeArguments(message);
	}

	private void writeSizeAndData(final OSCPacket packet) throws IOException, OSCSerializeException {

		// HACK NOTE We have to do it in this ugly way,
		//   because we have to know the packets size in bytes
		//   and write it to the stream,
		//   before we can write the packets content to the stream.
		final byte[] packetBytes = writeToBuffer(packet);
		// this first writes the size (packetBytes.length),
		// followed by the actual data (packetBytes)
		write(packetBytes);
	}

	public void write(final OSCPacket packet) throws IOException, OSCSerializeException {

		getStream().reset();
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
	 * Write only the type tags for a given list of arguments.
	 * This is primarily used by the packet dispatcher.
	 * @param arguments to write the type tags from
	 * @throws IOException if the currently used stream produces an exception when written to
	 * @throws OSCSerializeException if the arguments failed to serialize
	 */
	public void writeOnlyTypeTags(final List<?> arguments) throws IOException, OSCSerializeException
	{
		getStream().reset();
		writeTypeTagsRaw(arguments);
	}

	private ArgumentHandler findType(final Object argumentValue) throws OSCSerializeException {

		final ArgumentHandler type;
		final Class argumentClass = extractTypeClass(argumentValue);
		final Boolean markerType = classToMarker.get(argumentClass);
		if (markerType == null) {
			throw new OSCSerializeException("No type handler registered for serializing class "
					+ argumentClass.getCanonicalName());
		} else if (markerType) {
			type = markerValueToType.get(argumentValue);
		} else {
			type = classToType.get(argumentClass);
		}

		return type;
	}

	/**
	 * Write an object into the byte stream.
	 * @param anObject (usually) one of Float, Double, String, Character, Integer, Long,
	 *   or a Collection of these.
	 *   See {@link #getClassToTypeMapping()} for a complete list of which classes may be used here.
	 * @throws IOException if there is a problem with the currently used stream
	 * @throws OSCSerializeException if the argument object failed to serialize
	 */
	private void write(final Object anObject) throws IOException, OSCSerializeException {

		if (anObject instanceof Collection) {
			@SuppressWarnings("unchecked") final Collection<Object> theArray = (Collection<Object>) anObject;
			for (final Object entry : theArray) {
				write(entry);
			}
		} else {
			final ArgumentHandler type = findType(anObject);
			type.serialize(getStream(), anObject);
		}
	}

	private static Class extractTypeClass(final Object value) {
		return (value == null) ? Object.class : value.getClass();
	}

	/**
	 * Write the OSC specification type tag for the type a certain Java type
	 * converts to.
	 * @param value of this argument, we need to write the type identifier
	 * @throws IOException if there is a problem with the currently used stream
	 * @throws OSCSerializeException if the value failed to serialize
	 */
	private void writeType(final Object value) throws IOException, OSCSerializeException {

		final ArgumentHandler type = findType(value);
		getStream().write(type.getDefaultIdentifier());
	}

	/**
	 * Write the type tags for a given list of arguments.
	 * @param arguments array of base Objects
	 * @throws IOException if the currently used stream produces an exception when written to
	 * @throws OSCSerializeException if the arguments failed to serialize
	 */
	private void writeTypeTagsRaw(final List<?> arguments) throws IOException, OSCSerializeException
	{
		for (final Object argument : arguments) {
			if (argument instanceof List) {
				final SizeTrackingOutputStream stream = getStream();
				// This is used for nested arguments.
				// open the array
				stream.write(OSCParser.TYPE_ARRAY_BEGIN);
				// fill the [] with the nested argument types
				@SuppressWarnings("unchecked") List<Object> collArg = (List<Object>) argument;
				writeTypeTagsRaw(collArg);
				// close the array
				stream.write(OSCParser.TYPE_ARRAY_END);
			} else {
				// write a single, simple arguments type
				writeType(argument);
			}
		}
	}

	/**
	 * Write the type tags for a given list of arguments, and cleanup the stream.
	 * @param arguments  the arguments to an OSCMessage
	 * @throws IOException if the currently used stream produces an exception when written to
	 * @throws OSCSerializeException if the arguments failed to serialize
	 */
	private void writeTypeTags(final List<?> arguments) throws IOException, OSCSerializeException {

		writeTypeTagsRaw(arguments);
		terminateAndAlign(getStream());
	}
}
