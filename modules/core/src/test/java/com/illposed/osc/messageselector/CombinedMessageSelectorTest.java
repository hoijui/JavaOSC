/*
 * Copyright (C) 2019, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.illposed.osc.messageselector;

import com.illposed.osc.MessageSelector;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCMessageEvent;
import com.illposed.osc.OSCMessageInfo;
import org.junit.Assert;
import org.junit.Test;

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

		Assert.assertTrue( matches(matcher, "/", ""));
		Assert.assertFalse(matches(matcher, "/hello", ""));
		Assert.assertFalse(matches(matcher, "/", "i"));
		Assert.assertFalse(matches(matcher, "/hello", "i"));
	}

	@Test
	public void testNoArgs() {

		final MessageSelector matcher = new CombinedMessageSelector(
				new OSCPatternAddressMessageSelector("/hello"),
				new OSCPatternTypeTagsMessageSelector(""),
				CombinedMessageSelector.LogicOperator.AND);

		Assert.assertFalse( matches(matcher, "/", ""));
		Assert.assertTrue(matches(matcher, "/hello", ""));
		Assert.assertFalse(matches(matcher, "/", "i"));
		Assert.assertFalse(matches(matcher, "/hello", "i"));
	}

	@Test
	public void testAddressAndArgs() {

		final MessageSelector matcher = new CombinedMessageSelector(
				new OSCPatternAddressMessageSelector("/hello"),
				new OSCPatternTypeTagsMessageSelector("i"),
				CombinedMessageSelector.LogicOperator.AND);

		Assert.assertFalse(matches(matcher, "/", ""));
		Assert.assertFalse(matches(matcher, "/hello", ""));
		Assert.assertFalse(matches(matcher, "/", "i"));
		Assert.assertTrue( matches(matcher, "/hello", "i"));
	}

	@Test
	public void testPartPostfix() {

		final MessageSelector matcher = new CombinedMessageSelector(
				new OSCPatternAddressMessageSelector("/hello"),
				new OSCPatternTypeTagsMessageSelector("i"),
				CombinedMessageSelector.LogicOperator.OR);

		Assert.assertFalse(matches(matcher, "/", ""));
		Assert.assertTrue( matches(matcher, "/hello", ""));
		Assert.assertTrue( matches(matcher, "/", "i"));
		Assert.assertTrue( matches(matcher, "/hello", "i"));
	}

}
