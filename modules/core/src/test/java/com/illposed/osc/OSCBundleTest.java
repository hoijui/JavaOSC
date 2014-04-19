/*
 * Copyright (C) 2003, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import com.illposed.osc.utility.OSCByteArrayToJavaConverter;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Chandrasekhar Ramakrishnan
 * @see OSCBundle
 */
public class OSCBundleTest {

	private void sendBundleTimestampTestHelper(OSCBundle bundle, Date expectedTimestamp) {
		byte[] byteArray = bundle.getByteArray();
		OSCByteArrayToJavaConverter converter = new OSCByteArrayToJavaConverter();
		OSCBundle packet = (OSCBundle) converter.convert(byteArray, byteArray.length);
		if (!packet.getTimestamp().equals(expectedTimestamp)) {
			Assert.fail("Send Bundle did not receive the correct timestamp " + packet.getTimestamp()
				+ "(" + packet.getTimestamp().getTime() +
				") (should be " + expectedTimestamp +"( " + expectedTimestamp.getTime() + ")) ");
		}
		List<OSCPacket> packets = packet.getPackets();
		OSCMessage msg = (OSCMessage) packets.get(0);
		if (!msg.getAddress().equals("/dummy")) {
			Assert.fail("Send Bundle's message did not receive the correct address");
		}
	}

	@Test
	public void testSendBundle() {
		Date timestampNow = GregorianCalendar.getInstance().getTime();
		List<OSCPacket> packetsSent = new ArrayList<OSCPacket>(1);
		packetsSent.add(new OSCMessage("/dummy"));
		OSCBundle bundle = new OSCBundle(packetsSent, timestampNow);
		sendBundleTimestampTestHelper(bundle, timestampNow);
	}

	@Test
	public void testSendBundleImmediate() {
		List<OSCPacket> packetsSent = new ArrayList<OSCPacket>(1);
		packetsSent.add(new OSCMessage("/dummy"));
		OSCBundle bundle = new OSCBundle(packetsSent);
		sendBundleTimestampTestHelper(bundle, OSCBundle.TIMESTAMP_IMMEDIATE);
	}

	@Test
	public void testSendBundleImmediateExplicit() {
		Date timestampNow = GregorianCalendar.getInstance().getTime();
		List<OSCPacket> packetsSent = new ArrayList<OSCPacket>(1);
		packetsSent.add(new OSCMessage("/dummy"));
		OSCBundle bundle = new OSCBundle(packetsSent, timestampNow);
		bundle.setTimestamp(OSCBundle.TIMESTAMP_IMMEDIATE);
		sendBundleTimestampTestHelper(bundle, OSCBundle.TIMESTAMP_IMMEDIATE);
	}

	@Test
	public void testSendBundleImmediateExplicitNull() {
		Date timestampNow = GregorianCalendar.getInstance().getTime();
		List<OSCPacket> packetsSent = new ArrayList<OSCPacket>(1);
		packetsSent.add(new OSCMessage("/dummy"));
		OSCBundle bundle = new OSCBundle(packetsSent, timestampNow);
		bundle.setTimestamp(null);
		sendBundleTimestampTestHelper(bundle, OSCBundle.TIMESTAMP_IMMEDIATE);
	}
}
