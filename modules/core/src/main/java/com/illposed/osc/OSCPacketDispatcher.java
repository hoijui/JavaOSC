/*
 * Copyright (C) 2003-2014, C. Ramakrishnan / Auracle.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import com.illposed.osc.argument.OSCTimeStamp;
import com.illposed.osc.argument.handler.StringArgumentHandler;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Dispatches {@link OSCPacket}s to registered listeners (<i>Method</i>s).
 */
public class OSCPacketDispatcher {

	private static final int DEFAULT_CORE_THREADS = 3;
	private final ByteBuffer argumentTypesBuffer;
	private final OSCSerializer serializer;
	private final Charset typeTagsCharset;
	private final Map<MessageSelector, OSCMessageListener> selectorToListener;
	private boolean metaInfoRequired;
	/**
	 * Whether to disregard bundle time-stamps for dispatch-scheduling.
	 */
	private boolean alwaysDispatchingImmediatly;
	private final ScheduledExecutorService dispatchScheduler;

	private static class NullOSCSerializer extends OSCSerializer {

		public NullOSCSerializer() {
			super(Collections.EMPTY_LIST, Collections.EMPTY_MAP, null);
		}

		@Override
		public void writeOnlyTypeTags(final List<?> arguments) throws OSCSerializeException {
			throw new IllegalStateException(
					"You need to either dispatch only packets containing meta-info, "
					+ "or supply a serialization factory to the dispatcher");
		}
	}

	private static class NullOSCSerializerFactory extends OSCSerializerFactory {
		@Override
		public OSCSerializer create(final ByteBuffer output) {
			return new NullOSCSerializer();
		}
	}

	public OSCPacketDispatcher(
			final OSCSerializerFactory serializerFactory,
			final ScheduledExecutorService dispatchScheduler)
	{
		final OSCSerializerFactory nonNullSerializerFactory;
		if (serializerFactory == null) {
			this.argumentTypesBuffer = ByteBuffer.allocate(0);
			nonNullSerializerFactory = new NullOSCSerializerFactory();
		} else {
			this.argumentTypesBuffer = ByteBuffer.allocate(64);
			nonNullSerializerFactory = serializerFactory;
		}
		this.serializer = nonNullSerializerFactory.create(argumentTypesBuffer);
		final Map<String, Object> serializationProperties
				= nonNullSerializerFactory.getProperties();
		final Charset propertiesCharset
				= (Charset) serializationProperties.get(StringArgumentHandler.PROP_NAME_CHARSET);
		this.typeTagsCharset = (propertiesCharset == null)
				? Charset.defaultCharset()
				: propertiesCharset;
		this.selectorToListener = new HashMap<MessageSelector, OSCMessageListener>();
		this.metaInfoRequired = false;
		this.alwaysDispatchingImmediatly = false;
		this.dispatchScheduler = dispatchScheduler;
	}

	public OSCPacketDispatcher(final OSCSerializerFactory serializerFactory) {
		this(serializerFactory, createDefaultDispatchScheduler());
	}

	public OSCPacketDispatcher() {
		this(null);
	}

	public static ScheduledExecutorService createDefaultDispatchScheduler() {
		return Executors.newScheduledThreadPool(DEFAULT_CORE_THREADS);
	}

	/**
	 * Set whether to disregard bundle time-stamps for dispatch-scheduling.
	 * @param alwaysDispatchingImmediatly if {@code true}, all bundles will be
	 *   dispatched immediately
	 */
	public void setAlwaysDispatchingImmediatly(final boolean alwaysDispatchingImmediatly) {
		this.alwaysDispatchingImmediatly = alwaysDispatchingImmediatly;
	}

	/**
	 * Indicates whether we disregard bundle time-stamps for dispatch-scheduling.
	 * @return {@code true}, if all bundles are dispatched immediately
	 */
	public boolean isAlwaysDispatchingImmediatly() {
		return alwaysDispatchingImmediatly;
	}

	/**
	 * Indicates whether we need outgoing messages to have meta-info attached.
	 * @return {@code true}, if at least one of the registered listeners requires message meta-info
	 */
	public boolean isMetaInfoRequired() {
		return metaInfoRequired;
	}

	/**
	 * Adds a listener (<i>Method</i> in OSC speak) that will be notified
	 * of incoming messages that match the selector.
	 * @param messageSelector selects which messages will be forwarded to the listener
	 * @param listener receives messages accepted by the selector
	 */
	public void addListener(
			final MessageSelector messageSelector,
			final OSCMessageListener listener)
	{
		selectorToListener.put(messageSelector, listener);
		if (messageSelector.isInfoRequired()) {
			metaInfoRequired = true;
		}
	}

	public void dispatchPacket(final OSCPacket packet) {
		dispatchPacket(packet, OSCTimeStamp.IMMEDIATE);
	}

	private void dispatchPacket(final OSCPacket packet, final OSCTimeStamp timestamp) {
		if (packet instanceof OSCBundle) {
			dispatchBundle((OSCBundle) packet);
		} else {
			dispatchMessageNow((OSCMessage) packet, timestamp);
		}
	}

	private class BundleDispatcher implements Runnable {

		private final OSCBundle bundle;

		BundleDispatcher(final OSCBundle bundle) {
			this.bundle = bundle;
		}

		@Override
		public void run() {
			dispatchBundleNow(bundle);
		}
	}

	private long calculateDelayFromNow(final OSCTimeStamp timestamp) {
		return timestamp.toDate().getTime() - System.currentTimeMillis();
	}

	private void dispatchBundle(final OSCBundle bundle) {
		final OSCTimeStamp timestamp = bundle.getTimestamp();
		if (isAlwaysDispatchingImmediatly() || timestamp.isImmediate()) {
			dispatchBundleNow(bundle);
		} else {
			// NOTE This scheduling accurracy is only to at most the accurracy of the
			//   default system clock, and thus might not be enough in some use-cases.
			//   It can never be more accurate then 1ms, and on many systems will be ~ 10ms.
			final long delayMs = calculateDelayFromNow(timestamp);
			dispatchScheduler.schedule(
					new BundleDispatcher(bundle),
					delayMs,
					TimeUnit.MILLISECONDS);
		}
	}

	private void dispatchBundleNow(final OSCBundle bundle) {
		final OSCTimeStamp timestamp = bundle.getTimestamp();
		final List<OSCPacket> packets = bundle.getPackets();
		for (final OSCPacket packet : packets) {
			dispatchPacket(packet, timestamp);
		}
	}

	private CharSequence generateTypeTagsString(final List<?> arguments) {

		try {
			serializer.writeOnlyTypeTags(arguments);
		} catch (final OSCSerializeException ex) {
			throw new IllegalArgumentException(
					"Failed generating Arguments Type Tag string while dispatching",
					ex);
		}
		argumentTypesBuffer.flip();
		final CharSequence typeTagsStr
				= new String(OSCSerializer.toByteArray(argumentTypesBuffer), typeTagsCharset);

		return typeTagsStr;
	}

	private void ensureMetaInfo(final OSCMessage message) {

		if (isMetaInfoRequired() && !message.isInfoSet()) {
			final CharSequence generateTypeTagsString
					= generateTypeTagsString(message.getArguments());
			final OSCMessageInfo messageInfo = new OSCMessageInfo(generateTypeTagsString);
			message.setInfo(messageInfo);
		}
	}

	private void dispatchMessageNow(final OSCMessage message, final OSCTimeStamp time) {

		ensureMetaInfo(message);

		for (final Entry<MessageSelector, OSCMessageListener> addrList
				: selectorToListener.entrySet())
		{
			if (addrList.getKey().matches(message)) {
				addrList.getValue().acceptMessage(time, message);
			}
		}
	}
}
