// SPDX-FileCopyrightText: 2015-2017 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc;

import java.nio.ByteBuffer;

public class OSCParseException extends Exception {
	private final ByteBuffer data;

	public ByteBuffer getData() {
		return data;
	}

	public OSCParseException(final String message, final ByteBuffer data) {
		super(message);
		this.data = data;
	}

	public OSCParseException(final Throwable cause, final ByteBuffer data) {
		super(cause);
		this.data = data;
	}

	public OSCParseException(
		final String message,
		final Throwable cause,
		final ByteBuffer data)
	{
		super(message, cause);
		this.data = data;
	}
}
