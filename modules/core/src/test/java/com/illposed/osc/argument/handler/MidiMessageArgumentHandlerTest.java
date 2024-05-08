// SPDX-FileCopyrightText: 2015 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc.argument.handler;

import com.illposed.osc.argument.OSCMidiMessage;
import com.illposed.osc.OSCParseException;
import com.illposed.osc.OSCSerializeException;
import java.util.Random;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MidiMessageArgumentHandlerTest {

	private final Logger log = LoggerFactory.getLogger(MidiMessageArgumentHandlerTest.class);

	private static OSCMidiMessage reparse(final OSCMidiMessage orig)
			throws OSCSerializeException, OSCParseException
	{
		return ColorArgumentHandlerTest.reparse(
				MidiMessageArgumentHandler.INSTANCE,
				OSCMidiMessage.NUM_CONTENT_BYTES,
				orig);
	}

	private static void testReparse(final OSCMidiMessage orig)
			throws OSCSerializeException, OSCParseException
	{
		Assertions.assertEquals(orig, reparse(orig));
	}

	@Test
	public void testReparseNull() throws OSCSerializeException, OSCParseException {

		final OSCMidiMessage orig = null;
		Assertions.assertThrows(
			NullPointerException.class,
			() -> Assertions.assertEquals(orig, reparse(orig))
		);
	}

	private static void testContent(final byte[] content) {
		OSCMidiMessage.valueOf(content);
	}

	@Test
	public void testContentBytesZero() {
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> testContent(new byte[0])
		);
	}

	@Test
	public void testContentBytesOne() {
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> testContent(new byte[1])
		);
	}

	@Test
	public void testContentBytesTwo() {
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> testContent(new byte[2])
		);
	}

	@Test
	public void testContentBytesThree() {
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> testContent(new byte[3])
		);
	}

	@Test
	public void testContentBytesFour() {
		testContent(new byte[4]);
	}

	@Test
	public void testContentBytesFive() {
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> testContent(new byte[5])
		);
	}

	@Test
	public void testReparseZeros() throws Exception {
		testReparse(OSCMidiMessage.valueOf(new byte[] {0, 0, 0, 0}));
	}

	@Test
	public void testReparsePositive() throws Exception {
		testReparse(OSCMidiMessage.valueOf(new byte[] {127, 127, 127, 127}));
	}

	@Test
	public void testReparseNegative() throws Exception {
		testReparse(OSCMidiMessage.valueOf(new byte[] {-128, -128, -128, -128}));
	}

	@Test
	public void testReparseRandom() throws Exception {

		final long contentRandomSeed = new Random().nextLong();
		log.debug("{}#testReparseRandom:contentRandomSeed: {}",
				MidiMessageArgumentHandlerTest.class.getSimpleName(), contentRandomSeed);
		final Random contentRandom = new Random(contentRandomSeed);
		final byte[] content = new byte[4];
		contentRandom.nextBytes(content);
		testReparse(OSCMidiMessage.valueOf(content));
	}
}
