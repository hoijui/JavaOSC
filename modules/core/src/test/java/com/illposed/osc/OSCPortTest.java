/*
 * Copyright (C) 2001, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class OSCPortTest extends junit.framework.TestCase {

	private static final long WAIT_FOR_SOCKET_CLOSE = 30;

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
		receiver.close();
		sender.close();
		// wait a bit after closing the receiver,
		// because (some) operating systems need some time
		// to actually close the underlying socket
		Thread.sleep(WAIT_FOR_SOCKET_CLOSE);
		super.tearDown();
	}

	public void testSocketClose() throws Exception {

		// close the underlying sockets
		receiver.close();
		sender.close();

		// make sure the old receiver is gone for good
		Thread.sleep(WAIT_FOR_SOCKET_CLOSE);

		// check if the underlying sockets were closed
		// NOTE We can have many (out-)sockets sending
		//   on the same address and port,
		//   but only one receiving per each such tuple.
		sender = new OSCPortOut();
		receiver = new OSCPortIn(OSCPort.defaultSCOSCPort());
	}

	public void testSocketAutoClose() throws Exception {

		// DANGEROUS! here we forget to close the underlying sockets!
		receiver = null;
		sender = null;

		// make sure the old receiver is gone for good
		System.gc();
		Thread.sleep(WAIT_FOR_SOCKET_CLOSE);

		// check if the underlying sockets were closed
		// NOTE We can have many (out-)sockets sending
		//   on the same address and port,
		//   but only one receiving per each such tuple.
		sender = new OSCPortOut();
		receiver = new OSCPortIn(OSCPort.defaultSCOSCPort());
	}

	public void testPorts() throws Exception {

		assertEquals("Bad default SuperCollider OSC port",
				57110, OSCPort.defaultSCOSCPort());
		assertEquals("Bad default SuperCollider Language OSC port",
				57120, OSCPort.defaultSCLangOSCPort());

		assertEquals("Bad default port with ctor()",
				57110, sender.getPort());

		sender.close();
		sender = new OSCPortOut(InetAddress.getLocalHost());
		assertEquals("Bad default port with ctor(address)",
				57110, sender.getPort());

		sender.close();
		sender = new OSCPortOut(InetAddress.getLocalHost(), 12345);
		assertEquals("Bad port with ctor(address, port)",
				12345, sender.getPort());
	}

	public void testStart() throws Exception {
		OSCMessage mesg = new OSCMessage("/sc/stop");
		sender.send(mesg);
	}

	public void testMessageWithArgs() throws Exception {
		List<Object> args = new ArrayList<Object>(2);
		args.add(3);
		args.add("hello");
		OSCMessage mesg = new OSCMessage("/foo/bar", args);
		sender.send(mesg);
	}

	public void testBundle() throws Exception {
		List<Object> args = new ArrayList<Object>(2);
		args.add(3);
		args.add("hello");
		List<OSCPacket> msgs = new ArrayList<OSCPacket>(1);
		msgs.add(new OSCMessage("/foo/bar", args));
		OSCBundle bundle = new OSCBundle(msgs);
		sender.send(bundle);
	}

	public void testBundle2() throws Exception {
		OSCMessage mesg = new OSCMessage("/foo/bar");
		mesg.addArgument(3);
		mesg.addArgument("hello");
		OSCBundle bundle = new OSCBundle();
		bundle.addPacket(mesg);
		sender.send(bundle);
	}

	public void testReceiving() throws Exception {
		OSCMessage mesg = new OSCMessage("/message/receiving");
		SimpleOSCListener listener = new SimpleOSCListener();
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
		SimpleOSCListener listener = new SimpleOSCListener();
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
