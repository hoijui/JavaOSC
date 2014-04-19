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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class OSCPortTest {

	private static final long WAIT_FOR_SOCKET_CLOSE = 30;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private OSCPortOut sender;
	private OSCPortIn  receiver;

	@Before
	public void setUp() throws Exception {
		sender = new OSCPortOut();
		receiver = new OSCPortIn(OSCPort.defaultSCOSCPort());
	}

	@After
	public void tearDown() throws Exception {
		receiver.close();
		sender.close();
		// wait a bit after closing the receiver,
		// because (some) operating systems need some time
		// to actually close the underlying socket
		Thread.sleep(WAIT_FOR_SOCKET_CLOSE);
	}

	@Test
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

	@Test
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

	@Test
	public void testPorts() throws Exception {

		Assert.assertEquals("Bad default SuperCollider OSC port",
				57110, OSCPort.defaultSCOSCPort());
		Assert.assertEquals("Bad default SuperCollider Language OSC port",
				57120, OSCPort.defaultSCLangOSCPort());

		Assert.assertEquals("Bad default port with ctor()",
				57110, sender.getPort());

		sender.close();
		sender = new OSCPortOut(InetAddress.getLocalHost());
		Assert.assertEquals("Bad default port with ctor(address)",
				57110, sender.getPort());

		sender.close();
		sender = new OSCPortOut(InetAddress.getLocalHost(), 12345);
		Assert.assertEquals("Bad port with ctor(address, port)",
				12345, sender.getPort());
	}

	@Test
	public void testStart() throws Exception {
		OSCMessage mesg = new OSCMessage("/sc/stop");
		sender.send(mesg);
	}

	@Test
	public void testMessageWithArgs() throws Exception {
		List<Object> args = new ArrayList<Object>(2);
		args.add(3);
		args.add("hello");
		OSCMessage mesg = new OSCMessage("/foo/bar", args);
		sender.send(mesg);
	}

	@Test
	public void testMessageWithNullAddress() throws Exception {
		OSCMessage mesg = new OSCMessage(null);
		expectedException.expect(NullPointerException.class);
		sender.send(mesg);
	}

	@Test
	public void testBundle() throws Exception {
		List<Object> args = new ArrayList<Object>(2);
		args.add(3);
		args.add("hello");
		List<OSCPacket> msgs = new ArrayList<OSCPacket>(1);
		msgs.add(new OSCMessage("/foo/bar", args));
		OSCBundle bundle = new OSCBundle(msgs);
		sender.send(bundle);
	}

	@Test
	public void testBundle2() throws Exception {
		OSCMessage mesg = new OSCMessage("/foo/bar");
		mesg.addArgument(3);
		mesg.addArgument("hello");
		OSCBundle bundle = new OSCBundle();
		bundle.addPacket(mesg);
		sender.send(bundle);
	}

	@Test
	public void testReceiving() throws Exception {
		OSCMessage mesg = new OSCMessage("/message/receiving");
		SimpleOSCListener listener = new SimpleOSCListener();
		receiver.addListener("/message/receiving", listener);
		receiver.startListening();
		sender.send(mesg);
		Thread.sleep(100); // wait a bit
		receiver.stopListening();
		if (!listener.isMessageReceived()) {
			Assert.fail("Message was not received");
		}
	}

	@Test
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
			Assert.fail("Message was not received");
		}
		if (!listener.getReceivedTimestamp().equals(bundle.getTimestamp())) {
			Assert.fail("Message should have timestamp " + bundle.getTimestamp()
					+ " but has " + listener.getReceivedTimestamp());
		}
	}
}
