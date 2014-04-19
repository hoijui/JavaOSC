/*
 * Copyright (C) 2014, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.utility;

import org.junit.Assert;
import org.junit.Test;

public class OSCPatternAddressSelectorTest {

	@Test
	public void testNumberOfParts() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/*/*");

		Assert.assertFalse(matcher.matches("/hello"));
		Assert.assertTrue( matcher.matches("/hello/world"));
		Assert.assertFalse(matcher.matches("/hello/world/two"));
	}

	@Test
	public void testPartPrefix() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/he*/wo*");

		Assert.assertFalse(matcher.matches("/bello/world"));
		Assert.assertTrue( matcher.matches("/hello/world"));
		Assert.assertFalse(matcher.matches("/hello/universe"));
		Assert.assertTrue( matcher.matches("/hells/worlds"));
	}

	@Test
	public void testPartPostfix() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/*o/*rld");

		Assert.assertTrue( matcher.matches("/bello/world"));
		Assert.assertTrue( matcher.matches("/hello/world"));
		Assert.assertFalse(matcher.matches("/hello/universe"));
		Assert.assertFalse(matcher.matches("/hells/worlds"));
	}

	@Test
	public void testPartPreAndPostfix() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/h*o/w*rld*");

		Assert.assertFalse(matcher.matches("/bello/world"));
		Assert.assertTrue( matcher.matches("/hello/world"));
		Assert.assertFalse(matcher.matches("/hello/universe"));
		Assert.assertTrue( matcher.matches("/heyo/worlds"));
	}

	@Test
	public void testSingle() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/?ello/w?rl?");

		Assert.assertTrue( matcher.matches("/bello/world"));
		Assert.assertTrue( matcher.matches("/hello/world"));
		Assert.assertFalse(matcher.matches("/hello/universe"));
		Assert.assertFalse(matcher.matches("/hello/worlds"));
	}

	@Test
	public void testGroupForwards() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hel[a-z]o");

		Assert.assertTrue( matcher.matches("/hello"));
		Assert.assertFalse(matcher.matches("/hellos"));
		Assert.assertFalse(matcher.matches("/hel_o"));
		Assert.assertFalse(matcher.matches("/helLo"));
	}

	@Test
	public void testGroupBackwards() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hel[z-a]o");

		Assert.assertTrue( matcher.matches("/helao"));
		Assert.assertTrue( matcher.matches("/helzo"));
		Assert.assertFalse(matcher.matches("/helo"));
		Assert.assertFalse(matcher.matches("/helzzzzzo"));
		Assert.assertFalse(matcher.matches("/hello"));
		Assert.assertFalse(matcher.matches("/hellos"));
		Assert.assertFalse(matcher.matches("/hel_o"));
		Assert.assertFalse(matcher.matches("/helLo"));
	}

	@Test
	public void testGroupList() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hel[aly]o");

		Assert.assertTrue( matcher.matches("/helao"));
		Assert.assertTrue( matcher.matches("/hello"));
		Assert.assertTrue( matcher.matches("/helyo"));
		Assert.assertFalse(matcher.matches("/helzo"));
		Assert.assertFalse(matcher.matches("/hellllo"));
		Assert.assertFalse(matcher.matches("/hellos"));
		Assert.assertFalse(matcher.matches("/hel_o"));
		Assert.assertFalse(matcher.matches("/helLo"));
	}

	@Test
	public void testGroupListAndRange() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hel[lya-c]o");

		Assert.assertTrue( matcher.matches("/helao"));
		Assert.assertTrue( matcher.matches("/helbo"));
		Assert.assertTrue( matcher.matches("/helco"));
		Assert.assertTrue( matcher.matches("/hello"));
		Assert.assertTrue( matcher.matches("/helyo"));
		Assert.assertFalse(matcher.matches("/helzo"));
		Assert.assertFalse(matcher.matches("/hellllo"));
		Assert.assertFalse(matcher.matches("/hellos"));
		Assert.assertFalse(matcher.matches("/hel_o"));
		Assert.assertFalse(matcher.matches("/helLo"));
	}

	@Test
	public void testGroupHyphenAndRange() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hel[-a-c]o");

		Assert.assertTrue( matcher.matches("/hel-o"));
		Assert.assertTrue( matcher.matches("/helao"));
		Assert.assertTrue( matcher.matches("/helbo"));
		Assert.assertTrue( matcher.matches("/helco"));
		Assert.assertFalse(matcher.matches("/helzo"));
		Assert.assertFalse(matcher.matches("/hellllo"));
		Assert.assertFalse(matcher.matches("/hellos"));
		Assert.assertFalse(matcher.matches("/hel_o"));
		Assert.assertFalse(matcher.matches("/helLo"));
	}

	@Test
	public void testCloseBracketAndRange() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hel[]a-c]o");

		Assert.assertTrue( matcher.matches("/hel]o"));
		Assert.assertTrue( matcher.matches("/helao"));
		Assert.assertTrue( matcher.matches("/helbo"));
		Assert.assertTrue( matcher.matches("/helco"));
		Assert.assertFalse(matcher.matches("/helzo"));
		Assert.assertFalse(matcher.matches("/hellllo"));
		Assert.assertFalse(matcher.matches("/hellos"));
		Assert.assertFalse(matcher.matches("/hel_o"));
		Assert.assertFalse(matcher.matches("/helLo"));
	}

	@Test
	public void testRegexNegate() {

		// '^' is just a character like any other, no special meaning!
		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hel[^a-c]o");

		Assert.assertTrue( matcher.matches("/hel^o"));
		Assert.assertTrue( matcher.matches("/helao"));
		Assert.assertTrue( matcher.matches("/helbo"));
		Assert.assertTrue( matcher.matches("/helco"));
		Assert.assertFalse(matcher.matches("/hel-o"));
		Assert.assertFalse(matcher.matches("/hel]o"));
		Assert.assertFalse(matcher.matches("/hello"));
		Assert.assertFalse(matcher.matches("/helzo"));
		Assert.assertFalse(matcher.matches("/hellllo"));
		Assert.assertFalse(matcher.matches("/hellos"));
		Assert.assertFalse(matcher.matches("/hel_o"));
		Assert.assertFalse(matcher.matches("/helLo"));
	}

	@Test
	public void testNegate() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hel[!a-c]o");

		Assert.assertFalse(matcher.matches("/helao"));
		Assert.assertFalse(matcher.matches("/helbo"));
		Assert.assertFalse(matcher.matches("/helco"));
		Assert.assertTrue( matcher.matches("/hel-o"));
		Assert.assertTrue( matcher.matches("/hel]o"));
		Assert.assertTrue( matcher.matches("/hello"));
		Assert.assertTrue( matcher.matches("/helzo"));
		Assert.assertFalse(matcher.matches("/hellllo"));
		Assert.assertFalse(matcher.matches("/hellos"));
		Assert.assertTrue( matcher.matches("/hel_o"));
		Assert.assertTrue( matcher.matches("/helLo"));
	}

	@Test
	public void testStringGroupSolo() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/{hello,hididelidoo}");

		Assert.assertTrue( matcher.matches("/hello"));
		Assert.assertTrue( matcher.matches("/hididelidoo"));
		Assert.assertFalse(matcher.matches("/helco"));
		Assert.assertFalse(matcher.matches("/hel-o"));
		Assert.assertFalse(matcher.matches("/hel]o"));
		Assert.assertFalse(matcher.matches("/helzo"));
		Assert.assertFalse(matcher.matches("/hellllo"));
		Assert.assertFalse(matcher.matches("/hellos"));
		Assert.assertFalse(matcher.matches("/hel_o"));
		Assert.assertFalse(matcher.matches("/helLo"));
	}

	@Test
	public void testStringGroupPrefix() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/h{ello,ididelidoo}");

		Assert.assertTrue( matcher.matches("/hello"));
		Assert.assertTrue( matcher.matches("/hididelidoo"));
		Assert.assertFalse(matcher.matches("/helco"));
		Assert.assertFalse(matcher.matches("/hel-o"));
		Assert.assertFalse(matcher.matches("/hel]o"));
		Assert.assertFalse(matcher.matches("/helzo"));
		Assert.assertFalse(matcher.matches("/hellllo"));
		Assert.assertFalse(matcher.matches("/hellos"));
		Assert.assertFalse(matcher.matches("/hel_o"));
		Assert.assertFalse(matcher.matches("/helLo"));
	}

	@Test
	public void testStringGroupPostfix() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/{hell,hididelido}o");

		Assert.assertTrue( matcher.matches("/hello"));
		Assert.assertTrue( matcher.matches("/hididelidoo"));
		Assert.assertFalse(matcher.matches("/helco"));
		Assert.assertFalse(matcher.matches("/hel-o"));
		Assert.assertFalse(matcher.matches("/hel]o"));
		Assert.assertFalse(matcher.matches("/helzo"));
		Assert.assertFalse(matcher.matches("/hellllo"));
		Assert.assertFalse(matcher.matches("/hellos"));
		Assert.assertFalse(matcher.matches("/hel_o"));
		Assert.assertFalse(matcher.matches("/helLo"));
	}

	@Test
	public void testStringGroupPreAndPostfix() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/h{ell,ididelido}o");

		Assert.assertTrue( matcher.matches("/hello"));
		Assert.assertTrue( matcher.matches("/hididelidoo"));
		Assert.assertFalse(matcher.matches("/helco"));
		Assert.assertFalse(matcher.matches("/hel-o"));
		Assert.assertFalse(matcher.matches("/hel]o"));
		Assert.assertFalse(matcher.matches("/helzo"));
		Assert.assertFalse(matcher.matches("/hellllo"));
		Assert.assertFalse(matcher.matches("/hellos"));
		Assert.assertFalse(matcher.matches("/hel_o"));
		Assert.assertFalse(matcher.matches("/helLo"));
	}
}
