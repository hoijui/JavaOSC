/*
 * Copyright (C) 2001, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.illposed.osc;

import com.illposed.osc.argument.OSCTimeTag64;

public class SimpleOSCMessageListener implements OSCMessageListener {

	private int messageReceivedCount;
	private OSCMessageEvent event;

	public SimpleOSCMessageListener() {

		this.messageReceivedCount = 0;
		this.event = null;
	}
	public boolean isMessageReceived() {
		return (messageReceivedCount > 0);
	}

	@SuppressWarnings("WeakerAccess")
	public int getMessageReceivedCount() {
		return messageReceivedCount;
	}

	public OSCMessageEvent getReceivedEvent() {
		return event;
	}

	@Override
	public void acceptMessage(final OSCMessageEvent event) {
		messageReceivedCount++;
		this.event = event;
	}
}
