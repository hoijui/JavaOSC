/*
 * Copyright (C) 2015, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.argument.handler;

import com.illposed.osc.argument.OSCSymbol;
import com.illposed.osc.utility.OSCParseException;
import com.illposed.osc.utility.OSCSerializeException;
import org.junit.Assert;
import org.junit.Test;

public class SymbolArgumentHandlerTest {

	private static OSCSymbol reparse(final OSCSymbol orig)
			throws OSCSerializeException, OSCParseException
	{
		final int strLength = (orig.toString() == null) ? 0 : orig.toString().length();
		return ColorArgumentHandlerTest.reparse(new SymbolArgumentHandler(), strLength + 4, orig);
	}

	@Test(expected = NullPointerException.class)
	public void testReparseNull() throws OSCSerializeException, OSCParseException {

		final OSCSymbol orig = OSCSymbol.valueOf(null);
		Assert.assertEquals(orig, reparse(orig));
	}

	@Test
	public void testReparseValid() throws OSCSerializeException, OSCParseException {

		final OSCSymbol[] symbols = new OSCSymbol[] {
			OSCSymbol.valueOf(""),
			OSCSymbol.valueOf("a"),
			OSCSymbol.valueOf("ab"),
			OSCSymbol.valueOf("abc"),
			OSCSymbol.valueOf("abcd")
		};

		for (final OSCSymbol orig : symbols) {
			Assert.assertEquals(orig, reparse(orig));
		}
	}
}
