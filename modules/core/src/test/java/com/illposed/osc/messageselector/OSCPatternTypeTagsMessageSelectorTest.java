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

public class OSCPatternTypeTagsMessageSelectorTest {

	private boolean matches(final MessageSelector selector, final String typeTags) {

		final String address = "/irrelevant";
		final OSCMessage message = new OSCMessage(address);
		message.setInfo(new OSCMessageInfo(typeTags));
		return selector.matches(new OSCMessageEvent(this, null, message));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testNoArgs() {

		final MessageSelector matcher = new OSCPatternTypeTagsMessageSelector("");

		Assert.assertTrue( matches(matcher, ""));
		Assert.assertFalse(matches(matcher, "i"));
		Assert.assertFalse(matches(matcher, "t"));
		Assert.assertFalse(matches(matcher, "iiti"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testStar() {

		final MessageSelector matcher = new OSCPatternTypeTagsMessageSelector("*");

		Assert.assertTrue(matches(matcher, ""));
		Assert.assertTrue(matches(matcher, "i"));
		Assert.assertTrue(matches(matcher, "t"));
		Assert.assertTrue(matches(matcher, "iiti"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testPartPrefixes() {

		final MessageSelector matcher = new OSCPatternTypeTagsMessageSelector("i*f*");

		Assert.assertFalse(matches(matcher, "i"));
		Assert.assertFalse(matches(matcher, "it"));
		Assert.assertFalse(matches(matcher, "fi"));
		Assert.assertFalse(matches(matcher, "fit"));
		Assert.assertFalse(matches(matcher, "fti"));
		Assert.assertFalse(matches(matcher, "fiitiittt"));
		Assert.assertFalse(matches(matcher, "tif"));
		Assert.assertFalse(matches(matcher, "fif"));
		Assert.assertFalse(matches(matcher, "ffffif"));
		Assert.assertTrue( matches(matcher, "if"));
		Assert.assertTrue( matches(matcher, "itf"));
		Assert.assertTrue( matches(matcher, "ift"));
		Assert.assertTrue( matches(matcher, "iftf"));
		Assert.assertTrue( matches(matcher, "iiif"));
		Assert.assertTrue( matches(matcher, "ifff"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testPartPostfix() {

		final MessageSelector matcher = new OSCPatternTypeTagsMessageSelector("*i*f");

		Assert.assertFalse(matches(matcher, "i"));
		Assert.assertFalse(matches(matcher, "it"));
		Assert.assertFalse(matches(matcher, "fi"));
		Assert.assertFalse(matches(matcher, "fit"));
		Assert.assertFalse(matches(matcher, "fti"));
		Assert.assertFalse(matches(matcher, "fiitiittt"));
		Assert.assertTrue(matches(matcher, "tif"));
		Assert.assertTrue(matches(matcher, "fif"));
		Assert.assertTrue(matches(matcher, "ffffif"));
		Assert.assertTrue( matches(matcher, "if"));
		Assert.assertTrue( matches(matcher, "itf"));
		Assert.assertFalse(matches(matcher, "ift"));
		Assert.assertTrue( matches(matcher, "iftf"));
		Assert.assertTrue( matches(matcher, "iiif"));
		Assert.assertTrue( matches(matcher, "ifff"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testSingle() {

		final MessageSelector matcher = new OSCPatternTypeTagsMessageSelector("?i?f");

		Assert.assertTrue( matches(matcher, "iiff"));
		Assert.assertTrue( matches(matcher, "fiif"));
		Assert.assertTrue( matches(matcher, "titf"));
		Assert.assertTrue( matches(matcher, "zizf"));
		Assert.assertFalse(matches(matcher, "iff"));
		Assert.assertFalse(matches(matcher, "iif"));
		Assert.assertFalse(matches(matcher, "iiiff"));
		Assert.assertFalse(matches(matcher, "iifff"));
		Assert.assertFalse(matches(matcher, "iift"));
		Assert.assertFalse(matches(matcher, "itff"));
		Assert.assertFalse(matches(matcher, "iifi"));
		Assert.assertFalse(matches(matcher, "ifff"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testGroupForwards() {

		final MessageSelector matcher = new OSCPatternTypeTagsMessageSelector("i[f-i]f");

		Assert.assertTrue( matches(matcher, "iff"));
		Assert.assertTrue( matches(matcher, "iif"));
		Assert.assertFalse(matches(matcher, "if"));
		Assert.assertFalse(matches(matcher, "iiff"));
		Assert.assertFalse(matches(matcher, "izf"));
		Assert.assertFalse(matches(matcher, "tiif"));
		Assert.assertFalse(matches(matcher, "iift"));
		Assert.assertFalse(matches(matcher, "tiift"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testGroupBackwards() {

		final MessageSelector matcher = new OSCPatternTypeTagsMessageSelector("i[i-f]f");

		Assert.assertTrue( matches(matcher, "iff"));
		Assert.assertTrue( matches(matcher, "iif"));
		Assert.assertFalse(matches(matcher, "if"));
		Assert.assertFalse(matches(matcher, "iiff"));
		Assert.assertFalse(matches(matcher, "izf"));
		Assert.assertFalse(matches(matcher, "tiif"));
		Assert.assertFalse(matches(matcher, "iift"));
		Assert.assertFalse(matches(matcher, "tiift"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testGroupList() {

		final MessageSelector matcher = new OSCPatternTypeTagsMessageSelector("i[fz]f");

		Assert.assertTrue( matches(matcher, "iff"));
		Assert.assertFalse(matches(matcher, "iif"));
		Assert.assertFalse(matches(matcher, "if"));
		Assert.assertFalse(matches(matcher, "iiff"));
		Assert.assertTrue( matches(matcher, "izf"));
		Assert.assertFalse(matches(matcher, "tiff"));
		Assert.assertFalse(matches(matcher, "ifft"));
		Assert.assertFalse(matches(matcher, "tifft"));
		Assert.assertFalse(matches(matcher, "ifzf"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testGroupListAndRange() {

		final MessageSelector matcher = new OSCPatternTypeTagsMessageSelector("i[fi-z]f");

		Assert.assertTrue( matches(matcher, "iff"));
		Assert.assertTrue( matches(matcher, "iif"));
		Assert.assertFalse(matches(matcher, "if"));
		Assert.assertFalse(matches(matcher, "iiff"));
		Assert.assertTrue( matches(matcher, "izf"));
		Assert.assertFalse(matches(matcher, "tiff"));
		Assert.assertFalse(matches(matcher, "ifft"));
		Assert.assertFalse(matches(matcher, "tifft"));
		Assert.assertFalse(matches(matcher, "ifzf"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testGroupHyphenAndRange() {

		final MessageSelector matcher = new OSCPatternTypeTagsMessageSelector("i[-f-i]f");

		Assert.assertTrue( matches(matcher, "iff"));
		Assert.assertTrue( matches(matcher, "iif"));
		Assert.assertFalse(matches(matcher, "if"));
		Assert.assertFalse(matches(matcher, "iiff"));
		Assert.assertFalse(matches(matcher, "izf"));
		Assert.assertFalse(matches(matcher, "tiif"));
		Assert.assertFalse(matches(matcher, "iift"));
		Assert.assertFalse(matches(matcher, "tiift"));
		Assert.assertTrue( matches(matcher, "i-f"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testRegexNegate() {

		// '^' is just a character like any other, no special meaning!
		final MessageSelector matcher = new OSCPatternTypeTagsMessageSelector("i[^f-i]f");

		Assert.assertTrue( matches(matcher, "iff"));
		Assert.assertTrue( matches(matcher, "iif"));
		Assert.assertFalse(matches(matcher, "if"));
		Assert.assertFalse(matches(matcher, "iiff"));
		Assert.assertFalse(matches(matcher, "izf"));
		Assert.assertFalse(matches(matcher, "tiif"));
		Assert.assertFalse(matches(matcher, "iift"));
		Assert.assertFalse(matches(matcher, "tiift"));
		Assert.assertTrue( matches(matcher, "i^f"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testNegate() {

		final MessageSelector matcher = new OSCPatternTypeTagsMessageSelector("i[!f-i]f");

		Assert.assertFalse(matches(matcher, "iff"));
		Assert.assertFalse(matches(matcher, "iif"));
		Assert.assertFalse(matches(matcher, "if"));
		Assert.assertFalse(matches(matcher, "iiff"));
		Assert.assertTrue( matches(matcher, "izf"));
		Assert.assertFalse(matches(matcher, "tiif"));
		Assert.assertFalse(matches(matcher, "iift"));
		Assert.assertFalse(matches(matcher, "tiift"));
		Assert.assertTrue( matches(matcher, "i^f"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testStringGroupSolo() {

		final MessageSelector matcher = new OSCPatternTypeTagsMessageSelector("{if,tz}");

		Assert.assertTrue( matches(matcher, "if"));
		Assert.assertTrue( matches(matcher, "tz"));
		Assert.assertFalse(matches(matcher, "fi"));
		Assert.assertFalse(matches(matcher, "zt"));
		Assert.assertFalse(matches(matcher, "iz"));
		Assert.assertFalse(matches(matcher, "tf"));
		Assert.assertFalse(matches(matcher, "i^"));
		Assert.assertFalse(matches(matcher, "-z"));
		Assert.assertFalse(matches(matcher, "iff"));
		Assert.assertFalse(matches(matcher, "iif"));
		Assert.assertFalse(matches(matcher, "iiff"));
		Assert.assertFalse(matches(matcher, "izf"));
		Assert.assertFalse(matches(matcher, "tiif"));
		Assert.assertFalse(matches(matcher, "iift"));
		Assert.assertFalse(matches(matcher, "tiift"));
		Assert.assertFalse(matches(matcher, "i^f"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testStringGroupPrefix() {

		final MessageSelector matcher = new OSCPatternTypeTagsMessageSelector("i{if,tz}");

		Assert.assertTrue( matches(matcher, "iif"));
		Assert.assertTrue( matches(matcher, "itz"));
		Assert.assertFalse(matches(matcher, "ifi"));
		Assert.assertFalse(matches(matcher, "izt"));
		Assert.assertFalse(matches(matcher, "iiz"));
		Assert.assertFalse(matches(matcher, "itf"));
		Assert.assertFalse(matches(matcher, "ii^"));
		Assert.assertFalse(matches(matcher, "i-z"));
		Assert.assertFalse(matches(matcher, "iiff"));
		Assert.assertFalse(matches(matcher, "izf"));
		Assert.assertFalse(matches(matcher, "tiif"));
		Assert.assertFalse(matches(matcher, "iift"));
		Assert.assertFalse(matches(matcher, "tiift"));
		Assert.assertFalse(matches(matcher, "i^f"));
	}
}
