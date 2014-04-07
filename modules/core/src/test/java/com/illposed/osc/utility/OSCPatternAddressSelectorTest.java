/*
 * Copyright (C) 2014, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.utility;

public class OSCPatternAddressSelectorTest extends junit.framework.TestCase {

	public void testNumberOfParts() throws Exception {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/*/*");

		assertFalse(matcher.matches("/hello"));
		assertTrue( matcher.matches("/hello/world"));
		assertFalse(matcher.matches("/hello/world/two"));
	}

	public void testPartPrefix() throws Exception {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/he*/wo*");

		assertFalse(matcher.matches("/bello/world"));
		assertTrue( matcher.matches("/hello/world"));
		assertFalse(matcher.matches("/hello/universe"));
		assertTrue( matcher.matches("/hells/worlds"));
	}

	public void testPartPostfix() throws Exception {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/*o/*rld");

		assertTrue( matcher.matches("/bello/world"));
		assertTrue( matcher.matches("/hello/world"));
		assertFalse(matcher.matches("/hello/universe"));
		assertFalse(matcher.matches("/hells/worlds"));
	}

	public void testPartPreAndPostfix() throws Exception {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/h*o/w*rld*");

		assertFalse(matcher.matches("/bello/world"));
		assertTrue( matcher.matches("/hello/world"));
		assertFalse(matcher.matches("/hello/universe"));
		assertTrue( matcher.matches("/heyo/worlds"));
	}

	public void testSingle() throws Exception {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/?ello/w?rl?");

		assertTrue( matcher.matches("/bello/world"));
		assertTrue( matcher.matches("/hello/world"));
		assertFalse(matcher.matches("/hello/universe"));
		assertFalse(matcher.matches("/hello/worlds"));
	}

	public void testGroupForwards() throws Exception {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hel[a-z]o");

		assertTrue( matcher.matches("/hello"));
		assertFalse(matcher.matches("/hellos"));
		assertFalse(matcher.matches("/hel_o"));
		assertFalse(matcher.matches("/helLo"));
	}

	public void testGroupBackwards() throws Exception {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hel[z-a]o");

		assertTrue( matcher.matches("/helao"));
		assertTrue( matcher.matches("/helzo"));
		assertFalse(matcher.matches("/helo"));
		assertFalse(matcher.matches("/helzzzzzo"));
		assertFalse(matcher.matches("/hello"));
		assertFalse(matcher.matches("/hellos"));
		assertFalse(matcher.matches("/hel_o"));
		assertFalse(matcher.matches("/helLo"));
	}

	public void testGroupList() throws Exception {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hel[aly]o");

		assertTrue( matcher.matches("/helao"));
		assertTrue( matcher.matches("/hello"));
		assertTrue( matcher.matches("/helyo"));
		assertFalse(matcher.matches("/helzo"));
		assertFalse(matcher.matches("/hellllo"));
		assertFalse(matcher.matches("/hellos"));
		assertFalse(matcher.matches("/hel_o"));
		assertFalse(matcher.matches("/helLo"));
	}

	public void testGroupListAndRange() throws Exception {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hel[lya-c]o");

		assertTrue( matcher.matches("/helao"));
		assertTrue( matcher.matches("/helbo"));
		assertTrue( matcher.matches("/helco"));
		assertTrue( matcher.matches("/hello"));
		assertTrue( matcher.matches("/helyo"));
		assertFalse(matcher.matches("/helzo"));
		assertFalse(matcher.matches("/hellllo"));
		assertFalse(matcher.matches("/hellos"));
		assertFalse(matcher.matches("/hel_o"));
		assertFalse(matcher.matches("/helLo"));
	}

	public void testGroupHyphenAndRange() throws Exception {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hel[-a-c]o");

		assertTrue( matcher.matches("/hel-o"));
		assertTrue( matcher.matches("/helao"));
		assertTrue( matcher.matches("/helbo"));
		assertTrue( matcher.matches("/helco"));
		assertFalse(matcher.matches("/helzo"));
		assertFalse(matcher.matches("/hellllo"));
		assertFalse(matcher.matches("/hellos"));
		assertFalse(matcher.matches("/hel_o"));
		assertFalse(matcher.matches("/helLo"));
	}

	public void testCloseBracketAndRange() throws Exception {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hel[]a-c]o");

		assertTrue( matcher.matches("/hel]o"));
		assertTrue( matcher.matches("/helao"));
		assertTrue( matcher.matches("/helbo"));
		assertTrue( matcher.matches("/helco"));
		assertFalse(matcher.matches("/helzo"));
		assertFalse(matcher.matches("/hellllo"));
		assertFalse(matcher.matches("/hellos"));
		assertFalse(matcher.matches("/hel_o"));
		assertFalse(matcher.matches("/helLo"));
	}

	public void testRegexNegate() throws Exception {

		// '^' is just a character like any other, no special meaning!
		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hel[^a-c]o");

		assertTrue( matcher.matches("/hel^o"));
		assertTrue( matcher.matches("/helao"));
		assertTrue( matcher.matches("/helbo"));
		assertTrue( matcher.matches("/helco"));
		assertFalse(matcher.matches("/hel-o"));
		assertFalse(matcher.matches("/hel]o"));
		assertFalse(matcher.matches("/hello"));
		assertFalse(matcher.matches("/helzo"));
		assertFalse(matcher.matches("/hellllo"));
		assertFalse(matcher.matches("/hellos"));
		assertFalse(matcher.matches("/hel_o"));
		assertFalse(matcher.matches("/helLo"));
	}

	public void testNegate() throws Exception {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hel[!a-c]o");

		assertFalse(matcher.matches("/helao"));
		assertFalse(matcher.matches("/helbo"));
		assertFalse(matcher.matches("/helco"));
		assertTrue( matcher.matches("/hel-o"));
		assertTrue( matcher.matches("/hel]o"));
		assertTrue( matcher.matches("/hello"));
		assertTrue( matcher.matches("/helzo"));
		assertFalse(matcher.matches("/hellllo"));
		assertFalse(matcher.matches("/hellos"));
		assertTrue( matcher.matches("/hel_o"));
		assertTrue( matcher.matches("/helLo"));
	}

	public void testStringGroupSolo() throws Exception {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/{hello,hididelidoo}");

		assertTrue( matcher.matches("/hello"));
		assertTrue( matcher.matches("/hididelidoo"));
		assertFalse(matcher.matches("/helco"));
		assertFalse(matcher.matches("/hel-o"));
		assertFalse(matcher.matches("/hel]o"));
		assertFalse(matcher.matches("/helzo"));
		assertFalse(matcher.matches("/hellllo"));
		assertFalse(matcher.matches("/hellos"));
		assertFalse(matcher.matches("/hel_o"));
		assertFalse(matcher.matches("/helLo"));
	}

	public void testStringGroupPrefix() throws Exception {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/h{ello,ididelidoo}");

		assertTrue( matcher.matches("/hello"));
		assertTrue( matcher.matches("/hididelidoo"));
		assertFalse(matcher.matches("/helco"));
		assertFalse(matcher.matches("/hel-o"));
		assertFalse(matcher.matches("/hel]o"));
		assertFalse(matcher.matches("/helzo"));
		assertFalse(matcher.matches("/hellllo"));
		assertFalse(matcher.matches("/hellos"));
		assertFalse(matcher.matches("/hel_o"));
		assertFalse(matcher.matches("/helLo"));
	}

	public void testStringGroupPostfix() throws Exception {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/{hell,hididelido}o");

		assertTrue( matcher.matches("/hello"));
		assertTrue( matcher.matches("/hididelidoo"));
		assertFalse(matcher.matches("/helco"));
		assertFalse(matcher.matches("/hel-o"));
		assertFalse(matcher.matches("/hel]o"));
		assertFalse(matcher.matches("/helzo"));
		assertFalse(matcher.matches("/hellllo"));
		assertFalse(matcher.matches("/hellos"));
		assertFalse(matcher.matches("/hel_o"));
		assertFalse(matcher.matches("/helLo"));
	}

	public void testStringGroupPreAndPostfix() throws Exception {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/h{ell,ididelido}o");

		assertTrue( matcher.matches("/hello"));
		assertTrue( matcher.matches("/hididelidoo"));
		assertFalse(matcher.matches("/helco"));
		assertFalse(matcher.matches("/hel-o"));
		assertFalse(matcher.matches("/hel]o"));
		assertFalse(matcher.matches("/helzo"));
		assertFalse(matcher.matches("/hellllo"));
		assertFalse(matcher.matches("/hellos"));
		assertFalse(matcher.matches("/hel_o"));
		assertFalse(matcher.matches("/helLo"));
	}
}
