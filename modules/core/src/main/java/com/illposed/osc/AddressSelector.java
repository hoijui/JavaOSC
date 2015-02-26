/*
 * Copyright (C) 2014, C. Ramakrishnan / Auracle.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

/**
 * Checks whether an OSC <i>Message</i> fulfills certain criteria.
 */
public interface AddressSelector {

	/**
	 * Checks whether the OSC <i>Message</i> in question matches this selector.
	 * @param message to be checked if it matches
	 * @return {@code true} if this matcher selects the message in question
	 */
	boolean matches(OSCMessage message);
}
