// SPDX-FileCopyrightText: 2018 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc;

import com.illposed.osc.argument.OSCTimeTag64;

import java.util.EventObject;

/**
 * Event state of reception of an OSC Message.
 */
public class OSCMessageEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	private final OSCTimeTag64 time;
	private final OSCMessage message;

	/**
	 * Creates a new message received event.
	 * @param source The object on which the Event initially occurred.
	 * @param time when the message is to be processed.
	 *   This should be the time the event is delivered, or {@code OSCTimeTag64.IMMEDIATE}.
	 *   It may never be {@code null}.
	 * @param message the message that was received/is to be processed
	 */
	public OSCMessageEvent(final Object source, final OSCTimeTag64 time, final OSCMessage message) {
		super(source);

		this.time = time;
		this.message = message;
	}

	/**
	 * Returns the time for message processing.
	 * @return the time the message is to be processed.
	 * 	 This should be the time the event is delivered, or {@code OSCTimeTag64.IMMEDIATE}.
	 * 	 It may never be {@code null}.
	 */
	public OSCTimeTag64 getTime() {
		return time;
	}

	/**
	 * Returns the main content of this event.
	 * @return the received message
	 */
	public OSCMessage getMessage() {
		return message;
	}
}
