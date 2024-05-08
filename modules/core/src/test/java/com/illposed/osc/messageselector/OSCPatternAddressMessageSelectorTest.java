// SPDX-FileCopyrightText: 2014 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc.messageselector;

import com.illposed.osc.MessageSelector;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCMessageEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OSCPatternAddressMessageSelectorTest {

	private boolean matches(final MessageSelector matcher, final String address) {
		return matcher.matches(new OSCMessageEvent(this, null, new OSCMessage(address)));
	}

	@Test
	public void testNumberOfParts() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/*/*");

		Assertions.assertFalse(matches(matcher, "/hello"));
		Assertions.assertTrue( matches(matcher, "/hello/world"));
		Assertions.assertFalse(matches(matcher, "/hello/world/two"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testPartPrefix() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/he*/wo*");

		Assertions.assertFalse(matches(matcher, "/bello/world"));
		Assertions.assertTrue( matches(matcher, "/hello/world"));
		Assertions.assertFalse(matches(matcher, "/hello/universe"));
		Assertions.assertTrue( matches(matcher, "/hells/worlds"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testPartPostfix() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/*o/*rld");

		Assertions.assertTrue( matches(matcher, "/bello/world"));
		Assertions.assertTrue( matches(matcher, "/hello/world"));
		Assertions.assertFalse(matches(matcher, "/hello/universe"));
		Assertions.assertFalse(matches(matcher, "/hells/worlds"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testPartPreAndPostfix() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/h*o/w*rld*");

		Assertions.assertFalse(matches(matcher, "/bello/world"));
		Assertions.assertTrue( matches(matcher, "/hello/world"));
		Assertions.assertFalse(matches(matcher, "/hello/universe"));
		Assertions.assertTrue( matches(matcher, "/heyo/worlds"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testSingle() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/?ello/w?rl?");

		Assertions.assertTrue( matches(matcher, "/bello/world"));
		Assertions.assertTrue( matches(matcher, "/hello/world"));
		Assertions.assertFalse(matches(matcher, "/hello/universe"));
		Assertions.assertFalse(matches(matcher, "/hello/worlds"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testGroupForwards() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/hel[a-z]o");

		Assertions.assertTrue( matches(matcher, "/hello"));
		Assertions.assertTrue( matches(matcher, "/helao"));
		Assertions.assertTrue( matches(matcher, "/helzo"));
		Assertions.assertFalse(matches(matcher, "/hellos"));
		Assertions.assertFalse(matches(matcher, "/hel_o"));
		Assertions.assertFalse(matches(matcher, "/helLo"));
		Assertions.assertFalse(matches(matcher, "/helAo"));
		Assertions.assertFalse(matches(matcher, "/helZo"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testGroupBackwards() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/hel[z-a]o");

		Assertions.assertTrue( matches(matcher, "/helao"));
		Assertions.assertTrue( matches(matcher, "/helzo"));
		Assertions.assertFalse(matches(matcher, "/helo"));
		Assertions.assertFalse(matches(matcher, "/helzzzzzo"));
		Assertions.assertFalse(matches(matcher, "/hello"));
		Assertions.assertFalse(matches(matcher, "/hellos"));
		Assertions.assertFalse(matches(matcher, "/hel_o"));
		Assertions.assertFalse(matches(matcher, "/helLo"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testGroupList() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/hel[aly]o");

		Assertions.assertTrue( matches(matcher, "/helao"));
		Assertions.assertTrue( matches(matcher, "/hello"));
		Assertions.assertTrue( matches(matcher, "/helyo"));
		Assertions.assertFalse(matches(matcher, "/helzo"));
		Assertions.assertFalse(matches(matcher, "/hellllo"));
		Assertions.assertFalse(matches(matcher, "/hellos"));
		Assertions.assertFalse(matches(matcher, "/hel_o"));
		Assertions.assertFalse(matches(matcher, "/helLo"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testGroupListAndRange() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/hel[lya-c]o");

		Assertions.assertTrue( matches(matcher, "/helao"));
		Assertions.assertTrue( matches(matcher, "/helbo"));
		Assertions.assertTrue( matches(matcher, "/helco"));
		Assertions.assertTrue( matches(matcher, "/hello"));
		Assertions.assertTrue( matches(matcher, "/helyo"));
		Assertions.assertFalse(matches(matcher, "/helzo"));
		Assertions.assertFalse(matches(matcher, "/hellllo"));
		Assertions.assertFalse(matches(matcher, "/hellos"));
		Assertions.assertFalse(matches(matcher, "/hel_o"));
		Assertions.assertFalse(matches(matcher, "/helLo"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testGroupHyphenAndRange() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/hel[-a-c]o");

		Assertions.assertTrue( matches(matcher, "/hel-o"));
		Assertions.assertTrue( matches(matcher, "/helao"));
		Assertions.assertTrue( matches(matcher, "/helbo"));
		Assertions.assertTrue( matches(matcher, "/helco"));
		Assertions.assertFalse(matches(matcher, "/helzo"));
		Assertions.assertFalse(matches(matcher, "/hellllo"));
		Assertions.assertFalse(matches(matcher, "/hellos"));
		Assertions.assertFalse(matches(matcher, "/hel_o"));
		Assertions.assertFalse(matches(matcher, "/helLo"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testCloseBracketAndRange() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/hel[]a-c]o");

//		Assertions.assertTrue( matches(matcher, "/hel]o")); // not a valid OSC address
		Assertions.assertTrue( matches(matcher, "/helao"));
		Assertions.assertTrue( matches(matcher, "/helbo"));
		Assertions.assertTrue( matches(matcher, "/helco"));
		Assertions.assertFalse(matches(matcher, "/helzo"));
		Assertions.assertFalse(matches(matcher, "/hellllo"));
		Assertions.assertFalse(matches(matcher, "/hellos"));
		Assertions.assertFalse(matches(matcher, "/hel_o"));
		Assertions.assertFalse(matches(matcher, "/helLo"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testRegexNegate() {

		// '^' is just a character like any other, no special meaning!
		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/hel[^a-c]o");

		Assertions.assertTrue( matches(matcher, "/hel^o"));
		Assertions.assertTrue( matches(matcher, "/helao"));
		Assertions.assertTrue( matches(matcher, "/helbo"));
		Assertions.assertTrue( matches(matcher, "/helco"));
		Assertions.assertFalse(matches(matcher, "/hel-o"));
		Assertions.assertFalse(matches(matcher, "/hello"));
		Assertions.assertFalse(matches(matcher, "/helzo"));
		Assertions.assertFalse(matches(matcher, "/hellllo"));
		Assertions.assertFalse(matches(matcher, "/hellos"));
		Assertions.assertFalse(matches(matcher, "/hel_o"));
		Assertions.assertFalse(matches(matcher, "/helLo"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testNegate() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/hel[!a-c]o");

		Assertions.assertFalse(matches(matcher, "/helao"));
		Assertions.assertFalse(matches(matcher, "/helbo"));
		Assertions.assertFalse(matches(matcher, "/helco"));
		Assertions.assertTrue( matches(matcher, "/hel-o"));
		Assertions.assertTrue( matches(matcher, "/hello"));
		Assertions.assertTrue( matches(matcher, "/helzo"));
		Assertions.assertFalse(matches(matcher, "/hellllo"));
		Assertions.assertFalse(matches(matcher, "/hellos"));
		Assertions.assertTrue( matches(matcher, "/hel_o"));
		Assertions.assertTrue( matches(matcher, "/helLo"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testStringGroupSolo() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/{hello,hididelidoo}");

		Assertions.assertTrue( matches(matcher, "/hello"));
		Assertions.assertTrue( matches(matcher, "/hididelidoo"));
		Assertions.assertFalse(matches(matcher, "/helco"));
		Assertions.assertFalse(matches(matcher, "/hel-o"));
		Assertions.assertFalse(matches(matcher, "/helzo"));
		Assertions.assertFalse(matches(matcher, "/hellllo"));
		Assertions.assertFalse(matches(matcher, "/hellos"));
		Assertions.assertFalse(matches(matcher, "/hel_o"));
		Assertions.assertFalse(matches(matcher, "/helLo"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testStringGroupPrefix() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/h{ello,ididelidoo}");

		Assertions.assertTrue( matches(matcher, "/hello"));
		Assertions.assertTrue( matches(matcher, "/hididelidoo"));
		Assertions.assertFalse(matches(matcher, "/helco"));
		Assertions.assertFalse(matches(matcher, "/hel-o"));
		Assertions.assertFalse(matches(matcher, "/helzo"));
		Assertions.assertFalse(matches(matcher, "/hellllo"));
		Assertions.assertFalse(matches(matcher, "/hellos"));
		Assertions.assertFalse(matches(matcher, "/hel_o"));
		Assertions.assertFalse(matches(matcher, "/helLo"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testStringGroupPostfix() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/{hell,hididelido}o");

		Assertions.assertTrue( matches(matcher, "/hello"));
		Assertions.assertTrue( matches(matcher, "/hididelidoo"));
		Assertions.assertFalse(matches(matcher, "/helco"));
		Assertions.assertFalse(matches(matcher, "/hel-o"));
		Assertions.assertFalse(matches(matcher, "/helzo"));
		Assertions.assertFalse(matches(matcher, "/hellllo"));
		Assertions.assertFalse(matches(matcher, "/hellos"));
		Assertions.assertFalse(matches(matcher, "/hel_o"));
		Assertions.assertFalse(matches(matcher, "/helLo"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testStringGroupPreAndPostfix() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/h{ell,ididelido}o");

		Assertions.assertTrue( matches(matcher, "/hello"));
		Assertions.assertTrue( matches(matcher, "/hididelidoo"));
		Assertions.assertFalse(matches(matcher, "/helco"));
		Assertions.assertFalse(matches(matcher, "/hel-o"));
		Assertions.assertFalse(matches(matcher, "/helzo"));
		Assertions.assertFalse(matches(matcher, "/hellllo"));
		Assertions.assertFalse(matches(matcher, "/hellos"));
		Assertions.assertFalse(matches(matcher, "/hel_o"));
		Assertions.assertFalse(matches(matcher, "/helLo"));
	}

	@Test
	public void testPathTraversingWildcardNone() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/");

		Assertions.assertTrue( matches(matcher, "/"));
		Assertions.assertFalse(matches(matcher, "/hello"));
		Assertions.assertFalse(matches(matcher, "/hello/world"));
		Assertions.assertFalse(matches(matcher, "/hello/world/two"));
	}

	@Test
	public void testPathTraversingWildcardAll() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("//");

		Assertions.assertTrue( matches(matcher, "/"));
		Assertions.assertTrue( matches(matcher, "/hello"));
		Assertions.assertTrue( matches(matcher, "/hello/world"));
		Assertions.assertTrue( matches(matcher, "/hello/world/two"));
	}

	@Test
	public void testPathTraversingWildcardPrefix() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/hello//");

		Assertions.assertTrue( matches(matcher, "/hello"));
		Assertions.assertTrue( matches(matcher, "/hello/world"));
		Assertions.assertTrue( matches(matcher, "/hello/world/two"));
		Assertions.assertFalse(matches(matcher, "/bye"));
		Assertions.assertFalse(matches(matcher, "/bye/world"));
		Assertions.assertFalse(matches(matcher, "/bye/world/two"));
	}

	@Test
	public void testPathTraversingWildcardPostfix() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("//two");

		Assertions.assertFalse(matches(matcher, "/hello"));
		Assertions.assertFalse(matches(matcher, "/hello/world"));
		Assertions.assertTrue( matches(matcher, "/hello/world/two"));
		Assertions.assertFalse(matches(matcher, "/bye"));
		Assertions.assertFalse(matches(matcher, "/bye/world"));
		Assertions.assertTrue( matches(matcher, "/bye/world/two"));
	}

	@Test
	public void testPathTraversingWildcardInfix() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("//world//");

		Assertions.assertTrue( matches(matcher, "/world"));
		Assertions.assertFalse(matches(matcher, "/hello"));
		Assertions.assertTrue( matches(matcher, "/hello/world"));
		Assertions.assertTrue( matches(matcher, "/hello/world/two"));
		Assertions.assertFalse(matches(matcher, "/bye"));
		Assertions.assertTrue( matches(matcher, "/bye/world"));
		Assertions.assertTrue( matches(matcher, "/bye/world/two"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testPathTraversingWildcardAroundfix() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/hello//two");

		Assertions.assertFalse(matches(matcher, "/world"));
		Assertions.assertFalse(matches(matcher, "/hello"));
		Assertions.assertFalse(matches(matcher, "/hello/world"));
		Assertions.assertTrue( matches(matcher, "/hello/world/two"));
		Assertions.assertTrue( matches(matcher, "/hello/my/sweet/world/two"));
		Assertions.assertTrue( matches(matcher, "/hello/universe/two"));
		Assertions.assertFalse(matches(matcher, "/bye"));
		Assertions.assertFalse(matches(matcher, "/bye/world"));
		Assertions.assertFalse(matches(matcher, "/bye/world/two"));
	}

	@Test
	public void testPathTraversingWildcardMultiplePrefix() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/hello////");

		Assertions.assertTrue( matches(matcher, "/hello"));
		Assertions.assertTrue( matches(matcher, "/hello/world"));
		Assertions.assertTrue( matches(matcher, "/hello/world/two"));
		Assertions.assertFalse(matches(matcher, "/bye"));
		Assertions.assertFalse(matches(matcher, "/bye/world"));
		Assertions.assertFalse(matches(matcher, "/bye/world/two"));
	}

	@Test
	public void testPathTraversingWildcardMultiplePostfix() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("////two");

		Assertions.assertFalse(matches(matcher, "/hello"));
		Assertions.assertFalse(matches(matcher, "/hello/world"));
		Assertions.assertTrue( matches(matcher, "/hello/world/two"));
		Assertions.assertFalse(matches(matcher, "/bye"));
		Assertions.assertFalse(matches(matcher, "/bye/world"));
		Assertions.assertTrue( matches(matcher, "/bye/world/two"));
	}

	@Test
	public void testPathTraversingWildcardMultipleInfix() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("////world////");

		Assertions.assertTrue( matches(matcher, "/world"));
		Assertions.assertFalse(matches(matcher, "/hello"));
		Assertions.assertTrue( matches(matcher, "/hello/world"));
		Assertions.assertTrue( matches(matcher, "/hello/world/two"));
		Assertions.assertFalse(matches(matcher, "/bye"));
		Assertions.assertTrue( matches(matcher, "/bye/world"));
		Assertions.assertTrue( matches(matcher, "/bye/world/two"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testPathTraversingWildcardMultipleAroundfix() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/hello////two");

		Assertions.assertFalse(matches(matcher, "/world"));
		Assertions.assertFalse(matches(matcher, "/hello"));
		Assertions.assertFalse(matches(matcher, "/hello/world"));
		Assertions.assertTrue( matches(matcher, "/hello/world/two"));
		Assertions.assertTrue( matches(matcher, "/hello/my/sweet/world/two"));
		Assertions.assertTrue( matches(matcher, "/hello/universe/two"));
		Assertions.assertFalse(matches(matcher, "/bye"));
		Assertions.assertFalse(matches(matcher, "/bye/world"));
		Assertions.assertFalse(matches(matcher, "/bye/world/two"));
	}

	@Test
	public void testPathTraversingWildcardNoneSingleTrailingSlash() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/hello/");

		Assertions.assertTrue( matches(matcher, "/hello"));
		Assertions.assertFalse(matches(matcher, "/hello/world"));
		Assertions.assertFalse(matches(matcher, "/hello/world/two"));
		Assertions.assertFalse(matches(matcher, "/bye"));
		Assertions.assertFalse(matches(matcher, "/bye/world"));
		Assertions.assertFalse(matches(matcher, "/bye/world/two"));
	}

	@Test
	public void testSimple() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/hello");

		Assertions.assertTrue( matches(matcher, "/hello"));
		Assertions.assertFalse(matches(matcher, "/hello/world"));
		Assertions.assertFalse(matches(matcher, "/hello/world/two"));
		Assertions.assertFalse(matches(matcher, "/bye"));
		Assertions.assertFalse(matches(matcher, "/bye/world"));
		Assertions.assertFalse(matches(matcher, "/bye/world/two"));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testPathTraversingWildcardComplex() {

		OSCPatternAddressMessageSelector matcher = new OSCPatternAddressMessageSelector("/my//hello///two/cents//");

		Assertions.assertFalse(matches(matcher, "/hello"));
		Assertions.assertFalse(matches(matcher, "/hello/world"));
		Assertions.assertFalse(matches(matcher, "/hello/world/two"));
		Assertions.assertFalse(matches(matcher, "/bye"));
		Assertions.assertFalse(matches(matcher, "/bye/world"));
		Assertions.assertFalse(matches(matcher, "/bye/world/two"));
		Assertions.assertTrue( matches(matcher, "/my/hello/two/cents"));
		Assertions.assertTrue( matches(matcher, "/my/few/cents/hello/two/cents"));
		Assertions.assertTrue( matches(matcher, "/my/few/cents/hello/thats/two/cents"));
		Assertions.assertTrue( matches(matcher, "/my/few/cents/hello/thats/two/cents/too"));
		Assertions.assertTrue( matches(matcher, "/my/few/cents/hello/thats/two/or/three/no/two/cents"));
		Assertions.assertTrue( matches(matcher, "/my/few/cents/hello/thats/two/or/three/no/two/cents/too"));
		Assertions.assertFalse(matches(matcher, "/my/few/cents/hello/thats/two/or/three/no/two/bad/cents/too"));
	}
}
