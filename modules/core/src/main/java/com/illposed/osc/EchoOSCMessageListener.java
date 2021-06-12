// SPDX-FileCopyrightText: 2017 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prints out received messages to a {@link Logger}.
 * Messages are printed in this format:
 * <blockquote><pre>{@code
 * address
 *   type-tags
 *     argument-0
 *     argument-1
 *     ...
 *     argument-(n-1)
 * new-line
 * }</pre></blockquote>
 * so for example:
 * <blockquote><pre>{@code
 * /my/messages/address
 *   iffc
 *     100
 *     0.3
 *     99.2
 *     x
 *
 * /my/other/messages/address
 *   fsi
 *     9234.63
 *     hello world
 *     8325
 *
 * }</pre></blockquote>
 * @see ConsoleEchoServer
 */
public class EchoOSCMessageListener implements OSCMessageListener {

	private final Logger log;

	// Public API
	@SuppressWarnings("WeakerAccess")
	public EchoOSCMessageListener(final Logger log) {

		this.log = log;
	}

	// Public API
	@SuppressWarnings("unused")
	public EchoOSCMessageListener() {
		this(LoggerFactory.getLogger(EchoOSCMessageListener.class));
	}

	@Override
	public void acceptMessage(final OSCMessageEvent event) {

		final OSCMessage message = event.getMessage();
		log.info(message.getAddress());
		log.info("  {}", message.getInfo().getArgumentTypeTags());
		for (final Object arg : message.getArguments()) {
			log.info("    {}", arg);
		}
		log.info("");
	}
}
