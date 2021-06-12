// SPDX-FileCopyrightText: 2014-2017 C. Ramakrishnan / Auracle
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc.messageselector;

import com.illposed.osc.MessageSelector;
import com.illposed.osc.OSCMessageEvent;

import java.util.regex.Pattern;

/**
 * Checks whether an OSC <i>Argument Type Tags</i> string matches a given Java regular expression.
 */
public class JavaRegexTypeTagsMessageSelector implements MessageSelector {

	private final Pattern selector;

	// Public API
	@SuppressWarnings("WeakerAccess")
	public JavaRegexTypeTagsMessageSelector(final Pattern selector) {
		this.selector = selector;
	}

	// Public API
	@SuppressWarnings("unused")
	public JavaRegexTypeTagsMessageSelector(final String selectorRegex) {
		this(Pattern.compile(selectorRegex));
	}

	// Public API
	@SuppressWarnings("unused")
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
	public boolean matches(final OSCMessageEvent messageEvent) {
		return selector.matcher(messageEvent.getMessage().getInfo().getArgumentTypeTags()).matches();
	}
}
