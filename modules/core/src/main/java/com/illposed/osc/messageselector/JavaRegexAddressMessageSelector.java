// SPDX-FileCopyrightText: 2014-2017 C. Ramakrishnan / Auracle
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc.messageselector;

import com.illposed.osc.MessageSelector;
import com.illposed.osc.OSCMessageEvent;

import java.util.regex.Pattern;

/**
 * Checks whether an OSC <i>Address Pattern</i> matches a given Java regular expression.
 */
public class JavaRegexAddressMessageSelector implements MessageSelector {

	private final Pattern selector;

	// Public API
	@SuppressWarnings("WeakerAccess")
	public JavaRegexAddressMessageSelector(final Pattern selector) {
		this.selector = selector;
	}

	public JavaRegexAddressMessageSelector(final String selectorRegex) {
		this(Pattern.compile(selectorRegex));
	}

	@Override
	public boolean equals(final Object other) {

		boolean equal = false;
		if (other instanceof JavaRegexAddressMessageSelector) {
			final JavaRegexAddressMessageSelector otherSelector
					= (JavaRegexAddressMessageSelector) other;
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
		return false;
	}

	@Override
	public boolean matches(final OSCMessageEvent messageEvent) {
		return selector.matcher(messageEvent.getMessage().getAddress()).matches();
	}
}
