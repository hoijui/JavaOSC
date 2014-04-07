/*
 * Copyright (C) 2003, C. Ramakrishnan / Auracle.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.utility;

import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPacket;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Dispatches {@link OSCPacket}s to registered listeners (<i>Method</i>s).
 *
 * @author Chandrasekhar Ramakrishnan
 */
public class OSCPacketDispatcher {

	private final Map<String, OSCListener> addressToListener;

	public OSCPacketDispatcher() {
		this.addressToListener = new HashMap<String, OSCListener>();
	}

	/**
	 * Adds a listener that will then be notified of incoming messages.
	 * @param addressSelector addresses of incoming messages are checked
	 *   against this; you may use Java regular expressions here FIXME see the fix-me of {@link #matches(String, String)}
	 * @param listener will be notified of incoming packets, if they match
	 */
	public void addListener(String addressSelector, OSCListener listener) {
		addressToListener.put(addressSelector, listener);
	}

	public void dispatchPacket(OSCPacket packet) {
		dispatchPacket(packet, null);
	}

	public void dispatchPacket(OSCPacket packet, Date timestamp) {
		if (packet instanceof OSCBundle) {
			dispatchBundle((OSCBundle) packet);
		} else {
			dispatchMessage((OSCMessage) packet, timestamp);
		}
	}

	private void dispatchBundle(OSCBundle bundle) {
		Date timestamp = bundle.getTimestamp();
		List<OSCPacket> packets = bundle.getPackets();
		for (OSCPacket packet : packets) {
			dispatchPacket(packet, timestamp);
		}
	}

	/**
	 * Checks whether a OSC message address matches the given supplier.
	 * The matching is currently done with Java regexp matching.
	 * FIXME OSC standard requires wildcard matching (as in shell file-name matching, for example)
	 * @param messageAddress for example "/sc/mixer/volume"
	 * @param selector for example "/sc/[^/]+/volume" or "/sc/mixer/volume"
	 * @return true if the message matches the supplied selector
	 */
	private boolean matches(String messageAddress, String selector) {
		return messageAddress.matches(selector);
	}

	private void dispatchMessage(OSCMessage message, Date time) {
		for (Entry<String, OSCListener> addrList : addressToListener.entrySet()) {
			if (matches(message.getAddress(), addrList.getKey())) {
				addrList.getValue().acceptMessage(time, message);
			}
		}
	}
}
