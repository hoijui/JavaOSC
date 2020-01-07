/*
 * Copyright (C) 2003-2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import com.illposed.osc.argument.ArgumentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
	 * If a supplied arguments class implements more then one supported argument types,
	 * there is no solid way to figure out as which one we should interpret and serialize it,
	 * so we will fail fast.
	 */
	private static final int MAX_IMPLEMENTED_ARGUMENT_TYPES = 1;

	private final Logger log = LoggerFactory.getLogger(OSCSerializer.class);

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

	/**
	 * Creates a new serializer with all the required ingredients.
	 * @param types all of these, and only these arguments will be serializable
	 *   by this object, that are supported by these handlers
	 * @param properties see {@link ArgumentHandler#setProperties(Map)}
	 */
	public OSCSerializer(
			final List<ArgumentHandler> types,
			final Map<String, Object> properties)
	{
		final Map<Class, Boolean> classToMarkerTmp = new HashMap<>(types.size());
		final Map<Class, ArgumentHandler> classToTypeTmp = new HashMap<>();
		final Map<Object, ArgumentHandler> markerValueToTypeTmp = new HashMap<>();
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
					throw new IllegalStateException("Developer error; this should never happen",
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
		this.unsupportedTypes = new HashSet<>(4);
		this.subToSuperTypes = new HashMap<>(4);
		// We create (shallow) copies of these collections,
		// so if "the creator" modifies them after creating us,
		// we do not get different behaviour during our lifetime,
		// which might be very confusing to users of this class.
		// As the copies are not deep though,
		// It does not protect us from change in behaviour
		// do to change of the objects themselves,
		// which are contained in these collections.
		// TODO instead of these shallow copies, maybe create deep ones?
		this.classToMarker = Collections.unmodifiableMap(
				new HashMap<>(classToMarkerTmp));
		this.classToType = Collections.unmodifiableMap(
				new HashMap<>(classToTypeTmp));
		this.markerValueToType = Collections.unmodifiableMap(
				new HashMap<>(markerValueToTypeTmp));
		this.properties = Collections.unmodifiableMap(
				new HashMap<>(properties));
	}

	// Public API
	@SuppressWarnings({"WeakerAccess", "unused"})
	public Map<Class, ArgumentHandler> getClassToTypeMapping() {
		return classToType;
	}

	/**
	 * Returns the set of properties this parser was created with.
	 * @return the set of properties to adhere to
	 * @see ArgumentHandler#setProperties(Map)
	 */
	public Map<String, Object> getProperties() {
		return properties;
	}

	private interface Transformer<A, B> {
		public B transform(A thing) throws OSCSerializeException;
	}

  // A wrapper around Util.concat(Function<T, byte[]>, T...) that works around
  // the limitation of Java functions that they can't throw checked exceptions.
  //
  // We use Util.concat with a function internally; the function does work that
  // might throw an OSCSerializeException, and if an OSCSerializeException is
  // thrown, we catch it and re-throw it as a RuntimeException, thereby avoiding
  // a compilation error because functions can't throw checked exceptions. But
  // we still want the OSCSerializeException to percolate upward, so we have an
  // outer try/catch that catches RuntimeExceptions, checks to see if the cause
  // was an OSCSerializeException, and if it was, then we throw the
  // OSCSerializeException.
	private <T> byte[] concat(Transformer<T, byte[]> transformer, List<T> things)
	throws OSCSerializeException
	{
		try {
			return Util.concat(
				thing -> {
					try {
						return transformer.transform(thing);
					} catch (OSCSerializeException ex) {
						throw new RuntimeException(ex);
					}
				},
				things
			);
		} catch (RuntimeException runtimeException) {
			Throwable cause = runtimeException.getCause();

			if (cause instanceof OSCSerializeException) {
				throw (OSCSerializeException)cause;
			}

			throw runtimeException;
		}
	}


	/**
	 * Returns the "final" version of a serialized packet, which ends in at least
	 * one {@code (byte) '0'} byte and has a size divisible by {@link OSCParser#ALIGNMENT_BYTES}.
	 * We always need to terminate with a zero, especially when the stream is
	 * already aligned.
	 * @param packet the array of bytes that have been serialized so far
	 * @return a slightly longer byte array that is the input followed by 1 or
	 * more 0 bytes
	 */
	public static byte[] terminatedAndAligned(final byte[] packet) {
		// Unless `packet` already ends with a 0 byte, we need at least 1 of them.
		int numZeroBytes =
			packet.length == 0 || packet[packet.length - 1] != 0
			? 1
			: 0;

		// Add additional 0 bytes to ensure that the packet length is divisible by
		// OSCParser.ALIGNMENT_BYTES.
		final int ab = OSCParser.ALIGNMENT_BYTES;
		final int overlap = (packet.length + numZeroBytes) % ab;
		numZeroBytes += ((ab - overlap) % ab);

		final byte[] zeroBytes = new byte[numZeroBytes];
		Arrays.fill(zeroBytes, (byte)0);

		return Util.concat(packet, zeroBytes);
	}

	private byte[] serialize(final OSCBundle bundle)
	throws OSCSerializeException
	{
		return Util.concat(
			serialize(OSCParser.BUNDLE_START),
			serialize(bundle.getTimestamp()),
			concat(
        new Transformer<OSCPacket, byte[]>() {
          public byte[] transform(OSCPacket packet) throws OSCSerializeException {
            byte[] packetBytes = serialize(packet);

            // Serialize the length of the packet followed by the data.
            return Util.concat(
              serialize(packetBytes.length),
              packetBytes
            );
          }
        },
				bundle.getPackets()
			)
		);
	}

	private byte[] serialize(final OSCMessage message)
	throws OSCSerializeException
	{
    String address = message.getAddress();
    List<Object> arguments = message.getArguments();

    return Util.concat(
      serialize(address),
      terminatedAndAligned(
        Util.concat(
          new byte[]{OSCParser.TYPES_VALUES_SEPARATOR},
          serializedTypeTags(arguments)
        )
      ),
      concat(
        new Transformer<Object, byte[]>() {
          public byte[] transform(Object argument) throws OSCSerializeException {
            return serialize(argument);
          }
        },
        arguments
      )
    );
	}

	public byte[] serialize(final OSCPacket packet)
	throws OSCSerializeException
	{
		if (packet instanceof OSCBundle) {
			return serialize((OSCBundle) packet);
		}

		if (packet instanceof OSCMessage) {
			return serialize((OSCMessage) packet);
		}

		throw new UnsupportedOperationException(
			"We do not support writing packets of type: " + packet.getClass()
		);
	}

	private Set<ArgumentHandler> findSuperTypes(final Class argumentClass) {

		final Set<ArgumentHandler> matchingSuperTypes = new HashSet<>();

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

		// check if we already found the base-class for this argument-class before
		Class superType = subToSuperTypes.get(argumentClass);
		// ... if we did not, ...
		if ((superType == null)
				// check if we already know this argument-class to not be supported
				&& !unsupportedTypes.contains(argumentClass))
		{
			final Set<ArgumentHandler> matchingSuperTypes = findSuperTypes(argumentClass);
			if (matchingSuperTypes.isEmpty()) {
				unsupportedTypes.add(argumentClass);
			} else {
				if (matchingSuperTypes.size() > MAX_IMPLEMENTED_ARGUMENT_TYPES) {
					log.warn("Java class {} is a sub-class of multiple supported argument types:",
							argumentClass.getCanonicalName());
					for (final ArgumentHandler matchingSuperType : matchingSuperTypes) {
						log.warn("\t{} (supported by {})",
								matchingSuperType.getJavaClass().getCanonicalName(),
								matchingSuperType.getClass().getCanonicalName());
					}
				}
				final ArgumentHandler matchingSuperType = matchingSuperTypes.iterator().next();
				log.info("Java class {} will be mapped to {} (supported by {})",
						argumentClass.getCanonicalName(),
						matchingSuperType.getJavaClass().getCanonicalName(),
						matchingSuperType.getClass().getCanonicalName());
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

	private ArgumentHandler findHandler(
		final Object argumentValue, final Class argumentClass)
		throws OSCSerializeException
	{
		final Boolean markerType = classToMarker.get(argumentClass);

		if (markerType == null) {
			return findHandler(argumentValue, findSuperType(argumentClass));
		}

		if (markerType) {
			return markerValueToType.get(argumentValue);
		}

		return classToType.get(argumentClass);
	}

	private ArgumentHandler findHandler(final Object argumentValue)
	throws OSCSerializeException
	{
		return findHandler(argumentValue, extractTypeClass(argumentValue));
	}

	/**
	 * Returns the byte representation of an object.
	 * @param anObject (usually) one of Float, Double, String, Character, Integer,
	 *   Long, or a Collection of these.
	 *   See {@link #getClassToTypeMapping()} for a complete list of which classes
	 *   may be used here.
	 * @return the byte representation of the object
	 * @throws OSCSerializeException if the argument object failed to serialize
	 */
	private byte[] serialize(final Object anObject)
	throws OSCSerializeException
	{
		if (anObject instanceof Collection) {
      // We can safely suppress the warning, as we already made sure the cast
      // will not fail.
      @SuppressWarnings("unchecked")
      final Collection<?> theArray = (Collection<?>) anObject;

      return concat(
        new Transformer<Object, byte[]>() {
          public byte[] transform(Object entry) throws OSCSerializeException {
            return serialize(entry);
          }
        },
        theArray.stream().collect(Collectors.toList())
      );
		}

		@SuppressWarnings("unchecked")
		final ArgumentHandler<Object> handler = findHandler(anObject);
		return handler.serialize(anObject);
	}

	private static Class extractTypeClass(final Object value) {
		return (value == null) ? Object.class : value.getClass();
	}

	/**
	 * Serializes the type tags for a given list of arguments.
	 * This is primarily used by the packet dispatcher.
	 * @param arguments to write the type tags from
	 * @return the serialized type tags as a byte array
	 * @throws OSCSerializeException if the arguments failed to serialize
	 */
	public byte[] serializedTypeTags(final List<Object> arguments)
	throws OSCSerializeException
	{
    return concat(
      new Transformer<Object, byte[]>() {
        public byte[] transform(Object argument) throws OSCSerializeException {
          // Serialize nested arguments.
          if (argument instanceof List) {
            @SuppressWarnings("unchecked")
            final List<Object> argumentsArray = (List<Object>) argument;

            return Util.concat(
              new byte[]{OSCParser.TYPE_ARRAY_BEGIN},
              serializedTypeTags(argumentsArray),
              new byte[]{OSCParser.TYPE_ARRAY_END}
            );
          }

          // Serialize a single, simple arguments type.
          return new byte[]{
            (byte) findHandler(argument).getDefaultIdentifier()
          };
        }
      },
      arguments
    );
	}
}
