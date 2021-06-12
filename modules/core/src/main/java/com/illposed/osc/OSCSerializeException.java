// SPDX-FileCopyrightText: 2015-2017 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc;

public class OSCSerializeException extends Exception {

	public OSCSerializeException() {
		super();
	}

	public OSCSerializeException(final String message) {
		super(message);
	}

	public OSCSerializeException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public OSCSerializeException(final Throwable cause) {
		super(cause);
	}
}
