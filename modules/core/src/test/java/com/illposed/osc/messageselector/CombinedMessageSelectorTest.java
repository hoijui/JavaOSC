// SPDX-FileCopyrightText: 2019 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc.messageselector;

import com.illposed.osc.MessageSelector;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCMessageEvent;
import com.illposed.osc.OSCMessageInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CombinedMessageSelectorTest {

	private boolean matches(final MessageSelector selector, final String address, final String typeTags) {

		final OSCMessage message = new OSCMessage(address);
		message.setInfo(new OSCMessageInfo(typeTags));
		return selector.matches(new OSCMessageEvent(this, null, message));
	}

	@Test
	public void testNoAddressNorArgs() {

		final MessageSelector matcher = new CombinedMessageSelector(
				new OSCPatternAddressMessageSelector("/"),
				new OSCPatternTypeTagsMessageSelector(""),
				CombinedMessageSelector.LogicOperator.AND);

		Assertions.assertTrue( matches(matcher, "/", ""));
		Assertions.assertFalse(matches(matcher, "/hello", ""));
		Assertions.assertFalse(matches(matcher, "/", "i"));
		Assertions.assertFalse(matches(matcher, "/hello", "i"));
	}

	@Test
	public void testNoArgs() {

		final MessageSelector matcher = new CombinedMessageSelector(
				new OSCPatternAddressMessageSelector("/hello"),
				new OSCPatternTypeTagsMessageSelector(""),
				CombinedMessageSelector.LogicOperator.AND);

		Assertions.assertFalse( matches(matcher, "/", ""));
		Assertions.assertTrue(matches(matcher, "/hello", ""));
		Assertions.assertFalse(matches(matcher, "/", "i"));
		Assertions.assertFalse(matches(matcher, "/hello", "i"));
	}

	@Test
	public void testAddressAndArgs() {

		final MessageSelector matcher = new CombinedMessageSelector(
				new OSCPatternAddressMessageSelector("/hello"),
				new OSCPatternTypeTagsMessageSelector("i"),
				CombinedMessageSelector.LogicOperator.AND);

		Assertions.assertFalse(matches(matcher, "/", ""));
		Assertions.assertFalse(matches(matcher, "/hello", ""));
		Assertions.assertFalse(matches(matcher, "/", "i"));
		Assertions.assertTrue( matches(matcher, "/hello", "i"));
	}

	@Test
	public void testPartPostfix() {

		final MessageSelector matcher = new CombinedMessageSelector(
				new OSCPatternAddressMessageSelector("/hello"),
				new OSCPatternTypeTagsMessageSelector("i"),
				CombinedMessageSelector.LogicOperator.OR);

		Assertions.assertFalse(matches(matcher, "/", ""));
		Assertions.assertTrue( matches(matcher, "/hello", ""));
		Assertions.assertTrue( matches(matcher, "/", "i"));
		Assertions.assertTrue( matches(matcher, "/hello", "i"));
	}

}
