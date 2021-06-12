// SPDX-FileCopyrightText: 2003-2017 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc;

import java.io.Serializable;

/**
 * An abstract superclass for messages and bundles.
 *
 * The actual packets are:
 * <ul>
 * <li>{@link OSCMessage}: simple OSC messages
 * <li>{@link OSCBundle}: OSC messages with timestamps
 *   and/or made up of multiple messages
 * </ul>
 */
public interface OSCPacket extends Serializable {

}
