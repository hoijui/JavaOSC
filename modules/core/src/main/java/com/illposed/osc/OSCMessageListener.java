/*
 * Copyright (C) 2003-2014, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import com.illposed.osc.argument.OSCTimeTag64;

/**
 * Allows to listen to incoming messages.
 * In OSC speak, this is a <i>Method</i>, and it listens to <i>Messages</i>.
 */
public interface OSCMessageListener {

	/**
	 * Process a matching, incoming OSC Message.
	 * @param time when the message is to be processed.
	 *   This should be the time this method is called, or {@code OSCTimeTag64.IMMEDIATE}.
	 *   It may never be {@code null}.
	 * @param message to be processed
	 */
	void acceptMessage(OSCTimeTag64 time, OSCMessage message);
}
