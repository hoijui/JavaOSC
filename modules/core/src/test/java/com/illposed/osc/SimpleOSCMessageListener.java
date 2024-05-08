// SPDX-FileCopyrightText: 2001 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 - 2024 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc;

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
