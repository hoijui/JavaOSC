/*
 * Copyright (C) 2014, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.addressselector;

import com.illposed.osc.AddressSelector;
import org.junit.Assert;
import org.junit.Test;

public class OSCPatternAddressSelectorTest {

	private static boolean matches(final AddressSelector matcher, final String address) {
		return matcher.matches("/hello");
	}

	@Test
	public void testNumberOfParts() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/*/*");

		Assert.assertFalse(matches(matcher, "/hello"));
		Assert.assertTrue( matches(matcher, "/hello/world"));
		Assert.assertFalse(matches(matcher, "/hello/world/two"));
	}

	@Test
	public void testPartPrefix() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/he*/wo*");

		Assert.assertFalse(matches(matcher, "/bello/world"));
		Assert.assertTrue( matches(matcher, "/hello/world"));
		Assert.assertFalse(matches(matcher, "/hello/universe"));
		Assert.assertTrue( matches(matcher, "/hells/worlds"));
	}

	@Test
	public void testPartPostfix() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/*o/*rld");

		Assert.assertTrue( matches(matcher, "/bello/world"));
		Assert.assertTrue( matches(matcher, "/hello/world"));
		Assert.assertFalse(matches(matcher, "/hello/universe"));
		Assert.assertFalse(matches(matcher, "/hells/worlds"));
	}

	@Test
	public void testPartPreAndPostfix() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/h*o/w*rld*");

		Assert.assertFalse(matches(matcher, "/bello/world"));
		Assert.assertTrue( matches(matcher, "/hello/world"));
		Assert.assertFalse(matches(matcher, "/hello/universe"));
		Assert.assertTrue( matches(matcher, "/heyo/worlds"));
	}

	@Test
	public void testSingle() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/?ello/w?rl?");

		Assert.assertTrue( matches(matcher, "/bello/world"));
		Assert.assertTrue( matches(matcher, "/hello/world"));
		Assert.assertFalse(matches(matcher, "/hello/universe"));
		Assert.assertFalse(matches(matcher, "/hello/worlds"));
	}

	@Test
	public void testGroupForwards() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hel[a-z]o");

		Assert.assertTrue( matches(matcher, "/hello"));
		Assert.assertFalse(matches(matcher, "/hellos"));
		Assert.assertFalse(matches(matcher, "/hel_o"));
		Assert.assertFalse(matches(matcher, "/helLo"));
	}

	@Test
	public void testGroupBackwards() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hel[z-a]o");

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
	public void testGroupList() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hel[aly]o");

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
	public void testGroupListAndRange() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hel[lya-c]o");

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
	public void testGroupHyphenAndRange() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hel[-a-c]o");

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
	public void testCloseBracketAndRange() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hel[]a-c]o");

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
	public void testRegexNegate() {

		// '^' is just a character like any other, no special meaning!
		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hel[^a-c]o");

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
	public void testNegate() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hel[!a-c]o");

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
	public void testStringGroupSolo() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/{hello,hididelidoo}");

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
	public void testStringGroupPrefix() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/h{ello,ididelidoo}");

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
	public void testStringGroupPostfix() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/{hell,hididelido}o");

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
	public void testStringGroupPreAndPostfix() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/h{ell,ididelido}o");

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

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/");

		Assert.assertTrue( matches(matcher, "/"));
		Assert.assertFalse(matches(matcher, "/hello"));
		Assert.assertFalse(matches(matcher, "/hello/world"));
		Assert.assertFalse(matches(matcher, "/hello/world/two"));
	}

	@Test
	public void testPathTraversingWildcardAll() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("//");

		Assert.assertTrue( matches(matcher, "/"));
		Assert.assertTrue( matches(matcher, "/hello"));
		Assert.assertTrue( matches(matcher, "/hello/world"));
		Assert.assertTrue( matches(matcher, "/hello/world/two"));
	}

	@Test
	public void testPathTraversingWildcardPrefix() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hello//");

		Assert.assertTrue( matches(matcher, "/hello"));
		Assert.assertTrue( matches(matcher, "/hello/world"));
		Assert.assertTrue( matches(matcher, "/hello/world/two"));
		Assert.assertFalse(matches(matcher, "/bye"));
		Assert.assertFalse(matches(matcher, "/bye/world"));
		Assert.assertFalse(matches(matcher, "/bye/world/two"));
	}

	@Test
	public void testPathTraversingWildcardPostfix() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("//two");

		Assert.assertFalse(matches(matcher, "/hello"));
		Assert.assertFalse(matches(matcher, "/hello/world"));
		Assert.assertTrue( matches(matcher, "/hello/world/two"));
		Assert.assertFalse(matches(matcher, "/bye"));
		Assert.assertFalse(matches(matcher, "/bye/world"));
		Assert.assertTrue( matches(matcher, "/bye/world/two"));
	}

	@Test
	public void testPathTraversingWildcardInfix() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("//world//");

		Assert.assertTrue( matches(matcher, "/world"));
		Assert.assertFalse(matches(matcher, "/hello"));
		Assert.assertTrue( matches(matcher, "/hello/world"));
		Assert.assertTrue( matches(matcher, "/hello/world/two"));
		Assert.assertFalse(matches(matcher, "/bye"));
		Assert.assertTrue( matches(matcher, "/bye/world"));
		Assert.assertTrue( matches(matcher, "/bye/world/two"));
	}

	@Test
	public void testPathTraversingWildcardAroundfix() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hello//two");

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

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hello////");

		Assert.assertTrue( matches(matcher, "/hello"));
		Assert.assertTrue( matches(matcher, "/hello/world"));
		Assert.assertTrue( matches(matcher, "/hello/world/two"));
		Assert.assertFalse(matches(matcher, "/bye"));
		Assert.assertFalse(matches(matcher, "/bye/world"));
		Assert.assertFalse(matches(matcher, "/bye/world/two"));
	}

	@Test
	public void testPathTraversingWildcardMultiplePostfix() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("////two");

		Assert.assertFalse(matches(matcher, "/hello"));
		Assert.assertFalse(matches(matcher, "/hello/world"));
		Assert.assertTrue( matches(matcher, "/hello/world/two"));
		Assert.assertFalse(matches(matcher, "/bye"));
		Assert.assertFalse(matches(matcher, "/bye/world"));
		Assert.assertTrue( matches(matcher, "/bye/world/two"));
	}

	@Test
	public void testPathTraversingWildcardMultipleInfix() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("////world////");

		Assert.assertTrue( matches(matcher, "/world"));
		Assert.assertFalse(matches(matcher, "/hello"));
		Assert.assertTrue( matches(matcher, "/hello/world"));
		Assert.assertTrue( matches(matcher, "/hello/world/two"));
		Assert.assertFalse(matches(matcher, "/bye"));
		Assert.assertTrue( matches(matcher, "/bye/world"));
		Assert.assertTrue( matches(matcher, "/bye/world/two"));
	}

	@Test
	public void testPathTraversingWildcardMultipleAroundfix() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hello////two");

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

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hello/");

		Assert.assertTrue( matches(matcher, "/hello"));
		Assert.assertFalse(matches(matcher, "/hello/world"));
		Assert.assertFalse(matches(matcher, "/hello/world/two"));
		Assert.assertFalse(matches(matcher, "/bye"));
		Assert.assertFalse(matches(matcher, "/bye/world"));
		Assert.assertFalse(matches(matcher, "/bye/world/two"));
	}

	@Test
	public void testPathTraversingWildcardComplex() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/my//hello///two/cents//");

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
