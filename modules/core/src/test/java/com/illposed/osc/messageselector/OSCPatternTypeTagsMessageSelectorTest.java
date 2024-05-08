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

		Assertions.assertTrue( matches(matcher, ""));
		Assertions.assertFalse(matches(matcher, "i"));
		Assertions.assertFalse(matches(matcher, "t"));
		Assertions.assertFalse(matches(matcher, "iiti"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testStar() {

		final MessageSelector matcher = new OSCPatternTypeTagsMessageSelector("*");

		Assertions.assertTrue(matches(matcher, ""));
		Assertions.assertTrue(matches(matcher, "i"));
		Assertions.assertTrue(matches(matcher, "t"));
		Assertions.assertTrue(matches(matcher, "iiti"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testPartPrefixes() {

		final MessageSelector matcher = new OSCPatternTypeTagsMessageSelector("i*f*");

		Assertions.assertFalse(matches(matcher, "i"));
		Assertions.assertFalse(matches(matcher, "it"));
		Assertions.assertFalse(matches(matcher, "fi"));
		Assertions.assertFalse(matches(matcher, "fit"));
		Assertions.assertFalse(matches(matcher, "fti"));
		Assertions.assertFalse(matches(matcher, "fiitiittt"));
		Assertions.assertFalse(matches(matcher, "tif"));
		Assertions.assertFalse(matches(matcher, "fif"));
		Assertions.assertFalse(matches(matcher, "ffffif"));
		Assertions.assertTrue( matches(matcher, "if"));
		Assertions.assertTrue( matches(matcher, "itf"));
		Assertions.assertTrue( matches(matcher, "ift"));
		Assertions.assertTrue( matches(matcher, "iftf"));
		Assertions.assertTrue( matches(matcher, "iiif"));
		Assertions.assertTrue( matches(matcher, "ifff"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testPartPostfix() {

		final MessageSelector matcher = new OSCPatternTypeTagsMessageSelector("*i*f");

		Assertions.assertFalse(matches(matcher, "i"));
		Assertions.assertFalse(matches(matcher, "it"));
		Assertions.assertFalse(matches(matcher, "fi"));
		Assertions.assertFalse(matches(matcher, "fit"));
		Assertions.assertFalse(matches(matcher, "fti"));
		Assertions.assertFalse(matches(matcher, "fiitiittt"));
		Assertions.assertTrue(matches(matcher, "tif"));
		Assertions.assertTrue(matches(matcher, "fif"));
		Assertions.assertTrue(matches(matcher, "ffffif"));
		Assertions.assertTrue( matches(matcher, "if"));
		Assertions.assertTrue( matches(matcher, "itf"));
		Assertions.assertFalse(matches(matcher, "ift"));
		Assertions.assertTrue( matches(matcher, "iftf"));
		Assertions.assertTrue( matches(matcher, "iiif"));
		Assertions.assertTrue( matches(matcher, "ifff"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testSingle() {

		final MessageSelector matcher = new OSCPatternTypeTagsMessageSelector("?i?f");

		Assertions.assertTrue( matches(matcher, "iiff"));
		Assertions.assertTrue( matches(matcher, "fiif"));
		Assertions.assertTrue( matches(matcher, "titf"));
		Assertions.assertTrue( matches(matcher, "zizf"));
		Assertions.assertFalse(matches(matcher, "iff"));
		Assertions.assertFalse(matches(matcher, "iif"));
		Assertions.assertFalse(matches(matcher, "iiiff"));
		Assertions.assertFalse(matches(matcher, "iifff"));
		Assertions.assertFalse(matches(matcher, "iift"));
		Assertions.assertFalse(matches(matcher, "itff"));
		Assertions.assertFalse(matches(matcher, "iifi"));
		Assertions.assertFalse(matches(matcher, "ifff"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testGroupForwards() {

		final MessageSelector matcher = new OSCPatternTypeTagsMessageSelector("i[f-i]f");

		Assertions.assertTrue( matches(matcher, "iff"));
		Assertions.assertTrue( matches(matcher, "iif"));
		Assertions.assertFalse(matches(matcher, "if"));
		Assertions.assertFalse(matches(matcher, "iiff"));
		Assertions.assertFalse(matches(matcher, "izf"));
		Assertions.assertFalse(matches(matcher, "tiif"));
		Assertions.assertFalse(matches(matcher, "iift"));
		Assertions.assertFalse(matches(matcher, "tiift"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testGroupBackwards() {

		final MessageSelector matcher = new OSCPatternTypeTagsMessageSelector("i[i-f]f");

		Assertions.assertTrue( matches(matcher, "iff"));
		Assertions.assertTrue( matches(matcher, "iif"));
		Assertions.assertFalse(matches(matcher, "if"));
		Assertions.assertFalse(matches(matcher, "iiff"));
		Assertions.assertFalse(matches(matcher, "izf"));
		Assertions.assertFalse(matches(matcher, "tiif"));
		Assertions.assertFalse(matches(matcher, "iift"));
		Assertions.assertFalse(matches(matcher, "tiift"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testGroupList() {

		final MessageSelector matcher = new OSCPatternTypeTagsMessageSelector("i[fz]f");

		Assertions.assertTrue( matches(matcher, "iff"));
		Assertions.assertFalse(matches(matcher, "iif"));
		Assertions.assertFalse(matches(matcher, "if"));
		Assertions.assertFalse(matches(matcher, "iiff"));
		Assertions.assertTrue( matches(matcher, "izf"));
		Assertions.assertFalse(matches(matcher, "tiff"));
		Assertions.assertFalse(matches(matcher, "ifft"));
		Assertions.assertFalse(matches(matcher, "tifft"));
		Assertions.assertFalse(matches(matcher, "ifzf"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testGroupListAndRange() {

		final MessageSelector matcher = new OSCPatternTypeTagsMessageSelector("i[fi-z]f");

		Assertions.assertTrue( matches(matcher, "iff"));
		Assertions.assertTrue( matches(matcher, "iif"));
		Assertions.assertFalse(matches(matcher, "if"));
		Assertions.assertFalse(matches(matcher, "iiff"));
		Assertions.assertTrue( matches(matcher, "izf"));
		Assertions.assertFalse(matches(matcher, "tiff"));
		Assertions.assertFalse(matches(matcher, "ifft"));
		Assertions.assertFalse(matches(matcher, "tifft"));
		Assertions.assertFalse(matches(matcher, "ifzf"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testGroupHyphenAndRange() {

		final MessageSelector matcher = new OSCPatternTypeTagsMessageSelector("i[-f-i]f");

		Assertions.assertTrue( matches(matcher, "iff"));
		Assertions.assertTrue( matches(matcher, "iif"));
		Assertions.assertFalse(matches(matcher, "if"));
		Assertions.assertFalse(matches(matcher, "iiff"));
		Assertions.assertFalse(matches(matcher, "izf"));
		Assertions.assertFalse(matches(matcher, "tiif"));
		Assertions.assertFalse(matches(matcher, "iift"));
		Assertions.assertFalse(matches(matcher, "tiift"));
		Assertions.assertTrue( matches(matcher, "i-f"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testRegexNegate() {

		// '^' is just a character like any other, no special meaning!
		final MessageSelector matcher = new OSCPatternTypeTagsMessageSelector("i[^f-i]f");

		Assertions.assertTrue( matches(matcher, "iff"));
		Assertions.assertTrue( matches(matcher, "iif"));
		Assertions.assertFalse(matches(matcher, "if"));
		Assertions.assertFalse(matches(matcher, "iiff"));
		Assertions.assertFalse(matches(matcher, "izf"));
		Assertions.assertFalse(matches(matcher, "tiif"));
		Assertions.assertFalse(matches(matcher, "iift"));
		Assertions.assertFalse(matches(matcher, "tiift"));
		Assertions.assertTrue( matches(matcher, "i^f"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testNegate() {

		final MessageSelector matcher = new OSCPatternTypeTagsMessageSelector("i[!f-i]f");

		Assertions.assertFalse(matches(matcher, "iff"));
		Assertions.assertFalse(matches(matcher, "iif"));
		Assertions.assertFalse(matches(matcher, "if"));
		Assertions.assertFalse(matches(matcher, "iiff"));
		Assertions.assertTrue( matches(matcher, "izf"));
		Assertions.assertFalse(matches(matcher, "tiif"));
		Assertions.assertFalse(matches(matcher, "iift"));
		Assertions.assertFalse(matches(matcher, "tiift"));
		Assertions.assertTrue( matches(matcher, "i^f"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testStringGroupSolo() {

		final MessageSelector matcher = new OSCPatternTypeTagsMessageSelector("{if,tz}");

		Assertions.assertTrue( matches(matcher, "if"));
		Assertions.assertTrue( matches(matcher, "tz"));
		Assertions.assertFalse(matches(matcher, "fi"));
		Assertions.assertFalse(matches(matcher, "zt"));
		Assertions.assertFalse(matches(matcher, "iz"));
		Assertions.assertFalse(matches(matcher, "tf"));
		Assertions.assertFalse(matches(matcher, "i^"));
		Assertions.assertFalse(matches(matcher, "-z"));
		Assertions.assertFalse(matches(matcher, "iff"));
		Assertions.assertFalse(matches(matcher, "iif"));
		Assertions.assertFalse(matches(matcher, "iiff"));
		Assertions.assertFalse(matches(matcher, "izf"));
		Assertions.assertFalse(matches(matcher, "tiif"));
		Assertions.assertFalse(matches(matcher, "iift"));
		Assertions.assertFalse(matches(matcher, "tiift"));
		Assertions.assertFalse(matches(matcher, "i^f"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testStringGroupPrefix() {

		final MessageSelector matcher = new OSCPatternTypeTagsMessageSelector("i{if,tz}");

		Assertions.assertTrue( matches(matcher, "iif"));
		Assertions.assertTrue( matches(matcher, "itz"));
		Assertions.assertFalse(matches(matcher, "ifi"));
		Assertions.assertFalse(matches(matcher, "izt"));
		Assertions.assertFalse(matches(matcher, "iiz"));
		Assertions.assertFalse(matches(matcher, "itf"));
		Assertions.assertFalse(matches(matcher, "ii^"));
		Assertions.assertFalse(matches(matcher, "i-z"));
		Assertions.assertFalse(matches(matcher, "iiff"));
		Assertions.assertFalse(matches(matcher, "izf"));
		Assertions.assertFalse(matches(matcher, "tiif"));
		Assertions.assertFalse(matches(matcher, "iift"));
		Assertions.assertFalse(matches(matcher, "tiift"));
		Assertions.assertFalse(matches(matcher, "i^f"));
	}
}
