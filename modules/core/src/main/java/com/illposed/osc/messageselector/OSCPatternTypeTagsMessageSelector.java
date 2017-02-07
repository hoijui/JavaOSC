/*
 * Copyright (C) 2014, C. Ramakrishnan / Auracle.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.messageselector;

import com.illposed.osc.MessageSelector;
import com.illposed.osc.OSCMessage;

/**
 * Checks whether an OSC <i>Argument Type Tags</i> string matches a given wildcard expression,
 * as described in the OSC protocol specification for address matching.
 * See {@link OSCPatternAddressMessageSelector the corresponding address selector} for more details.
 */
public class OSCPatternTypeTagsMessageSelector implements MessageSelector {

	private final String selector;

	public OSCPatternTypeTagsMessageSelector(final String selector) {
		this.selector = selector;
	}

	public static MessageSelector createAddressAndTypeTagsSelector(
			final String addressSelector,
			final String typeTagsSelector)
	{
		return new CombinedMessageSelector(
				new OSCPatternAddressMessageSelector(addressSelector),
				new OSCPatternTypeTagsMessageSelector(typeTagsSelector));
	}

	@Override
	public boolean equals(final Object other) {

		boolean equal = false;
		if (other instanceof OSCPatternTypeTagsMessageSelector) {
			final OSCPatternTypeTagsMessageSelector otherSelector
					= (OSCPatternTypeTagsMessageSelector) other;
			equal = this.selector.equals(otherSelector.selector);
		}

		return equal;
	}

	@Override
	public int hashCode() {
		return this.selector.hashCode();
	}

	@Override
	public boolean isInfoRequired() {
		return true;
	}

	@Override
	public boolean matches(final OSCMessage message) {
		return OSCPatternAddressMessageSelector.matches(
				message.getInfo().getArgumentTypeTags().toString(),
				selector);
	}
}
