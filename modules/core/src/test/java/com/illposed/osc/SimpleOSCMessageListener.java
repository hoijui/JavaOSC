/*
 * Copyright (C) 2001, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import com.illposed.osc.argument.OSCTimeStamp;

public class SimpleOSCMessageListener implements OSCMessageListener {

	private boolean messageReceived;
	private OSCTimeStamp receivedTimestamp;

	public SimpleOSCMessageListener() {

		this.messageReceived = false;
		this.receivedTimestamp = null;
	}

	public OSCTimeStamp getReceivedTimestamp() {
		return receivedTimestamp;
	}

	public boolean isMessageReceived() {
		return messageReceived;
	}

	@Override
	public void acceptMessage(final OSCTimeStamp time, final OSCMessage message) {
		messageReceived = true;
		receivedTimestamp = time;
	}
}
