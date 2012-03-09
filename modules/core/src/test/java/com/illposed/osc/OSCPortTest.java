/*
 * Copyright (C) 2001, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import java.util.Date;

public class OSCPortTest extends junit.framework.TestCase {

	private boolean messageReceived;
	private Date    receivedTimestamp;
	private OSCPortOut sender;
	private OSCPortIn  receiver;

	public OSCPortTest(String name) {
		super(name);
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		sender = new OSCPortOut();
		receiver = new OSCPortIn(OSCPort.defaultSCOSCPort());
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		sender.close();
		receiver.close();
	}

	public void testStart() throws Exception {
		OSCMessage mesg = new OSCMessage("/sc/stop");
		sender.send(mesg);
	}

	public void testMessageWithArgs() throws Exception {
		Object args[] = new Object[2];
		args[0] = new Integer(3);
		args[1] = "hello";
		OSCMessage mesg = new OSCMessage("/foo/bar", args);
		sender.send(mesg);
	}

	public void testBundle() throws Exception {
		Object args[] = new Object[2];
		OSCPacket mesgs[] = new OSCPacket[1];
		args[0] = new Integer(3);
		args[1] = "hello";
		OSCMessage mesg = new OSCMessage("/foo/bar", args);
		mesgs[0] = mesg;
		OSCBundle bundle = new OSCBundle(mesgs);
		sender.send(bundle);
	}

	public void testBundle2() throws Exception {
		OSCMessage mesg = new OSCMessage("/foo/bar");
		mesg.addArgument(new Integer(3));
		mesg.addArgument("hello");
		OSCBundle bundle = new OSCBundle();
		bundle.addPacket(mesg);
		sender.send(bundle);
	}

	public void testReceiving() throws Exception {
		OSCMessage mesg = new OSCMessage("/message/receiving");
		messageReceived = false;
		OSCListener listener = new OSCListener() {
			public void acceptMessage(java.util.Date time, OSCMessage message) {
				messageReceived = true;
			}
		};
		receiver.addListener("/message/receiving", listener);
		receiver.startListening();
		sender.send(mesg);
		Thread.sleep(100); // wait a bit
		receiver.stopListening();
		if (!messageReceived)
			fail("Message was not received");
	}

	public void testBundleReceiving() throws Exception {
		OSCBundle bundle = new OSCBundle();
		bundle.addPacket(new OSCMessage("/bundle/receiving"));
		messageReceived = false;
		receivedTimestamp = null;
		OSCListener listener = new OSCListener() {
			public void acceptMessage(Date time, OSCMessage message) {
				messageReceived = true;
				receivedTimestamp = time;
			}
		};
		receiver.addListener("/bundle/receiving", listener);
		receiver.startListening();
		sender.send(bundle);
		Thread.sleep(100); // wait a bit
		receiver.stopListening();
		if (!messageReceived)
			fail("Message was not received");
		if (!receivedTimestamp.equals(bundle.getTimestamp()))
			fail("Message should have timestamp " + bundle.getTimestamp() + " but has " + receivedTimestamp);
	}
}
