/*
 * Copyright (C) 2001, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OSCPortTest extends junit.framework.TestCase {

	private OSCPortOut sender;
	private OSCPortIn  receiver;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		sender = new OSCPortOut();
		receiver = new OSCPortIn(OSCPort.defaultSCOSCPort());
	}

	@Override
	protected void tearDown() throws Exception {
		sender.close();
		receiver.close();
		super.tearDown();
	}

	public void testStart() throws Exception {
		OSCMessage mesg = new OSCMessage("/sc/stop");
		sender.send(mesg);
	}

	public void testMessageWithArgs() throws Exception {
		List<Object> args = new ArrayList<Object>(2);
		args.add(new Integer(3));
		args.add("hello");
		OSCMessage mesg = new OSCMessage("/foo/bar", args);
		sender.send(mesg);
	}

	public void testBundle() throws Exception {
		List<Object> args = new ArrayList<Object>(2);
		args.add(new Integer(3));
		args.add("hello");
		List<OSCPacket> msgs = new ArrayList<OSCPacket>(1);
		msgs.add(new OSCMessage("/foo/bar", args));
		OSCBundle bundle = new OSCBundle(msgs);
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
		TestOSCListener listener = new TestOSCListener();
		receiver.addListener("/message/receiving", listener);
		receiver.startListening();
		sender.send(mesg);
		Thread.sleep(100); // wait a bit
		receiver.stopListening();
		if (!listener.isMessageReceived()) {
			fail("Message was not received");
		}
	}

	public void testBundleReceiving() throws Exception {
		OSCBundle bundle = new OSCBundle();
		bundle.addPacket(new OSCMessage("/bundle/receiving"));
		TestOSCListener listener = new TestOSCListener();
		receiver.addListener("/bundle/receiving", listener);
		receiver.startListening();
		sender.send(bundle);
		Thread.sleep(100); // wait a bit
		receiver.stopListening();
		if (!listener.isMessageReceived()) {
			fail("Message was not received");
		}
		if (!listener.getReceivedTimestamp().equals(bundle.getTimestamp())) {
			fail("Message should have timestamp " + bundle.getTimestamp()
					+ " but has " + listener.getReceivedTimestamp());
		}
	}
}
