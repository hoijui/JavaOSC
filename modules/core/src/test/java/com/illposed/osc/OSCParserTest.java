/*
 * Copyright (C) 2003, C. Ramakrishnan / Auracle.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.illposed.osc;

import com.illposed.osc.argument.OSCTimeTag64;
import com.illposed.osc.argument.OSCUnsigned;
import java.nio.ByteBuffer;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @see OSCParser
 */
public class OSCParserTest {

	private OSCParser converter;

	@Before
	public void setUp() {
		converter = new OSCSerializerAndParserBuilder().buildParser();
	}

	@After
	public void tearDown() {

	}

	private static void checkAddress(final String expectedAddress, final String observedAddress) {
		if (!observedAddress.equals(expectedAddress)) {
			Assert.fail("Address should be " + expectedAddress + ", but is " + observedAddress);
		}
	}

	private OSCPacket convertToPacket(final byte[] bytes) {

		final OSCPacket packet;
		try {
			final ByteBuffer readOnlyBytes = ByteBuffer.wrap(bytes).asReadOnlyBuffer();
			packet = converter.convert(readOnlyBytes);
		} catch (final OSCParseException ex) {
			throw new RuntimeException(ex);
		}
		return packet;
	}

	private OSCMessage convertToMessage(final byte[] bytes) {
		return (OSCMessage) convertToPacket(bytes);
	}

	private void checkReadUnsignedInteger(final long given32bitUnsigned) {

		final byte[] bytes = {47, 0, 0, 0, 44, 117, 0, 0,
			(byte) (given32bitUnsigned >> 24 & 0xFFL),
			(byte) (given32bitUnsigned >> 16 & 0xFFL),
			(byte) (given32bitUnsigned >>  8 & 0xFFL),
			(byte) (given32bitUnsigned       & 0xFFL)};
		final OSCMessage packet = convertToMessage(bytes);
		final OSCUnsigned unsigned = (OSCUnsigned) packet.getArguments().get(0);
		final long parsed32bitUnsigned = unsigned.toLong();
		Assert.assertEquals("Failed parsing 32bit unsigned ('u') value",
				given32bitUnsigned, parsed32bitUnsigned);
	}

	/**
	 * @see OSCReparserTest#testArgumentUnsignedInteger0()
	 */
	@Test
	public void testReadUnsignedInteger0() {
		checkReadUnsignedInteger(0x0L);
	}

	@Test
	public void testReadUnsignedInteger1() {
		checkReadUnsignedInteger(0x1L);
	}

	@Test
	public void testReadUnsignedIntegerF() {
		checkReadUnsignedInteger(0xFL);
	}

	@Test
	public void testReadUnsignedIntegerFF() {
		checkReadUnsignedInteger(0xFFL);
	}

	@Test
	public void testReadUnsignedIntegerFFF() {
		checkReadUnsignedInteger(0xFFFL);
	}

	@Test
	public void testReadUnsignedIntegerFFFF() {
		checkReadUnsignedInteger(0xFFFFL);
	}

	@Test
	public void testReadUnsignedIntegerFFFFF() {
		checkReadUnsignedInteger(0xFFFFFL);
	}

	@Test
	public void testReadUnsignedIntegerFFFFFF() {
		checkReadUnsignedInteger(0xFFFFFFL);
	}

	@Test
	public void testReadUnsignedIntegerFFFFFFF() {
		checkReadUnsignedInteger(0xFFFFFFFL);
	}

	@Test
	public void testReadUnsignedIntegerFFFFFFFF() {
		checkReadUnsignedInteger(0xFFFFFFFFL);
	}

	@Test(expected=AssertionError.class)
	public void testReadUnsignedInteger100000000() {
		checkReadUnsignedInteger(0x100000000L); // 33bit -> out of range!
	}

	@Test(expected=AssertionError.class)
	public void testReadUnsignedInteger1FFFFFFFF() {
		checkReadUnsignedInteger(0x1FFFFFFFFL); // 33bit -> out of range!
	}

	@Test(expected=AssertionError.class)
	public void testReadUnsignedIntegerFFFFFFFFF() {
		checkReadUnsignedInteger(0xFFFFFFFFFL); // 36bit -> out of range!
	}

	@Test
	public void testReadShortestPacketWithoutArgumentsSeparator() {
		// This packet omits the character (',') that separates address
		// from parameters. This is supposed legacy practise,
		// but still not explicitly forbidden by the OSC 1.0 specifications,
		// and should therefore be supported.
		final byte[] bytes = {47, 0, 0, 0};
		final OSCMessage packet = convertToMessage(bytes);
		checkAddress("/", packet.getAddress());
	}

	@Test
	public void testReadShortPacket1() {
		final byte[] bytes = {47, 0, 0, 0, 44, 0, 0, 0};
		final OSCMessage packet = convertToMessage(bytes);
		checkAddress("/", packet.getAddress());
	}

	@Test
	public void testReadShortPacket2() {
		final byte[] bytes = {47, 115, 0, 0, 44, 0, 0, 0};
		final OSCMessage packet = convertToMessage(bytes);
		checkAddress("/s", packet.getAddress());
	}

	@Test
	public void testReadSimplePacket() {
		final byte[] bytes = {47, 115, 99, 47, 114, 117, 110, 0, 44, 0, 0, 0};
		final OSCMessage packet = convertToMessage(bytes);
		checkAddress("/sc/run", packet.getAddress());
	}

	@Test
	public void testReadComplexPacket() {
		final byte[] bytes = {0x2F, 0x73, 0x5F, 0x6E, 0x65, 0x77, 0, 0, 0x2C, 0x69, 0x73, 0x66, 0, 0, 0, 0, 0, 0, 0x3, (byte) 0xE9, 0x66, 0x72, 0x65, 0x71, 0, 0, 0, 0, 0x43, (byte) 0xDC, 0, 0};

		final OSCMessage packet = convertToMessage(bytes);
		checkAddress("/s_new", packet.getAddress());
		final List<?> arguments = packet.getArguments();
		if (arguments.size() != 3) {
			Assert.fail("Num arguments should be 3, but is " + arguments.size());
		}
		if (!(Integer.valueOf(1001).equals(arguments.get(0)))) {
			Assert.fail("Argument 1 should be 1001, but is " + arguments.get(0));
		}
		if (!("freq".equals(arguments.get(1)))) {
			Assert.fail("Argument 2 should be freq, but is " + arguments.get(1));
		}
		if (!(Float.valueOf(440.0f).equals(arguments.get(2)))) {
			Assert.fail("Argument 3 should be 440.0, but is " + arguments.get(2));
		}
	}

	@Test
	public void testReadBundle() {
		final byte[] bytes
				= {0x23, 0x62, 0x75, 0x6E, 0x64, 0x6C, 0x65, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0,
					0x0C, 0X2F, 0x74, 0x65, 0x73, 0x74, 0, 0, 0, 0x2C, 0, 0, 0};

		final OSCBundle bundle = (OSCBundle) convertToPacket(bytes);
		if (!bundle.getTimestamp().equals(OSCTimeTag64.IMMEDIATE)) {
			Assert.fail("Timestamp should be IMMEDIATE, but is " + bundle.getTimestamp());
		}
		final List<OSCPacket> packets = bundle.getPackets();
		if (packets.size() != 1) {
			Assert.fail("Num packets should be 1, but is " + packets.size());
		}
		final OSCMessage message = (OSCMessage) packets.get(0);
		checkAddress("/test", message.getAddress());
	}
}
