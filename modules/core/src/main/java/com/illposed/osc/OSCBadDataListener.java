// SPDX-FileCopyrightText: 2017 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc;

/**
 * Receives data that could not be properly parsed as OSC packet,
 * be it due to the data being invalid OSC, or a bug in this library.
 * This is most useful for error logging or debugging.
 */
public interface OSCBadDataListener {

	/**
	 * Process a bad/unrecognized bunch of data,
	 * which we expected to be OSC protocol formatted.
	 * @param evt bad OSC data event state
	 */
	void badDataReceived(OSCBadDataEvent evt);
}
