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

	@Test
	public void testPathTraversingWildcardNone() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/");

		Assert.assertTrue( matcher.matches("/"));
		Assert.assertFalse(matcher.matches("/hello"));
		Assert.assertFalse(matcher.matches("/hello/world"));
		Assert.assertFalse(matcher.matches("/hello/world/two"));
	}

	@Test
	public void testPathTraversingWildcardAll() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("//");

		Assert.assertTrue( matcher.matches("/"));
		Assert.assertTrue( matcher.matches("/hello"));
		Assert.assertTrue( matcher.matches("/hello/world"));
		Assert.assertTrue( matcher.matches("/hello/world/two"));
	}

	@Test
	public void testPathTraversingWildcardPrefix() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hello//");

		Assert.assertTrue( matcher.matches("/hello"));
		Assert.assertTrue( matcher.matches("/hello/world"));
		Assert.assertTrue( matcher.matches("/hello/world/two"));
		Assert.assertFalse(matcher.matches("/bye"));
		Assert.assertFalse(matcher.matches("/bye/world"));
		Assert.assertFalse(matcher.matches("/bye/world/two"));
	}

	@Test
	public void testPathTraversingWildcardPostfix() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("//two");

		Assert.assertFalse(matcher.matches("/hello"));
		Assert.assertFalse(matcher.matches("/hello/world"));
		Assert.assertTrue( matcher.matches("/hello/world/two"));
		Assert.assertFalse(matcher.matches("/bye"));
		Assert.assertFalse(matcher.matches("/bye/world"));
		Assert.assertTrue( matcher.matches("/bye/world/two"));
	}

	@Test
	public void testPathTraversingWildcardInfix() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("//world//");

		Assert.assertTrue( matcher.matches("/world"));
		Assert.assertFalse(matcher.matches("/hello"));
		Assert.assertTrue( matcher.matches("/hello/world"));
		Assert.assertTrue( matcher.matches("/hello/world/two"));
		Assert.assertFalse(matcher.matches("/bye"));
		Assert.assertTrue( matcher.matches("/bye/world"));
		Assert.assertTrue( matcher.matches("/bye/world/two"));
	}

	@Test
	public void testPathTraversingWildcardAroundfix() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hello//two");

		Assert.assertFalse(matcher.matches("/world"));
		Assert.assertFalse(matcher.matches("/hello"));
		Assert.assertFalse(matcher.matches("/hello/world"));
		Assert.assertTrue( matcher.matches("/hello/world/two"));
		Assert.assertTrue( matcher.matches("/hello/my/sweet/world/two"));
		Assert.assertTrue( matcher.matches("/hello/universe/two"));
		Assert.assertFalse(matcher.matches("/bye"));
		Assert.assertFalse(matcher.matches("/bye/world"));
		Assert.assertFalse(matcher.matches("/bye/world/two"));
	}

	@Test
	public void testPathTraversingWildcardMultiplePrefix() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hello////");

		Assert.assertTrue( matcher.matches("/hello"));
		Assert.assertTrue( matcher.matches("/hello/world"));
		Assert.assertTrue( matcher.matches("/hello/world/two"));
		Assert.assertFalse(matcher.matches("/bye"));
		Assert.assertFalse(matcher.matches("/bye/world"));
		Assert.assertFalse(matcher.matches("/bye/world/two"));
	}

	@Test
	public void testPathTraversingWildcardMultiplePostfix() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("////two");

		Assert.assertFalse(matcher.matches("/hello"));
		Assert.assertFalse(matcher.matches("/hello/world"));
		Assert.assertTrue( matcher.matches("/hello/world/two"));
		Assert.assertFalse(matcher.matches("/bye"));
		Assert.assertFalse(matcher.matches("/bye/world"));
		Assert.assertTrue( matcher.matches("/bye/world/two"));
	}

	@Test
	public void testPathTraversingWildcardMultipleInfix() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("////world////");

		Assert.assertTrue( matcher.matches("/world"));
		Assert.assertFalse(matcher.matches("/hello"));
		Assert.assertTrue( matcher.matches("/hello/world"));
		Assert.assertTrue( matcher.matches("/hello/world/two"));
		Assert.assertFalse(matcher.matches("/bye"));
		Assert.assertTrue( matcher.matches("/bye/world"));
		Assert.assertTrue( matcher.matches("/bye/world/two"));
	}

	@Test
	public void testPathTraversingWildcardMultipleAroundfix() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hello////two");

		Assert.assertFalse(matcher.matches("/world"));
		Assert.assertFalse(matcher.matches("/hello"));
		Assert.assertFalse(matcher.matches("/hello/world"));
		Assert.assertTrue( matcher.matches("/hello/world/two"));
		Assert.assertTrue( matcher.matches("/hello/my/sweet/world/two"));
		Assert.assertTrue( matcher.matches("/hello/universe/two"));
		Assert.assertFalse(matcher.matches("/bye"));
		Assert.assertFalse(matcher.matches("/bye/world"));
		Assert.assertFalse(matcher.matches("/bye/world/two"));
	}

	@Test
	public void testPathTraversingWildcardNoneSingleTrailingSlash() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/hello/");

		Assert.assertTrue( matcher.matches("/hello"));
		Assert.assertFalse(matcher.matches("/hello/world"));
		Assert.assertFalse(matcher.matches("/hello/world/two"));
		Assert.assertFalse(matcher.matches("/bye"));
		Assert.assertFalse(matcher.matches("/bye/world"));
		Assert.assertFalse(matcher.matches("/bye/world/two"));
	}

	@Test
	public void testPathTraversingWildcardComplex() {

		OSCPatternAddressSelector matcher = new OSCPatternAddressSelector("/my//hello///two/cents//");

		Assert.assertFalse(matcher.matches("/hello"));
		Assert.assertFalse(matcher.matches("/hello/world"));
		Assert.assertFalse(matcher.matches("/hello/world/two"));
		Assert.assertFalse(matcher.matches("/bye"));
		Assert.assertFalse(matcher.matches("/bye/world"));
		Assert.assertFalse(matcher.matches("/bye/world/two"));
		Assert.assertTrue( matcher.matches("/my/hello/two/cents"));
		Assert.assertTrue( matcher.matches("/my/few/cents/hello/two/cents"));
		Assert.assertTrue( matcher.matches("/my/few/cents/hello/thats/two/cents"));
		Assert.assertTrue( matcher.matches("/my/few/cents/hello/thats/two/cents/too"));
		Assert.assertTrue( matcher.matches("/my/few/cents/hello/thats/two/or/three/no/two/cents"));
		Assert.assertTrue( matcher.matches("/my/few/cents/hello/thats/two/or/three/no/two/cents/too"));
		Assert.assertFalse(matcher.matches("/my/few/cents/hello/thats/two/or/three/no/two/bad/cents/too"));
	}
}
