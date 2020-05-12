/*
 * Copyright (C) 2014, C. Ramakrishnan / Illposed Software.
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
import org.junit.Assert;
import org.junit.Test;

public class OSCPatternAddressMessageSelectorTest {

	private boolean matches(final MessageSelector matcher, final String address) {
		return matcher.matches(new OSCMessageEvent(this, null, new OSCMessage(address)));
	}

	@Test
	public void testNumberOfParts() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/*/*");

		Assert.assertFalse(matches(matcher, "/hello"));
		Assert.assertTrue( matches(matcher, "/hello/world"));
		Assert.assertFalse(matches(matcher, "/hello/world/two"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testPartPrefix() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/he*/wo*");

		Assert.assertFalse(matches(matcher, "/bello/world"));
		Assert.assertTrue( matches(matcher, "/hello/world"));
		Assert.assertFalse(matches(matcher, "/hello/universe"));
		Assert.assertTrue( matches(matcher, "/hells/worlds"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testPartPostfix() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/*o/*rld");

		Assert.assertTrue( matches(matcher, "/bello/world"));
		Assert.assertTrue( matches(matcher, "/hello/world"));
		Assert.assertFalse(matches(matcher, "/hello/universe"));
		Assert.assertFalse(matches(matcher, "/hells/worlds"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testPartPreAndPostfix() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/h*o/w*rld*");

		Assert.assertFalse(matches(matcher, "/bello/world"));
		Assert.assertTrue( matches(matcher, "/hello/world"));
		Assert.assertFalse(matches(matcher, "/hello/universe"));
		Assert.assertTrue( matches(matcher, "/heyo/worlds"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testSingle() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/?ello/w?rl?");

		Assert.assertTrue( matches(matcher, "/bello/world"));
		Assert.assertTrue( matches(matcher, "/hello/world"));
		Assert.assertFalse(matches(matcher, "/hello/universe"));
		Assert.assertFalse(matches(matcher, "/hello/worlds"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testGroupForwards() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/hel[a-z]o");

		Assert.assertTrue( matches(matcher, "/hello"));
		Assert.assertTrue( matches(matcher, "/helao"));
		Assert.assertTrue( matches(matcher, "/helzo"));
		Assert.assertFalse(matches(matcher, "/hellos"));
		Assert.assertFalse(matches(matcher, "/hel_o"));
		Assert.assertFalse(matches(matcher, "/helLo"));
		Assert.assertFalse(matches(matcher, "/helAo"));
		Assert.assertFalse(matches(matcher, "/helZo"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testGroupBackwards() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/hel[z-a]o");

		Assert.assertTrue( matches(matcher, "/helao"));
		Assert.assertTrue( matches(matcher, "/helzo"));
		Assert.assertFalse(matches(matcher, "/helo"));
		Assert.assertFalse(matches(matcher, "/helzzzzzo"));
		Assert.assertFalse(matches(matcher, "/hello"));
		Assert.assertFalse(matches(matcher, "/hellos"));
		Assert.assertFalse(matches(matcher, "/hel_o"));
		Assert.assertFalse(matches(matcher, "/helLo"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testGroupList() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/hel[aly]o");

		Assert.assertTrue( matches(matcher, "/helao"));
		Assert.assertTrue( matches(matcher, "/hello"));
		Assert.assertTrue( matches(matcher, "/helyo"));
		Assert.assertFalse(matches(matcher, "/helzo"));
		Assert.assertFalse(matches(matcher, "/hellllo"));
		Assert.assertFalse(matches(matcher, "/hellos"));
		Assert.assertFalse(matches(matcher, "/hel_o"));
		Assert.assertFalse(matches(matcher, "/helLo"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testGroupListAndRange() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/hel[lya-c]o");

		Assert.assertTrue( matches(matcher, "/helao"));
		Assert.assertTrue( matches(matcher, "/helbo"));
		Assert.assertTrue( matches(matcher, "/helco"));
		Assert.assertTrue( matches(matcher, "/hello"));
		Assert.assertTrue( matches(matcher, "/helyo"));
		Assert.assertFalse(matches(matcher, "/helzo"));
		Assert.assertFalse(matches(matcher, "/hellllo"));
		Assert.assertFalse(matches(matcher, "/hellos"));
		Assert.assertFalse(matches(matcher, "/hel_o"));
		Assert.assertFalse(matches(matcher, "/helLo"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testGroupHyphenAndRange() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/hel[-a-c]o");

		Assert.assertTrue( matches(matcher, "/hel-o"));
		Assert.assertTrue( matches(matcher, "/helao"));
		Assert.assertTrue( matches(matcher, "/helbo"));
		Assert.assertTrue( matches(matcher, "/helco"));
		Assert.assertFalse(matches(matcher, "/helzo"));
		Assert.assertFalse(matches(matcher, "/hellllo"));
		Assert.assertFalse(matches(matcher, "/hellos"));
		Assert.assertFalse(matches(matcher, "/hel_o"));
		Assert.assertFalse(matches(matcher, "/helLo"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testCloseBracketAndRange() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/hel[]a-c]o");

//		Assert.assertTrue( matches(matcher, "/hel]o")); // not a valid OSC address
		Assert.assertTrue( matches(matcher, "/helao"));
		Assert.assertTrue( matches(matcher, "/helbo"));
		Assert.assertTrue( matches(matcher, "/helco"));
		Assert.assertFalse(matches(matcher, "/helzo"));
		Assert.assertFalse(matches(matcher, "/hellllo"));
		Assert.assertFalse(matches(matcher, "/hellos"));
		Assert.assertFalse(matches(matcher, "/hel_o"));
		Assert.assertFalse(matches(matcher, "/helLo"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testRegexNegate() {

		// '^' is just a character like any other, no special meaning!
		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/hel[^a-c]o");

		Assert.assertTrue( matches(matcher, "/hel^o"));
		Assert.assertTrue( matches(matcher, "/helao"));
		Assert.assertTrue( matches(matcher, "/helbo"));
		Assert.assertTrue( matches(matcher, "/helco"));
		Assert.assertFalse(matches(matcher, "/hel-o"));
		Assert.assertFalse(matches(matcher, "/hello"));
		Assert.assertFalse(matches(matcher, "/helzo"));
		Assert.assertFalse(matches(matcher, "/hellllo"));
		Assert.assertFalse(matches(matcher, "/hellos"));
		Assert.assertFalse(matches(matcher, "/hel_o"));
		Assert.assertFalse(matches(matcher, "/helLo"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testNegate() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/hel[!a-c]o");

		Assert.assertFalse(matches(matcher, "/helao"));
		Assert.assertFalse(matches(matcher, "/helbo"));
		Assert.assertFalse(matches(matcher, "/helco"));
		Assert.assertTrue( matches(matcher, "/hel-o"));
		Assert.assertTrue( matches(matcher, "/hello"));
		Assert.assertTrue( matches(matcher, "/helzo"));
		Assert.assertFalse(matches(matcher, "/hellllo"));
		Assert.assertFalse(matches(matcher, "/hellos"));
		Assert.assertTrue( matches(matcher, "/hel_o"));
		Assert.assertTrue( matches(matcher, "/helLo"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testStringGroupSolo() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/{hello,hididelidoo}");

		Assert.assertTrue( matches(matcher, "/hello"));
		Assert.assertTrue( matches(matcher, "/hididelidoo"));
		Assert.assertFalse(matches(matcher, "/helco"));
		Assert.assertFalse(matches(matcher, "/hel-o"));
		Assert.assertFalse(matches(matcher, "/helzo"));
		Assert.assertFalse(matches(matcher, "/hellllo"));
		Assert.assertFalse(matches(matcher, "/hellos"));
		Assert.assertFalse(matches(matcher, "/hel_o"));
		Assert.assertFalse(matches(matcher, "/helLo"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testStringGroupPrefix() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/h{ello,ididelidoo}");

		Assert.assertTrue( matches(matcher, "/hello"));
		Assert.assertTrue( matches(matcher, "/hididelidoo"));
		Assert.assertFalse(matches(matcher, "/helco"));
		Assert.assertFalse(matches(matcher, "/hel-o"));
		Assert.assertFalse(matches(matcher, "/helzo"));
		Assert.assertFalse(matches(matcher, "/hellllo"));
		Assert.assertFalse(matches(matcher, "/hellos"));
		Assert.assertFalse(matches(matcher, "/hel_o"));
		Assert.assertFalse(matches(matcher, "/helLo"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testStringGroupPostfix() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/{hell,hididelido}o");

		Assert.assertTrue( matches(matcher, "/hello"));
		Assert.assertTrue( matches(matcher, "/hididelidoo"));
		Assert.assertFalse(matches(matcher, "/helco"));
		Assert.assertFalse(matches(matcher, "/hel-o"));
		Assert.assertFalse(matches(matcher, "/helzo"));
		Assert.assertFalse(matches(matcher, "/hellllo"));
		Assert.assertFalse(matches(matcher, "/hellos"));
		Assert.assertFalse(matches(matcher, "/hel_o"));
		Assert.assertFalse(matches(matcher, "/helLo"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testStringGroupPreAndPostfix() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/h{ell,ididelido}o");

		Assert.assertTrue( matches(matcher, "/hello"));
		Assert.assertTrue( matches(matcher, "/hididelidoo"));
		Assert.assertFalse(matches(matcher, "/helco"));
		Assert.assertFalse(matches(matcher, "/hel-o"));
		Assert.assertFalse(matches(matcher, "/helzo"));
		Assert.assertFalse(matches(matcher, "/hellllo"));
		Assert.assertFalse(matches(matcher, "/hellos"));
		Assert.assertFalse(matches(matcher, "/hel_o"));
		Assert.assertFalse(matches(matcher, "/helLo"));
	}

	@Test
	public void testPathTraversingWildcardNone() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/");

		Assert.assertTrue( matches(matcher, "/"));
		Assert.assertFalse(matches(matcher, "/hello"));
		Assert.assertFalse(matches(matcher, "/hello/world"));
		Assert.assertFalse(matches(matcher, "/hello/world/two"));
	}

	@Test
	public void testPathTraversingWildcardAll() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("//");

		Assert.assertTrue( matches(matcher, "/"));
		Assert.assertTrue( matches(matcher, "/hello"));
		Assert.assertTrue( matches(matcher, "/hello/world"));
		Assert.assertTrue( matches(matcher, "/hello/world/two"));
	}

	@Test
	public void testPathTraversingWildcardPrefix() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/hello//");

		Assert.assertTrue( matches(matcher, "/hello"));
		Assert.assertTrue( matches(matcher, "/hello/world"));
		Assert.assertTrue( matches(matcher, "/hello/world/two"));
		Assert.assertFalse(matches(matcher, "/bye"));
		Assert.assertFalse(matches(matcher, "/bye/world"));
		Assert.assertFalse(matches(matcher, "/bye/world/two"));
	}

	@Test
	public void testPathTraversingWildcardPostfix() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("//two");

		Assert.assertFalse(matches(matcher, "/hello"));
		Assert.assertFalse(matches(matcher, "/hello/world"));
		Assert.assertTrue( matches(matcher, "/hello/world/two"));
		Assert.assertFalse(matches(matcher, "/bye"));
		Assert.assertFalse(matches(matcher, "/bye/world"));
		Assert.assertTrue( matches(matcher, "/bye/world/two"));
	}

	@Test
	public void testPathTraversingWildcardInfix() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("//world//");

		Assert.assertTrue( matches(matcher, "/world"));
		Assert.assertFalse(matches(matcher, "/hello"));
		Assert.assertTrue( matches(matcher, "/hello/world"));
		Assert.assertTrue( matches(matcher, "/hello/world/two"));
		Assert.assertFalse(matches(matcher, "/bye"));
		Assert.assertTrue( matches(matcher, "/bye/world"));
		Assert.assertTrue( matches(matcher, "/bye/world/two"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testPathTraversingWildcardAroundfix() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/hello//two");

		Assert.assertFalse(matches(matcher, "/world"));
		Assert.assertFalse(matches(matcher, "/hello"));
		Assert.assertFalse(matches(matcher, "/hello/world"));
		Assert.assertTrue( matches(matcher, "/hello/world/two"));
		Assert.assertTrue( matches(matcher, "/hello/my/sweet/world/two"));
		Assert.assertTrue( matches(matcher, "/hello/universe/two"));
		Assert.assertFalse(matches(matcher, "/bye"));
		Assert.assertFalse(matches(matcher, "/bye/world"));
		Assert.assertFalse(matches(matcher, "/bye/world/two"));
	}

	@Test
	public void testPathTraversingWildcardMultiplePrefix() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/hello////");

		Assert.assertTrue( matches(matcher, "/hello"));
		Assert.assertTrue( matches(matcher, "/hello/world"));
		Assert.assertTrue( matches(matcher, "/hello/world/two"));
		Assert.assertFalse(matches(matcher, "/bye"));
		Assert.assertFalse(matches(matcher, "/bye/world"));
		Assert.assertFalse(matches(matcher, "/bye/world/two"));
	}

	@Test
	public void testPathTraversingWildcardMultiplePostfix() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("////two");

		Assert.assertFalse(matches(matcher, "/hello"));
		Assert.assertFalse(matches(matcher, "/hello/world"));
		Assert.assertTrue( matches(matcher, "/hello/world/two"));
		Assert.assertFalse(matches(matcher, "/bye"));
		Assert.assertFalse(matches(matcher, "/bye/world"));
		Assert.assertTrue( matches(matcher, "/bye/world/two"));
	}

	@Test
	public void testPathTraversingWildcardMultipleInfix() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("////world////");

		Assert.assertTrue( matches(matcher, "/world"));
		Assert.assertFalse(matches(matcher, "/hello"));
		Assert.assertTrue( matches(matcher, "/hello/world"));
		Assert.assertTrue( matches(matcher, "/hello/world/two"));
		Assert.assertFalse(matches(matcher, "/bye"));
		Assert.assertTrue( matches(matcher, "/bye/world"));
		Assert.assertTrue( matches(matcher, "/bye/world/two"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testPathTraversingWildcardMultipleAroundfix() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/hello////two");

		Assert.assertFalse(matches(matcher, "/world"));
		Assert.assertFalse(matches(matcher, "/hello"));
		Assert.assertFalse(matches(matcher, "/hello/world"));
		Assert.assertTrue( matches(matcher, "/hello/world/two"));
		Assert.assertTrue( matches(matcher, "/hello/my/sweet/world/two"));
		Assert.assertTrue( matches(matcher, "/hello/universe/two"));
		Assert.assertFalse(matches(matcher, "/bye"));
		Assert.assertFalse(matches(matcher, "/bye/world"));
		Assert.assertFalse(matches(matcher, "/bye/world/two"));
	}

	@Test
	public void testPathTraversingWildcardNoneSingleTrailingSlash() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/hello/");

		Assert.assertTrue( matches(matcher, "/hello"));
		Assert.assertFalse(matches(matcher, "/hello/world"));
		Assert.assertFalse(matches(matcher, "/hello/world/two"));
		Assert.assertFalse(matches(matcher, "/bye"));
		Assert.assertFalse(matches(matcher, "/bye/world"));
		Assert.assertFalse(matches(matcher, "/bye/world/two"));
	}

	@Test
	public void testSimple() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/hello");

		Assert.assertTrue( matches(matcher, "/hello"));
		Assert.assertFalse(matches(matcher, "/hello/world"));
		Assert.assertFalse(matches(matcher, "/hello/world/two"));
		Assert.assertFalse(matches(matcher, "/bye"));
		Assert.assertFalse(matches(matcher, "/bye/world"));
		Assert.assertFalse(matches(matcher, "/bye/world/two"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testPathTraversingWildcardComplex() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/my//hello///two/cents//");

		Assert.assertFalse(matches(matcher, "/hello"));
		Assert.assertFalse(matches(matcher, "/hello/world"));
		Assert.assertFalse(matches(matcher, "/hello/world/two"));
		Assert.assertFalse(matches(matcher, "/bye"));
		Assert.assertFalse(matches(matcher, "/bye/world"));
		Assert.assertFalse(matches(matcher, "/bye/world/two"));
		Assert.assertTrue( matches(matcher, "/my/hello/two/cents"));
		Assert.assertTrue( matches(matcher, "/my/few/cents/hello/two/cents"));
		Assert.assertTrue( matches(matcher, "/my/few/cents/hello/thats/two/cents"));
		Assert.assertTrue( matches(matcher, "/my/few/cents/hello/thats/two/cents/too"));
		Assert.assertTrue( matches(matcher, "/my/few/cents/hello/thats/two/or/three/no/two/cents"));
		Assert.assertTrue( matches(matcher, "/my/few/cents/hello/thats/two/or/three/no/two/cents/too"));
		Assert.assertFalse(matches(matcher, "/my/few/cents/hello/thats/two/or/three/no/two/bad/cents/too"));
	}
}
