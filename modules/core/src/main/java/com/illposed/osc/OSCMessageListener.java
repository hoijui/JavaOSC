// SPDX-FileCopyrightText: 2003-2017 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc;

/**
 * Allows to listen to incoming messages.
 * In OSC speak, this is a <i>Method</i>, and it listens to <i>Messages</i>.
 */
public interface OSCMessageListener {

	/**
	 * Process a matching, incoming OSC Message.
	 * @param event the message received event to be processed
	 */
	void acceptMessage(OSCMessageEvent event);
}
