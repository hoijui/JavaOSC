// SPDX-FileCopyrightText: 2014-2017 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc.argument;

import java.io.Serializable;

/**
 * An OSC 1.0 optional, and OSC 1.1 required argument type.
 * Impulse aka "bang", is used for event triggers.
 * No bytes are allocated in the argument data.
 * This type was named "Infinitum" in OSC 1.0.
 * Use like this:
 * <blockquote><pre>{@code
 * OSCMessage msg = new OSCMessage("/my/address");
 * msg.addArgument(OSCImpulse.INSTANCE);
 * }</pre></blockquote>
 */
public final class OSCImpulse implements Serializable {

	public static final OSCImpulse INSTANCE = new OSCImpulse();

	private static final long serialVersionUID = 1L;

	private OSCImpulse() {}

	@Override
	public String toString() {
		return "I";
	}
}
