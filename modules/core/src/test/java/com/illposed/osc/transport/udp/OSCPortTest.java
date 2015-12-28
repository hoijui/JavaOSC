/*
 * Copyright (C) 2001, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.transport.udp;

import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPacket;
import com.illposed.osc.OSCParserFactory;
import com.illposed.osc.OSCSerializerFactory;
import com.illposed.osc.SimpleOSCMessageListener;
import com.illposed.osc.messageselector.OSCPatternAddressMessageSelector;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
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

	private void reSetUp(
			final int portSenderOut,
			final int portSenderIn,
			final int portReceiverOut,
			final int portReceiverIn)
			throws Exception
	{
		final SocketAddress senderOutAddress = new InetSocketAddress(portSenderOut);
		final SocketAddress senderInAddress = new InetSocketAddress(portSenderIn);
		final SocketAddress receiverOutAddress = new InetSocketAddress(portReceiverOut);
		final SocketAddress receiverInAddress = new InetSocketAddress(portReceiverIn);

		if (receiver != null) {
			receiver.close();
		}
		receiver = new OSCPortIn(
				OSCParserFactory.createDefaultFactory(),
				receiverInAddress,
				senderInAddress);

		if (sender != null) {
			sender.close();
		}
		sender = new OSCPortOut(
				OSCSerializerFactory.createDefaultFactory(),
				receiverOutAddress,
				senderOutAddress);
	}

	private void reSetUp(final int portSender, final int portReceiver) throws Exception {
		reSetUp(portSender, portSender, portReceiver, portReceiver);
	}

	private void reSetUp(final int portReceiver) throws Exception {
		reSetUp(0, portReceiver);
	}

	@Before
	public void setUp() throws Exception {
		reSetUp(OSCPort.defaultSCOSCPort());
	}

	private void reSetUpDifferentPorts() throws Exception {
		reSetUp(OSCPort.defaultSCOSCPort(), OSCPort.defaultSCOSCPort() + 1);
	}

	private void reSetUpDifferentSender() throws Exception {
		reSetUp(
				OSCPort.defaultSCOSCPort(),
				OSCPort.defaultSCOSCPort() + 1,
				OSCPort.defaultSCOSCPort() + 2,
				OSCPort.defaultSCOSCPort() + 2);
	}

	private void reSetUpDifferentReceiver() throws Exception {
		reSetUp(
				OSCPort.defaultSCOSCPort(),
				OSCPort.defaultSCOSCPort(),
				OSCPort.defaultSCOSCPort() + 1,
				OSCPort.defaultSCOSCPort() + 2);
	}

	@After
	public void tearDown() throws Exception {

		receiver.disconnect();
		sender.disconnect();

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

		InetSocketAddress remoteAddress = ((InetSocketAddress) sender.getRemoteAddress());
		Assert.assertEquals("Bad default port with ctor()",
				57110, remoteAddress.getPort());

		sender.close();
		sender = new OSCPortOut(InetAddress.getLocalHost());
		remoteAddress = ((InetSocketAddress) sender.getRemoteAddress());
		Assert.assertEquals("Bad default port with ctor(address)",
				57110, remoteAddress.getPort());

		sender.close();
		sender = new OSCPortOut(InetAddress.getLocalHost(), 12345);
		remoteAddress = ((InetSocketAddress) sender.getRemoteAddress());
		Assert.assertEquals("Bad port with ctor(address, port)",
				12345, remoteAddress.getPort());
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

		final List<Object> arguments = new ArrayList<Object>(2);
		arguments.add(3);
		arguments.add("hello");
		final OSCMessage mesg = new OSCMessage("/foo/bar", arguments);
		OSCBundle bundle = new OSCBundle();
		bundle.addPacket(mesg);
		sender.send(bundle);
	}

	@Test
	public void testReceiving() throws Exception {

		OSCMessage mesg = new OSCMessage("/message/receiving");
		SimpleOSCMessageListener listener = new SimpleOSCMessageListener();
		receiver.getDispatcher().addListener(new OSCPatternAddressMessageSelector("/message/receiving"),
				listener);
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
		SimpleOSCMessageListener listener = new SimpleOSCMessageListener();
		receiver.getDispatcher().addListener(new OSCPatternAddressMessageSelector("/bundle/receiving"),
				listener);
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

	private void testBundleReceiving(final boolean shouldReceive) throws Exception {

		OSCBundle bundle = new OSCBundle();
		bundle.addPacket(new OSCMessage("/bundle/receiving"));
		SimpleOSCMessageListener listener = new SimpleOSCMessageListener();
		receiver.getDispatcher().addListener(new OSCPatternAddressMessageSelector("/bundle/receiving"),
				listener);
		receiver.startListening();

		sender.send(bundle);
		Thread.sleep(100); // wait a bit
		receiver.stopListening();

//		receiver.disconnect();

		if (shouldReceive && !listener.isMessageReceived()) {
			Assert.fail("Message was not received");
		} else if (!shouldReceive && listener.isMessageReceived()) {
			Assert.fail("Message was received while it should not have!");
		}
	}

	@Test
	public void testBundleReceivingConnectedOut() throws Exception {
//		reSetUpDifferentPorts();

		sender.connect();
		testBundleReceiving(true);
	}

	@Test
	public void testBundleReceivingConnectedOutDifferentSender() throws Exception {
		reSetUpDifferentSender();

		sender.connect();
		testBundleReceiving(true);
	}

	@Test
	public void testBundleReceivingConnectedOutDifferentReceiver() throws Exception {
		reSetUpDifferentReceiver();

		sender.connect();
		testBundleReceiving(false);
	}

	@Test
	public void testBundleReceivingConnectedIn() throws Exception {
		reSetUpDifferentPorts();

		receiver.connect();
		testBundleReceiving(true);
	}

	@Test
	public void testBundleReceivingConnectedInDifferentSender() throws Exception {
		reSetUpDifferentSender();

		receiver.connect();
		testBundleReceiving(false);
	}

	@Test
	public void testBundleReceivingConnectedInDifferentReceiver() throws Exception {
		reSetUpDifferentReceiver();

		receiver.connect();
		testBundleReceiving(false);
	}

	@Test
	public void testBundleReceivingConnectedBoth() throws Exception {
		reSetUpDifferentPorts();

		receiver.connect();
		sender.connect();
		testBundleReceiving(true);
	}
}
