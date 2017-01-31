/*
 * Copyright (C) 2001, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import com.illposed.osc.argument.OSCTimeTag64;

public class SimpleOSCMessageListener implements OSCMessageListener {

	private boolean messageReceived;
	private OSCTimeTag64 receivedTimestamp;
	private OSCMessage message;

	public SimpleOSCMessageListener() {

		this.messageReceived = false;
		this.receivedTimestamp = null;
		this.message = null;
	}

	public OSCTimeTag64 getReceivedTimestamp() {
		return receivedTimestamp;
	}

	public boolean isMessageReceived() {
		return messageReceived;
	}

	public OSCMessage getMessage() {
		return message;
	}

	@Override
	public void acceptMessage(final OSCTimeTag64 time, final OSCMessage message) {
		messageReceived = true;
		receivedTimestamp = time;
		this.message = message;
	}
}
