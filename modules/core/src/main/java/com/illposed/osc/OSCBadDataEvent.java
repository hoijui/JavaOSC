// SPDX-FileCopyrightText: 2017 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc;

import java.nio.ByteBuffer;
import java.util.EventObject;

/**
 * Event state of reception of unrecognized/bad (supposed to be) OSC data.
 */
public class OSCBadDataEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	private final SerializableByteBuffer data;
	private final OSCParseException exception;

	public OSCBadDataEvent(final Object source, final ByteBuffer data, final OSCParseException exception) {
		super(source);

		this.data = new SerializableByteBuffer(data.asReadOnlyBuffer());
		this.exception = exception;
	}

	/**
	 * Returns the bad/unrecognized bunch of data,
	 * which we expected to be OSC protocol formatted.
	 * @return the unrecognized, raw data, as received from
	 *   {@link com.illposed.osc.transport.OSCPortIn}, for example
	 */
	public ByteBuffer getData() {
		return data.getBuffer();
	}

	/**
	 * Returns the exception that was triggered this event,
	 * indicating that the received data is not proper OSC data.
	 * @return details about what went wrong when parsing the data
	 */
	public OSCParseException getException() {
		return exception;
	}
}
