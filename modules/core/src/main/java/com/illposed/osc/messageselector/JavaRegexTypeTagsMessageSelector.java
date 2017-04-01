/*
 * Copyright (C) 2014-2017, C. Ramakrishnan / Auracle.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.messageselector;

import com.illposed.osc.MessageSelector;
import com.illposed.osc.OSCMessage;
import java.util.regex.Pattern;

/**
 * Checks whether an OSC <i>Argument Type Tags</i> string matches a given Java regular expression.
 */
public class JavaRegexTypeTagsMessageSelector implements MessageSelector {

	private final Pattern selector;

	public JavaRegexTypeTagsMessageSelector(final Pattern selector) {
		this.selector = selector;
	}

	public JavaRegexTypeTagsMessageSelector(final String selectorRegex) {
		this(Pattern.compile(selectorRegex));
	}

	public static MessageSelector createAddressAndTypeTagsSelector(
			final Pattern addressSelector,
			final Pattern typeTagsSelector)
	{
		return new CombinedMessageSelector(
				new JavaRegexAddressMessageSelector(addressSelector),
				new JavaRegexTypeTagsMessageSelector(typeTagsSelector));
	}

	@Override
	public boolean equals(final Object other) {

		boolean equal = false;
		if (other instanceof JavaRegexTypeTagsMessageSelector) {
			final JavaRegexTypeTagsMessageSelector otherSelector
					= (JavaRegexTypeTagsMessageSelector) other;
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
		return selector.matcher(message.getInfo().getArgumentTypeTags()).matches();
	}
}
