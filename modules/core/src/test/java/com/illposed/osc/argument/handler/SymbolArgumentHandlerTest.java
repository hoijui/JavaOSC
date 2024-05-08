// SPDX-FileCopyrightText: 2015 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc.argument.handler;

import com.illposed.osc.argument.OSCSymbol;
import com.illposed.osc.OSCParseException;
import com.illposed.osc.OSCSerializeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SymbolArgumentHandlerTest {

	private static OSCSymbol reparse(final OSCSymbol orig)
			throws OSCSerializeException, OSCParseException
	{
		final int strLength = (orig.toString() == null) ? 0 : orig.toString().length();
		return ColorArgumentHandlerTest.reparse(new SymbolArgumentHandler(), strLength + 4, orig);
	}

	@Test
	public void testReparseNull() throws Exception {

		final OSCSymbol orig = OSCSymbol.valueOf(null);
		Assertions.assertThrows(
			NullPointerException.class,
			() -> Assertions.assertEquals(orig, reparse(orig))
		);
	}

	@Test
	public void testReparseValid() throws Exception {

		@SuppressWarnings("SpellCheckingInspection") final OSCSymbol[] symbols = new OSCSymbol[] {
			OSCSymbol.valueOf(""),
			OSCSymbol.valueOf("a"),
			OSCSymbol.valueOf("ab"),
			OSCSymbol.valueOf("abc"),
			OSCSymbol.valueOf("abcd")
		};

		for (final OSCSymbol orig : symbols) {
			Assertions.assertEquals(orig, reparse(orig));
		}
	}
}
