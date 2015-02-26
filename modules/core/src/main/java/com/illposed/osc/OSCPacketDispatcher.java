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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
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

	private final ByteArrayOutputStream argumentTypesBuffer;
	private final OSCSerializer serializer;
	private final Charset typeTagsCharset;
	private final Map<MessageSelector, OSCListener> selectorToListener;
	private boolean metaInfoRequired;
	/**
	 * Whether to disregard bundle time-stamps for dispatch-scheduling.
	 */
	private boolean alwaysDispatchingImmediatly;
	private final ScheduledExecutorService dispatchScheduler;

	public OSCPacketDispatcher(final OSCSerializerFactory serializerFactory) {

		if (serializerFactory == null) {
			this.argumentTypesBuffer = null;
			this.serializer = null;
			this.typeTagsCharset = null;
		} else {
			this.argumentTypesBuffer = new ByteArrayOutputStream();
			this.serializer = serializerFactory.create(argumentTypesBuffer);
			final Map<String, Object> serializationProperties = serializerFactory.getProperties();
			final Charset propertiesCharset = (Charset) serializationProperties.get(StringArgumentHandler.PROP_NAME_CHARSET);
			this.typeTagsCharset = (propertiesCharset == null) ? Charset.defaultCharset() : propertiesCharset;
		}
		this.selectorToListener = new HashMap<MessageSelector, OSCListener>();
		this.metaInfoRequired = false;
		this.alwaysDispatchingImmediatly = false;
		this.dispatchScheduler = Executors.newScheduledThreadPool(3);
	}

	public OSCPacketDispatcher() {
		this(null);
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
	public void addListener(final MessageSelector messageSelector, final OSCListener listener) {

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
			dispatchScheduler.schedule(new BundleDispatcher(bundle), delayMs, TimeUnit.MILLISECONDS);
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

		final CharSequence typeTagsStr;
		if (serializer == null) {
			throw new IllegalStateException(
					"You need to either dispatch only packets containing meta-info, "
							+ "or supply a serialization factory to the dispatcher");
		} else {
			try {
				serializer.writeOnlyTypeTags(arguments);
			} catch (final IOException ex) {
				throw new IllegalStateException("This should only happen with full memory", ex);
			}
			final byte[] typeTags = argumentTypesBuffer.toByteArray();
			typeTagsStr = new String(typeTags, typeTagsCharset);
		}

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

		for (final Entry<MessageSelector, OSCListener> addrList : selectorToListener.entrySet()) {
			if (addrList.getKey().matches(message)) {
				addrList.getValue().acceptMessage(time, message);
			}
		}
	}
}
