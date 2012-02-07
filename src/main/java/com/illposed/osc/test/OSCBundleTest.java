package com.illposed.osc.test;

import java.math.BigInteger;
import java.util.Date;
import java.util.GregorianCalendar;

import com.illposed.osc.*;
import com.illposed.osc.utility.*;

public class OSCBundleTest extends junit.framework.TestCase {

	public OSCBundleTest(String name) {
		super(name);
	}

	public void testSendBundle() {
		Date timestamp = GregorianCalendar.getInstance().getTime();
		OSCBundle bundle = 
			new OSCBundle(
				new OSCPacket[] { new OSCMessage("/dummy") },
				timestamp);
		byte[] byteArray = bundle.getByteArray();
		OSCByteArrayToJavaConverter converter = new OSCByteArrayToJavaConverter();
		OSCBundle packet = (OSCBundle) converter.convert(byteArray, byteArray.length);
		if (!packet.getTimestamp().equals(timestamp))
			fail("Send Bundle did not receive the correct timestamp " + packet.getTimestamp()
				+ "(" + packet.getTimestamp().getTime() + 
				") (should be " + timestamp +"( " + timestamp.getTime() + ")) ");
		OSCPacket[] packets = packet.getPackets();
		OSCMessage msg = (OSCMessage) packets[0];
		if (!msg.getAddress().equals("/dummy"))
			fail("Send Bundle's message did not receive the correct address");
	}
	
	public void testSendBundleImmediate() {
		OSCBundle bundle = 
			new OSCBundle(new OSCPacket[] { new OSCMessage("/dummy") });
		byte[] byteArray = bundle.getByteArray();
		OSCByteArrayToJavaConverter converter = new OSCByteArrayToJavaConverter();
		OSCBundle packet = (OSCBundle) converter.convert(byteArray, byteArray.length);
		if (!packet.getTimestamp().equals(OSCBundle.TIMESTAMP_IMMEDIATE))
			fail("Timestamp should have been immediate, not " + packet.getTimestamp()
				+ "(" + packet.getTimestamp().getTime() + ")");
		OSCPacket[] packets = packet.getPackets();
		OSCMessage msg = (OSCMessage) packets[0];
		if (!msg.getAddress().equals("/dummy"))
			fail("Send Bundle's message did not receive the correct address");
	}
}