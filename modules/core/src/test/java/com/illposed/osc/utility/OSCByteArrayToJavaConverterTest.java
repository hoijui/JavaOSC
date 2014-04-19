/*
 * Copyright (C) 2003, C. Ramakrishnan / Auracle.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.utility;

import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPacket;
import java.util.Date;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Chandrasekhar Ramakrishnan
 * @see OSCByteArrayToJavaConverter
 */
public class OSCByteArrayToJavaConverterTest {

	private OSCByteArrayToJavaConverter converter;

	@Before
	public void setUp() {
		converter = new OSCByteArrayToJavaConverter();
	}

	@After
	public void tearDown() {

	}

	private static void checkAddress(final String expectedAddress, final String observedAddress) {
		if (!observedAddress.equals(expectedAddress)) {
			Assert.fail("Address should be " + expectedAddress + ", but is " + observedAddress);
		}
	}

	@Test
	public void testReadShortestPacketWithoutArgumentsSeparator() {
		// This pakcet ommits the character (',') that separates address
		// from parameters. This is supposed legacy practise,
		// but still not explicitly forbidden by the OSC 1.0 specificiations,
		// and should therefore be supported.
		final byte[] bytes = {47, 0, 0, 0};
		final OSCMessage packet = (OSCMessage) converter.convert(bytes, bytes.length);
		checkAddress("/", packet.getAddress());
	}

	@Test
	public void testReadShortPacket1() {
		final byte[] bytes = {47, 0, 0, 0, 44, 0, 0, 0};
		final OSCMessage packet = (OSCMessage) converter.convert(bytes, bytes.length);
		checkAddress("/", packet.getAddress());
	}

	@Test
	public void testReadShortPacket2() {
		final byte[] bytes = {47, 115, 0, 0, 44, 0, 0, 0};
		final OSCMessage packet = (OSCMessage) converter.convert(bytes, bytes.length);
		checkAddress("/s", packet.getAddress());
	}

	@Test
	public void testReadSimplePacket() {
		final byte[] bytes = {47, 115, 99, 47, 114, 117, 110, 0, 44, 0, 0, 0};
		final OSCMessage packet = (OSCMessage) converter.convert(bytes, bytes.length);
		checkAddress("/sc/run", packet.getAddress());
	}

	@Test
	public void testReadComplexPacket() {
		final byte[] bytes = {0x2F, 0x73, 0x5F, 0x6E, 0x65, 0x77, 0, 0, 0x2C, 0x69, 0x73, 0x66, 0, 0, 0, 0, 0, 0, 0x3, (byte) 0xE9, 0x66, 0x72, 0x65, 0x71, 0, 0, 0, 0, 0x43, (byte) 0xDC, 0, 0};

		final OSCMessage packet = (OSCMessage) converter.convert(bytes, bytes.length);
		checkAddress("/s_new", packet.getAddress());
		final List<Object> arguments = packet.getArguments();
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
		final byte[] bytes = {0x23, 0x62, 0x75, 0x6E, 0x64, 0x6C, 0x65, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0x0C, 0X2F, 0x74, 0x65, 0x73, 0x74, 0, 0, 0, 0x2C, 0, 0, 0};

		final OSCBundle bundle = (OSCBundle) converter.convert(bytes, bytes.length);
		if (!bundle.getTimestamp().equals(new Date(0))) {
			Assert.fail("Timestamp should be 0, but is " + bundle.getTimestamp());
		}
		final List<OSCPacket> packets = bundle.getPackets();
		if (packets.size() != 1) {
			Assert.fail("Num packets should be 1, but is " + packets.size());
		}
		final OSCMessage message = (OSCMessage) packets.get(0);
		checkAddress("/test", message.getAddress());
	}
}
