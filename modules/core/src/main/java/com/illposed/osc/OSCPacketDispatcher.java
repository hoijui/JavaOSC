/*
 * Copyright (C) 2003-2014, C. Ramakrishnan / Auracle.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import com.illposed.osc.argument.OSCTimeStamp;
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

	private final Map<AddressSelector, OSCListener> selectorToListener;
	/**
	 * Whether to disregard bundle time-stamps for dispatch-scheduling.
	 */
	private boolean alwaysDispatchingImmediatly;

	private final ScheduledExecutorService dispatchScheduler;

	public OSCPacketDispatcher() {

		this.selectorToListener = new HashMap<AddressSelector, OSCListener>();
		this.alwaysDispatchingImmediatly = false;
		this.dispatchScheduler = Executors.newScheduledThreadPool(3);
	}

	public void setAlwaysDispatchingImmediatly(final boolean alwaysDispatchingImmediatly) {
		this.alwaysDispatchingImmediatly = alwaysDispatchingImmediatly;
	}

	public boolean isAlwaysDispatchingImmediatly() {
		return alwaysDispatchingImmediatly;
	}

	/**
	 * Adds a listener (<i>Method</i> in OSC speak) that will be notified
	 * of incoming messages that match the selector.
	 * @param addressSelector selects which messages will be forwarded to the listener,
	 *   depending on the message address
	 * @param listener receives messages accepted by the selector
	 */
	public void addListener(final AddressSelector addressSelector, final OSCListener listener) {
		selectorToListener.put(addressSelector, listener);
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

	private void dispatchMessageNow(final OSCMessage message, final OSCTimeStamp time) {
		for (final Entry<AddressSelector, OSCListener> addrList : selectorToListener.entrySet()) {
			if (addrList.getKey().matches(message)) {
				addrList.getValue().acceptMessage(time, message);
			}
		}
	}
}
