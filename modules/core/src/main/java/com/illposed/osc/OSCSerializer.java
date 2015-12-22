/*
 * Copyright (C) 2003-2014, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import com.illposed.osc.argument.ArgumentHandler;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	private final ByteBuffer output;
	/**
	 * Cache for Java classes of which we know, that we have no argument handler
	 * that supports serializing them.
	 */
	private final Set<Class> unsupportedTypes;
	/**
	 * Cache for Java classes that are sub-classes of a supported argument handlers Java class,
	 * mapped to that base class.
	 * This does not include base classes
	 * (Java classes directly supported by one of our argument handlers).
	 */
	private final Map<Class, Class> subToSuperTypes;
	/**
	 * For each base-class, indicates whether it is a marker-only type.
	 * @see ArgumentHandler#isMarkerOnly()
	 */
	private final Map<Class, Boolean> classToMarker;
	/**
	 * Maps supported Java class to argument-handlers for all our non-marker-only base-classes.
	 * @see ArgumentHandler#isMarkerOnly()
	 */
	private final Map<Class, ArgumentHandler> classToType;
	/**
	 * Maps values to argument-handlers for all our marker-only base-classes.
	 * @see ArgumentHandler#isMarkerOnly()
	 */
	private final Map<Object, ArgumentHandler> markerValueToType;
	private final Map<String, Object> properties;

	public OSCSerializer(
			final List<ArgumentHandler> types,
			final Map<String, Object> properties,
			final ByteBuffer output)
	{
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

		this.output = output;
		// usually, we should not need a stack size of 16, which is the default initial size
		this.unsupportedTypes = new HashSet<Class>(4);
		this.subToSuperTypes = new HashMap<Class, Class>(4);
		// TODO create at least a shallow copy of these maps
		this.classToMarker = Collections.unmodifiableMap(classToMarkerTmp);
		this.classToType = Collections.unmodifiableMap(classToTypeTmp);
		this.markerValueToType = Collections.unmodifiableMap(markerValueToTypeTmp);
		this.properties = Collections.unmodifiableMap(properties);
	}

	public Map<Class, ArgumentHandler> getClassToTypeMapping() {
		return classToType;
	}

	/**
	 * Returns the set of properties this parser was created with.
	 * @return the set of properties to adhere to
	 */
	public Map<String, Object> getProperties() {
		return properties;
	}

	public static byte[] toByteArray(final ByteBuffer buffer) {

		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		return bytes;
	}

	/**
	 * Terminates the previously written piece of data with a single {@code (byte) '0'}.
	 * We always need to terminate with a zero, especially when the stream is already aligned.
	 * @param output to receive the data-piece termination
	 */
	public static void terminate(final ByteBuffer output) {
		output.put((byte) 0);
	}

	/**
	 * Align a buffer by padding it with {@code (byte) '0'}s so it has a size divisible by 4.
	 * @param output to be aligned
	 */
	public static void align(final ByteBuffer output) {
		final int alignmentOverlap = output.position() % 4;
		final int padLen = (4 - alignmentOverlap) % 4;
		for (int pci = 0; pci < padLen; pci++) {
			output.put((byte) 0);
		}
	}

	/**
	 * Terminates the previously written piece of data with a single {@code (byte) '0'},
	 * and then aligns the stream by padding it with {@code (byte) '0'}s so it has a size
	 * divisible by 4.
	 * We always need to terminate with a zero, especially when the stream is already aligned.
	 * @param output to receive the data-piece termination and alignment
	 */
	public static void terminateAndAlign(final ByteBuffer output) {
		terminate(output);
		align(output);
	}

	private void write(final OSCBundle bundle) throws OSCSerializeException {
		write("#bundle");
		write(bundle.getTimestamp());
		for (final OSCPacket pkg : bundle.getPackets()) {
			writeSizeAndData(pkg);
		}
	}

	/**
	 * Serializes a messages address.
	 * @param message the address of this message will be serialized
	 * @throws OSCSerializeException if the message failed to serialize
	 */
	private void writeAddress(final OSCMessage message) throws OSCSerializeException {

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
	 * @throws OSCSerializeException if the message arguments failed to serialize
	 */
	private void writeArguments(final OSCMessage message) throws OSCSerializeException {

		output.put(OSCParser.TYPES_VALUES_SEPARATOR);
		writeTypeTags(message.getArguments());
		for (final Object argument : message.getArguments()) {
			write(argument);
		}
	}

	private void write(final OSCMessage message) throws OSCSerializeException {
		writeAddress(message);
		writeArguments(message);
	}

	/**
	 * Converts the packet into its OSC compliant byte array representation,
	 * then writes the number of bytes to the stream, followed by the actual data bytes.
	 * @param packet to be converted and written to the stream
	 * @throws OSCSerializeException if the packet failed to serialize
	 */
	private void writeSizeAndData(final OSCPacket packet) throws OSCSerializeException {

		final int sizePosition = output.position();
		write(Integer.valueOf(-1)); // write place-holder size
		writePacket(packet);
		final int afterPacketPosition = output.position();
		final int packetSize = afterPacketPosition - sizePosition - 4;
		output.position(sizePosition);
		write(packetSize);
		output.position(afterPacketPosition);
	}

	private void writePacket(final OSCPacket packet) throws OSCSerializeException {

		if (packet instanceof OSCBundle) {
			write((OSCBundle) packet);
		} else if (packet instanceof OSCMessage) {
			write((OSCMessage) packet);
		} else {
			throw new UnsupportedOperationException("We do not support writing packets of type: "
					+ packet.getClass());
		}
	}

	public void write(final OSCPacket packet) throws OSCSerializeException {

		output.rewind();
		writePacket(packet);
	}

	/**
	 * Write only the type tags for a given list of arguments.
	 * This is primarily used by the packet dispatcher.
	 * @param arguments to write the type tags from
	 * @throws OSCSerializeException if the arguments failed to serialize
	 */
	public void writeOnlyTypeTags(final List<?> arguments) throws OSCSerializeException {

		output.rewind();
		writeTypeTagsRaw(arguments);
	}

	private Set<ArgumentHandler> findSuperTypes(final Class argumentClass) {

		final Set<ArgumentHandler> matchingSuperTypes = new HashSet<ArgumentHandler>();

		// check all base-classes, for whether our argument-class is a sub-class
		// of any of them
		for (final Map.Entry<Class, ArgumentHandler> baseClassAndType
				: classToType.entrySet())
		{
			final Class<?> baseClass = baseClassAndType.getKey();
			if ((baseClass != Object.class)
					&& baseClass.isAssignableFrom(argumentClass))
			{
				matchingSuperTypes.add(baseClassAndType.getValue());
			}
		}

		return matchingSuperTypes;
	}

	private Class findSuperType(final Class argumentClass) throws OSCSerializeException {

		Class superType;

		// check if we already found the base-class for this argument-class before
		superType = subToSuperTypes.get(argumentClass);
		// ... if we did not, ...
		if ((superType == null)
				// check if we already know this argument-class to not be supported
				&& !unsupportedTypes.contains(argumentClass))
		{
			final Set<ArgumentHandler> matchingSuperTypes = findSuperTypes(argumentClass);
			if (matchingSuperTypes.isEmpty()) {
				unsupportedTypes.add(argumentClass);
			} else {
				if (matchingSuperTypes.size() > 1) {
					System.out.println("WARNING: Java class "
							+ argumentClass.getCanonicalName()
							+ " is a sub-class of multiple supported argument types:");
					for (final ArgumentHandler matchingSuperType : matchingSuperTypes) {
						System.out.println('\t'
								+ matchingSuperType.getJavaClass().getCanonicalName()
								+ " (supported by "
								+ matchingSuperType.getClass().getCanonicalName()
								+ ')');
					}
				}
				final ArgumentHandler matchingSuperType = matchingSuperTypes.iterator().next();
				System.out.println("INFO: Java class "
						+ argumentClass.getCanonicalName()
						+ " will be mapped to "
						+ matchingSuperType.getJavaClass().getCanonicalName()
						+ " (supported by "
						+ matchingSuperType.getClass().getCanonicalName()
						+ ')');
				final Class matchingSuperClass = matchingSuperType.getJavaClass();
				subToSuperTypes.put(argumentClass, matchingSuperClass);
				superType = matchingSuperClass;
			}
		}

		if (superType == null) {
			throw new OSCSerializeException("No type handler registered for serializing class "
					+ argumentClass.getCanonicalName());
		}

		return superType;
	}

	private ArgumentHandler findType(final Object argumentValue, final Class argumentClass)
			throws OSCSerializeException
	{
		final ArgumentHandler type;

		final Boolean markerType = classToMarker.get(argumentClass);
		if (markerType == null) {
			type = findType(argumentValue, findSuperType(argumentClass));
		} else if (markerType) {
			type = markerValueToType.get(argumentValue);
		} else {
			type = classToType.get(argumentClass);
		}

		return type;
	}

	private ArgumentHandler findType(final Object argumentValue) throws OSCSerializeException {
		return findType(argumentValue, extractTypeClass(argumentValue));
	}

	/**
	 * Write an object into the byte stream.
	 * @param anObject (usually) one of Float, Double, String, Character, Integer, Long,
	 *   or a Collection of these.
	 *   See {@link #getClassToTypeMapping()} for a complete list of which classes may be used here.
	 * @throws OSCSerializeException if the argument object failed to serialize
	 */
	private void write(final Object anObject) throws OSCSerializeException {

		if (anObject instanceof Collection) {
			// We can safely suppress the warning, as we already made sure the cast will not fail.
			@SuppressWarnings("unchecked") final Collection<?> theArray = (Collection<?>) anObject;
			for (final Object entry : theArray) {
				write(entry);
			}
		} else {
			final ArgumentHandler type = findType(anObject);
			type.serialize(output, anObject);
		}
	}

	private static Class extractTypeClass(final Object value) {
		return (value == null) ? Object.class : value.getClass();
	}

	/**
	 * Write the OSC specification type tag for the type a certain Java type
	 * converts to.
	 * @param value of this argument, we need to write the type identifier
	 * @throws OSCSerializeException if the value failed to serialize
	 */
	private void writeType(final Object value) throws OSCSerializeException {

		final ArgumentHandler type = findType(value);
		output.put((byte) type.getDefaultIdentifier());
	}

	/**
	 * Write the type tags for a given list of arguments.
	 * @param arguments array of base Objects
	 * @throws OSCSerializeException if the arguments failed to serialize
	 */
	private void writeTypeTagsRaw(final List<?> arguments) throws OSCSerializeException
	{
		for (final Object argument : arguments) {
			if (argument instanceof List) {
				final List<?> argumentsArray = (List<?>) argument;
				// This is used for nested arguments.
				// open the array
				output.put((byte) OSCParser.TYPE_ARRAY_BEGIN);
				// fill the [] with the nested argument types
				writeTypeTagsRaw(argumentsArray);
				// close the array
				output.put((byte) OSCParser.TYPE_ARRAY_END);
			} else {
				// write a single, simple arguments type
				writeType(argument);
			}
		}
	}

	/**
	 * Write the type tags for a given list of arguments, and cleanup the stream.
	 * @param arguments  the arguments to an OSCMessage
	 * @throws OSCSerializeException if the arguments failed to serialize
	 */
	private void writeTypeTags(final List<?> arguments) throws OSCSerializeException {

		writeTypeTagsRaw(arguments);
		terminateAndAlign(output);
	}
}
