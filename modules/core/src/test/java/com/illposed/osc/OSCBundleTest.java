/*
 * Copyright (C) 2003, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import com.illposed.osc.argument.OSCTimeStamp;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 * @see OSCBundle
 */
public class OSCBundleTest {

	private void sendBundleTimestampTestHelper(
			final OSCBundle bundle,
			final OSCTimeStamp expectedTimestamp)
			throws IOException, OSCSerializeException
	{
		final OSCBundle reparsedBundle = OSCReparserTest.reparse(bundle);
		if (!reparsedBundle.getTimestamp().equals(expectedTimestamp)) {
			Assert.fail("Send Bundle did not receive the correct timestamp "
					+ reparsedBundle.getTimestamp().getNtpTime()
					+ " (should be " + expectedTimestamp.getNtpTime() + ")");
		}
		List<OSCPacket> packets = reparsedBundle.getPackets();
		OSCMessage msg = (OSCMessage) packets.get(0);
		if (!msg.getAddress().equals("/dummy")) {
			Assert.fail("Send Bundle's message did not receive the correct address");
		}
	}

	@Test
	public void testSendBundle() throws IOException, OSCSerializeException {
		final Date timeNow = GregorianCalendar.getInstance().getTime();
		final OSCTimeStamp timestampNow = OSCTimeStamp.valueOf(timeNow);
		List<OSCPacket> packetsSent = new ArrayList<OSCPacket>(1);
		packetsSent.add(new OSCMessage("/dummy"));
		OSCBundle bundle = new OSCBundle(packetsSent, timestampNow);
		sendBundleTimestampTestHelper(bundle, timestampNow);
	}

	@Test
	public void testSendBundleImmediate() throws IOException, OSCSerializeException {
		List<OSCPacket> packetsSent = new ArrayList<OSCPacket>(1);
		packetsSent.add(new OSCMessage("/dummy"));
		OSCBundle bundle = new OSCBundle(packetsSent);
		sendBundleTimestampTestHelper(bundle, OSCTimeStamp.IMMEDIATE);
	}

	@Test
	public void testSendBundleImmediateExplicit() throws IOException, OSCSerializeException {
		final Date timeNow = GregorianCalendar.getInstance().getTime();
		final OSCTimeStamp timestampNow = OSCTimeStamp.valueOf(timeNow);
		List<OSCPacket> packetsSent = new ArrayList<OSCPacket>(1);
		packetsSent.add(new OSCMessage("/dummy"));
		OSCBundle bundle = new OSCBundle(packetsSent, timestampNow);
		bundle.setTimestamp(OSCTimeStamp.IMMEDIATE);
		sendBundleTimestampTestHelper(bundle, OSCTimeStamp.IMMEDIATE);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSendBundleImmediateExplicitNull() throws IOException, OSCSerializeException {
		final Date timeNow = GregorianCalendar.getInstance().getTime();
		final OSCTimeStamp timestampNow = OSCTimeStamp.valueOf(timeNow);
		List<OSCPacket> packetsSent = new ArrayList<OSCPacket>(1);
		packetsSent.add(new OSCMessage("/dummy"));
		OSCBundle bundle = new OSCBundle(packetsSent, timestampNow);
		bundle.setTimestamp(null); // should throw IllegalArgumentException
		sendBundleTimestampTestHelper(bundle, OSCTimeStamp.IMMEDIATE);
	}

	@Test
	public void testSendMultiLevelBundle() throws IOException, OSCSerializeException {

		// create this structure:
		// bundle-0
		//   > message-0
		//   > bundle-1
		//     > message-1
		//     > bundle-2
		//       > message-2
		//       > message-3

		final List<OSCPacket> packetsBundle2 = new ArrayList<OSCPacket>(2);
		packetsBundle2.add(new OSCMessage("/leaf/2"));
		packetsBundle2.add(new OSCMessage("/leaf/3"));
		final OSCBundle bundle2 = new OSCBundle(packetsBundle2);

		final List<OSCPacket> packetsBundle1 = new ArrayList<OSCPacket>(2);
		packetsBundle1.add(new OSCMessage("/leaf/1"));
		packetsBundle1.add(bundle2);
		final OSCBundle bundle1 = new OSCBundle(packetsBundle1);

		final List<OSCPacket> packetsBundle0 = new ArrayList<OSCPacket>(2);
		packetsBundle0.add(new OSCMessage("/leaf/0"));
		packetsBundle0.add(bundle1);
		final OSCBundle bundle0 = new OSCBundle(packetsBundle0);

		OSCReparserTest.reparse(bundle0);
		// TODO make a smarter test, where we serialize, re-parse and then check the whole thing
	}
}
