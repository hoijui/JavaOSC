/*
 * Copyright (C) 2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import com.illposed.osc.argument.OSCTimeStamp;
import java.io.PrintStream;

/**
 * Prints out received messages to a {@link PrintStream},
 * most commonly {@link System#out}.
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

	private final PrintStream out;

	public EchoOSCMessageListener(final PrintStream out) {

		this.out = out;
	}

	public EchoOSCMessageListener() {
		this(System.out);
	}

	@Override
	public void acceptMessage(final OSCTimeStamp time, final OSCMessage message) {

		out.println(message.getAddress());
		out.printf("  %s%n", message.getInfo().getArgumentTypeTags());
		for (final Object arg : message.getArguments()) {
			out.printf("    %s%n", arg.toString());
		}
		out.println();
	}
}
