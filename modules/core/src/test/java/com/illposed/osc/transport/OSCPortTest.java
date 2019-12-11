/*
 * Copyright (C) 2001, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.transport;

import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCMessageTest;
import com.illposed.osc.OSCPacket;
import com.illposed.osc.OSCPacketListener;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.SimpleOSCMessageListener;
import com.illposed.osc.SimpleOSCPacketListener;
import com.illposed.osc.argument.OSCTimeTag64;
import com.illposed.osc.messageselector.OSCPatternAddressMessageSelector;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OSCPortTest {

	private static final long WAIT_FOR_SOCKET_CLOSE = 30;

	private final Logger log = LoggerFactory.getLogger(OSCPortTest.class);

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private OSCPortOut sender;
	private OSCPortIn receiver;
	private OSCPacketListener listener;

	private static boolean supportsIPv6() throws SocketException {
		return Stream.of(NetworkInterface.getNetworkInterfaces().nextElement())
				.map(NetworkInterface::getInterfaceAddresses)
				.flatMap(Collection::stream)
				.map(InterfaceAddress::getAddress)
				.anyMatch(((Predicate<InetAddress>) InetAddress::isLoopbackAddress).negate().and(address -> address instanceof Inet6Address));
	}

	private void setUp(
			final int portSenderOut,   // sender.local
			final int portSenderIn,    // receiver.remote
			final int portReceiverOut, // sender.remote
			final int portReceiverIn,  // receiver.local
			final OSCPacketListener packetListener,
			final NetworkProtocol protocol)
			throws Exception
	{
		final SocketAddress senderOutAddress = new InetSocketAddress(portSenderOut);
		final SocketAddress senderInAddress = new InetSocketAddress(portSenderIn);
		final SocketAddress receiverOutAddress = new InetSocketAddress(portReceiverOut);
		final SocketAddress receiverInAddress = new InetSocketAddress(portReceiverIn);

		if (receiver != null) {
			receiver.close();
		}

		OSCPortInBuilder builder = new OSCPortInBuilder()
			.setLocalSocketAddress(receiverInAddress)
			.setRemoteSocketAddress(senderInAddress)
			.setNetworkProtocol(protocol);

		if (packetListener != null) {
			builder.setPacketListener(packetListener);
		}

		receiver = builder.build();
		listener = packetListener;

		if (sender != null) {
			sender.close();
		}
		sender = new OSCPortOutBuilder()
			.setRemoteSocketAddress(receiverOutAddress)
			.setLocalSocketAddress(senderOutAddress)
			.setNetworkProtocol(protocol)
		  .build();
	}

	private void setUp(
			final int portSenderOut,
			final int portSenderIn,
			final int portReceiverOut,
			final int portReceiverIn,
			final OSCPacketListener packetListener)
			throws Exception
	{
		setUp(
			portSenderOut,
			portSenderIn,
			portReceiverOut,
			portReceiverIn,
			packetListener,
			NetworkProtocol.UDP
		);
	}

	private void setUp(final int portSender, final int portReceiver) throws Exception {
		setUp(portSender, portSender, portReceiver, portReceiver, null);
	}

	private void setUp(final int portReceiver) throws Exception {
		setUp(0, portReceiver);
	}

	@After
	public void tearDown() throws Exception {
		// There is at least one test that sets `sender` and `receiver` to null for
		// testing purposes. To avoid a NPE, we check for null here and return
		// early.
		if (receiver == null && sender == null) {
			return;
		}

		try {
			if (receiver.isConnected()) { // HACK This should not be required, as DatagramChannel#disconnect() is supposed to have no effect if a it is not connected, but in certain tests, removing this if clause makes the disconnect call hang forever; could even be a JVM bug -> we should report that (requires a minimal example first, though)
				receiver.disconnect();
			}
		} catch (final IOException ex) {
			log.error("Failed to disconnect test OSC in port", ex);
		}
		try {
			sender.disconnect();
		} catch (final IOException ex) {
			log.error("Failed to disconnect test OSC out port", ex);
		}

		try {
			receiver.close();
		} catch (final IOException ex) {
			log.error("Failed to close test OSC in port", ex);
		}
		try {
			sender.close();
		} catch (final IOException ex) {
			log.error("Failed to close test OSC out port", ex);
		}

		// wait a bit after closing the receiver,
		// because (some) operating systems need some time
		// to actually close the underlying socket
		Thread.sleep(WAIT_FOR_SOCKET_CLOSE);
	}

	private void testSocketClose(NetworkProtocol protocol) throws Exception {
		setUp(
			0,
			0,
			OSCPort.defaultSCOSCPort(),
			OSCPort.defaultSCOSCPort(),
			null,
			protocol
		);

		receiver.close();
		sender.close();

		// make sure the old receiver is gone for good
		Thread.sleep(WAIT_FOR_SOCKET_CLOSE);

		// check if the underlying sockets were closed
		// NOTE We can have many (out-)sockets sending
		//   on the same address and port,
		//   but only one receiving per each such tuple.
		sender = new OSCPortOutBuilder()
			.setRemotePort(OSCPort.defaultSCOSCPort())
			.setLocalPort(0)
			.setNetworkProtocol(protocol)
			.build();

		receiver = new OSCPortInBuilder()
			.setLocalPort(OSCPort.defaultSCOSCPort())
			.setRemotePort(0)
			.setNetworkProtocol(protocol)
			.build();
	}

	@Test
	public void testSocketCloseUDP() throws Exception {
		testSocketClose(NetworkProtocol.UDP);
	}

	@Test
	@Ignore
	public void testSocketCloseTCP() throws Exception {
		testSocketClose(NetworkProtocol.TCP);
	}

	private void testSocketAutoClose(NetworkProtocol protocol) throws Exception {
		setUp(
			0,
			0,
			OSCPort.defaultSCOSCPort(),
			OSCPort.defaultSCOSCPort(),
			null,
			protocol
		);

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
		sender = new OSCPortOutBuilder()
			.setRemotePort(OSCPort.defaultSCOSCPort())
			.setLocalPort(0)
			.setNetworkProtocol(protocol)
			.build();

		receiver = new OSCPortInBuilder()
			.setLocalPort(OSCPort.defaultSCOSCPort())
			.setRemotePort(0)
			.setNetworkProtocol(protocol)
			.build();
	}

	@Test
	public void testSocketAutoCloseUDP() throws Exception {
		testSocketAutoClose(NetworkProtocol.UDP);
	}

	@Test
	@Ignore
	public void testSocketAutoCloseTCP() throws Exception {
		testSocketAutoClose(NetworkProtocol.TCP);
	}

	private void testReceivingImpl() throws Exception {
		OSCMessage message = new OSCMessage("/message/receiving");
		SimpleOSCMessageListener listener = new SimpleOSCMessageListener();
		receiver.getDispatcher().addListener(new OSCPatternAddressMessageSelector("/message/receiving"),
				listener);
		receiver.startListening();
		sender.send(message);
		Thread.sleep(100); // wait a bit
		receiver.stopListening();
		if (!listener.isMessageReceived()) {
			Assert.fail("Message was not received");
		}
	}

	private void testReceiving(NetworkProtocol protocol) throws Exception {
		setUp(
			0,
			0,
			OSCPort.defaultSCOSCPort(),
			OSCPort.defaultSCOSCPort(),
			null,
			protocol
		);

		testReceivingImpl();
	}

	@Test
	public void testReceivingUDP() throws Exception {
		testReceiving(NetworkProtocol.UDP);
	}

	@Test
	@Ignore
	public void testReceivingTCP() throws Exception {
		testReceiving(NetworkProtocol.TCP);
	}

	private void testReceivingLoopback(
		final InetAddress loopbackAddress, NetworkProtocol protocol)
		throws Exception
	{
		final InetSocketAddress loopbackSocket =
			new InetSocketAddress(loopbackAddress, OSCPort.defaultSCOSCPort());

		final InetSocketAddress wildcardSocket =
			new InetSocketAddress(OSCPort.generateWildcard(loopbackSocket), 0);

		sender = new OSCPortOutBuilder()
			.setRemoteSocketAddress(loopbackSocket)
			.setLocalSocketAddress(wildcardSocket)
			.setNetworkProtocol(protocol)
			.build();

		receiver = new OSCPortInBuilder()
			.setLocalSocketAddress(loopbackSocket)
			.setRemoteSocketAddress(wildcardSocket)
			.setNetworkProtocol(protocol)
			.build();

		testReceivingImpl();
	}

	@Test
	public void testReceivingLoopbackIPv4UDP() throws Exception {
		final InetAddress loopbackAddress = InetAddress.getByName("127.0.0.1");
		testReceivingLoopback(loopbackAddress, NetworkProtocol.UDP);
	}

	@Test
	@Ignore
	public void testReceivingLoopbackIPv4TCP() throws Exception {
		final InetAddress loopbackAddress = InetAddress.getByName("127.0.0.1");
		testReceivingLoopback(loopbackAddress, NetworkProtocol.TCP);
	}

	@Test
	public void testReceivingLoopbackIPv6UDP() throws Exception {
		if (supportsIPv6()) {
			final InetAddress loopbackAddress = InetAddress.getByName("::1");
			testReceivingLoopback(loopbackAddress, NetworkProtocol.UDP);
		} else {
			log.warn("Skipping IPv6 test because: No IPv6 support available on this system");
		}
	}

	@Test
	@Ignore
	public void testReceivingLoopbackIPv6TCP() throws Exception {
		if (supportsIPv6()) {
			final InetAddress loopbackAddress = InetAddress.getByName("::1");
			testReceivingLoopback(loopbackAddress, NetworkProtocol.TCP);
		} else {
			log.warn("Skipping IPv6 test because: No IPv6 support available on this system");
		}
	}

	private void testReceivingBroadcast(
		NetworkProtocol protocol)
		throws Exception
	{
		sender = new OSCPortOutBuilder()
			.setRemoteSocketAddress(
				new InetSocketAddress(
					InetAddress.getByName("255.255.255.255"),
					OSCPort.defaultSCOSCPort()
				)
			)
			.setLocalPort(0)
			.setNetworkProtocol(protocol)
			.build();

		receiver = new OSCPortInBuilder()
			.setLocalPort(OSCPort.defaultSCOSCPort())
			.setRemotePort(0)
			.setNetworkProtocol(protocol)
			.build();

		testReceivingImpl();
	}

	@Test
	public void testReceivingBroadcastUDP() throws Exception {
		testReceivingBroadcast(NetworkProtocol.UDP);
	}

	@Test
	@Ignore
	public void testReceivingBroadcastTCP() throws Exception {
		testReceivingBroadcast(NetworkProtocol.UDP);
	}

	private void testStart(NetworkProtocol protocol) throws Exception {
		setUp(
			0,
			0,
			OSCPort.defaultSCOSCPort(),
			OSCPort.defaultSCOSCPort(),
			null,
			protocol
		);

		OSCMessage message = new OSCMessage("/sc/stop");
		sender.send(message);
	}

	@Test
	public void testStartUDP() throws Exception {
		testStart(NetworkProtocol.UDP);
	}

	@Test
	@Ignore
	public void testStartTCP() throws Exception {
		testStart(NetworkProtocol.TCP);
	}

	private void testMessageWithArgs(NetworkProtocol protocol) throws Exception {
		setUp(
			0,
			0,
			OSCPort.defaultSCOSCPort(),
			OSCPort.defaultSCOSCPort(),
			null,
			protocol
		);

		List<Object> args = new ArrayList<>(2);
		args.add(3);
		args.add("hello");
		OSCMessage message = new OSCMessage("/foo/bar", args);
		sender.send(message);
	}

	@Test
	public void testMessageWithArgsUDP() throws Exception {
		testMessageWithArgs(NetworkProtocol.UDP);
	}

	@Test
	@Ignore
	public void testMessageWithArgsTCP() throws Exception {
		testMessageWithArgs(NetworkProtocol.TCP);
	}

	private void testBundle(NetworkProtocol protocol) throws Exception {
		setUp(
			0,
			0,
			OSCPort.defaultSCOSCPort(),
			OSCPort.defaultSCOSCPort(),
			null,
			protocol
		);

		List<Object> args = new ArrayList<>(2);
		args.add(3);
		args.add("hello");
		List<OSCPacket> messages = new ArrayList<>(1);
		messages.add(new OSCMessage("/foo/bar", args));
		OSCBundle bundle = new OSCBundle(messages);
		sender.send(bundle);
	}

	@Test
	public void testBundleUDP() throws Exception {
		testBundle(NetworkProtocol.UDP);
	}

	@Test
	@Ignore
	public void testBundleTCP() throws Exception {
		testBundle(NetworkProtocol.TCP);
	}

	private void testBundle2(NetworkProtocol protocol) throws Exception {
		setUp(
			0,
			0,
			OSCPort.defaultSCOSCPort(),
			OSCPort.defaultSCOSCPort(),
			null,
			protocol
		);

		final List<Object> arguments = new ArrayList<>(2);
		arguments.add(3);
		arguments.add("hello");
		final OSCMessage message = new OSCMessage("/foo/bar", arguments);
		OSCBundle bundle = new OSCBundle();
		bundle.addPacket(message);
		sender.send(bundle);
	}

	@Test
	public void testBundle2UDP() throws Exception {
		testBundle2(NetworkProtocol.UDP);
	}

	@Test
	@Ignore
	public void testBundle2TCP() throws Exception {
		testBundle2(NetworkProtocol.TCP);
	}

	private void testBundleReceiving(NetworkProtocol protocol) throws Exception {
		setUp(
			0,
			0,
			OSCPort.defaultSCOSCPort(),
			OSCPort.defaultSCOSCPort(),
			null,
			protocol
		);

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
		if (!listener.getReceivedEvent().getTime().equals(bundle.getTimestamp())) {
			Assert.fail("Message should have timestamp " + bundle.getTimestamp()
					+ " but has " + listener.getReceivedEvent().getTime());
		}
	}

	@Test
	public void testBundleReceivingUDP() throws Exception {
		testBundleReceiving(NetworkProtocol.UDP);
	}

	@Test
	@Ignore
	public void testBundleReceivingTCP() throws Exception {
		testBundleReceiving(NetworkProtocol.TCP);
	}

	private void testLowLevelBundleReceiving(
		NetworkProtocol protocol)
		throws Exception
	{
		setUp(
			0,
			0,
			OSCPort.defaultSCOSCPort(),
			OSCPort.defaultSCOSCPort(),
			new SimpleOSCPacketListener(),
			protocol
		);

		SimpleOSCPacketListener simpleListener = (SimpleOSCPacketListener)listener;

		receiver.startListening();

		OSCBundle bundle = new OSCBundle();
		bundle.addPacket(new OSCMessage("/low-level/bundle/receiving"));
		sender.send(bundle);
		Thread.sleep(100); // wait a bit

		receiver.stopListening();

		if (!simpleListener.isMessageReceived()) {
			Assert.fail("Message was not received");
		}

		OSCBundle packet = (OSCBundle)simpleListener.getReceivedPacket();
		OSCTimeTag64 timeTag = packet.getTimestamp();

		if (!timeTag.equals(bundle.getTimestamp())) {
			Assert.fail(
				"Message should have timestamp " +
				bundle.getTimestamp() +
				" but has " +
				timeTag
			);
		}
	}

	@Test
	public void testLowLevelBundleReceivingUDP() throws Exception {
		testLowLevelBundleReceiving(NetworkProtocol.UDP);
	}

	@Test
	@Ignore
	public void testLowLevelBundleReceivingTCP() throws Exception {
		testLowLevelBundleReceiving(NetworkProtocol.TCP);
	}

	private void testLowLevelMessageReceiving(
		NetworkProtocol protocol)
		throws Exception
	{
		setUp(
			0,
			0,
			OSCPort.defaultSCOSCPort(),
			OSCPort.defaultSCOSCPort(),
			new SimpleOSCPacketListener(),
			protocol
		);

		SimpleOSCPacketListener simpleListener = (SimpleOSCPacketListener)listener;

		receiver.startListening();

		String expectedAddress = "/low-level/message/receiving";

		OSCMessage message = new OSCMessage(expectedAddress);
		sender.send(message);
		Thread.sleep(100); // wait a bit

		receiver.stopListening();

		if (!simpleListener.isMessageReceived()) {
			Assert.fail("Message was not received");
		}

		OSCMessage packet = (OSCMessage)simpleListener.getReceivedPacket();
		String actualAddress = packet.getAddress();

		if (!expectedAddress.equals(actualAddress)) {
			Assert.fail(
				"Message should have address " +
				expectedAddress +
				" but has address" +
				actualAddress
			);
		}
	}

	@Test
	public void testLowLevelMessageReceivingUDP() throws Exception {
		testLowLevelMessageReceiving(NetworkProtocol.UDP);
	}

	@Test
	@Ignore
	public void testLowLevelMessageReceivingTCP() throws Exception {
		testLowLevelMessageReceiving(NetworkProtocol.TCP);
	}

	private void testLowLevelRemovingPacketListener(
		NetworkProtocol protocol)
		throws Exception
	{
		setUp(
			0,
			0,
			OSCPort.defaultSCOSCPort(),
			OSCPort.defaultSCOSCPort(),
			null,
			protocol
		);

		SimpleOSCPacketListener listener = new SimpleOSCPacketListener();
		receiver.addPacketListener(listener);
		receiver.removePacketListener(listener);
		receiver.startListening();

		OSCMessage message = new OSCMessage("/low-level/removing-packet-listener");
		sender.send(message);
		Thread.sleep(100); // wait a bit

		receiver.stopListening();

		if (listener.isMessageReceived()) {
			Assert.fail(
				"Message was received, despite removePacketListener having been called."
			);
		}
	}

	@Test
	public void testLowLevelRemovingPacketListenerUDP() throws Exception {
		testLowLevelRemovingPacketListener(NetworkProtocol.UDP);
	}

	@Test
	@Ignore
	public void testLowLevelRemovingPacketListenerTCP() throws Exception {
		testLowLevelRemovingPacketListener(NetworkProtocol.TCP);
	}

	/**
	 * @param size the approximate size of the resulting, serialized OSC packet in bytes
	 */
	private void testReceivingBySize(final int size) throws Exception {
		final String address = "/message/sized";
		final int numIntegerArgs = (size - (((address.length() + 3 + 1) / 4) * 4)) / 5;
		final List<Object> args = new ArrayList<>(numIntegerArgs);
		final Random random = new Random();
		for (int ai = 0; ai < numIntegerArgs; ai++) {
			args.add(random.nextInt());
		}
		final OSCMessage message = new OSCMessage(address, args);
		final SimpleOSCMessageListener listener = new SimpleOSCMessageListener();
		receiver.getDispatcher().addListener(
				new OSCPatternAddressMessageSelector(message.getAddress()),
				listener);
		receiver.startListening();
		sender.send(message);
		Thread.sleep(100); // wait a bit
		receiver.stopListening();
		if (!listener.isMessageReceived()) {
			Assert.fail("Message was not received");
		}
		if (message.getArguments().size() != listener.getReceivedEvent().getMessage().getArguments().size()) {
			Assert.fail("Message received #arguments differs from #arguments sent");
		}
		if (!message.getArguments().get(numIntegerArgs - 1).equals(
				listener.getReceivedEvent().getMessage().getArguments().get(numIntegerArgs - 1)))
		{
			Assert.fail("Message received last argument '"
					+ message.getArguments().get(numIntegerArgs - 1)
					+ "' differs from the sent one '"
					+ listener.getReceivedEvent().getMessage().getArguments().get(numIntegerArgs - 1)
					+ '\'');
		}
	}

	@Test
	public void testReceivingBig() throws Exception {
		setUp(OSCPort.defaultSCOSCPort());

		// Create a list of arguments of size 1500 bytes,
		// so the resulting UDP packet size is sure to be bigger then the default maximum,
		// which is 1500 bytes (including headers).
		testReceivingBySize(1500);
	}

	@Test(expected=OSCSerializeException.class)
	public void testReceivingHuge() throws Exception {
		setUp(OSCPort.defaultSCOSCPort());

		// Create a list of arguments of size 66000 bytes,
		// so the resulting UDP packet size is sure to be bigger then the theoretical maximum,
		// which is 65k bytes (including headers).
		testReceivingBySize(66000);
	}

	@Test(expected=OSCSerializeException.class)
	public void testReceivingHugeConnectedOut() throws Exception {
		setUp(OSCPort.defaultSCOSCPort());

		// Create a list of arguments of size 66000 bytes,
		// so the resulting UDP packet size is sure to be bigger then the theoretical maximum,
		// which is 65k bytes (including headers).
		sender.connect();
		testReceivingBySize(66000);
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

	private void testBundleReceivingConnectedOut(
		NetworkProtocol protocol)
		throws Exception
	{
		setUp(
			0,
			0,
			OSCPort.defaultSCOSCPort(),
			OSCPort.defaultSCOSCPort(),
			// OSCPort.defaultSCOSCPort(),
			// OSCPort.defaultSCOSCPort(),
			// OSCPort.defaultSCOSCPort() + 1,
			// OSCPort.defaultSCOSCPort() + 1,
			null,
			protocol
		);

		sender.connect();
		testBundleReceiving(true);
	}

	@Test
	public void testBundleReceivingConnectedOutUDP() throws Exception {
		testBundleReceivingConnectedOut(NetworkProtocol.UDP);
	}

	@Test
	@Ignore
	public void testBundleReceivingConnectedOutTCP() throws Exception {
		testBundleReceivingConnectedOut(NetworkProtocol.TCP);
	}

	private void testBundleReceivingConnectedOutDifferentSender(
		NetworkProtocol protocol)
		throws Exception
	{
		setUp(
			// sender.local != receiver.remote
			OSCPort.defaultSCOSCPort(),
			OSCPort.defaultSCOSCPort() + 1,
			// sender.remote == receiver.local
			OSCPort.defaultSCOSCPort() + 2,
			OSCPort.defaultSCOSCPort() + 2,
			null,
			protocol
		);

		sender.connect();
		testBundleReceiving(true);
	}

	@Test
	public void testBundleReceivingConnectedOutDifferentSenderUDP()
	throws Exception
	{
		testBundleReceivingConnectedOutDifferentSender(NetworkProtocol.UDP);
	}

	@Test
	@Ignore
	public void testBundleReceivingConnectedOutDifferentSenderTCP()
	throws Exception
	{
		testBundleReceivingConnectedOutDifferentSender(NetworkProtocol.TCP);
	}

	private void testBundleReceivingConnectedOutDifferentReceiver(
		NetworkProtocol protocol)
		throws Exception
	{
		setUp(
			// sender.local == receiver.remote
			OSCPort.defaultSCOSCPort(),
			OSCPort.defaultSCOSCPort(),
			// sender.remote != receiver.local
			OSCPort.defaultSCOSCPort() + 1,
			OSCPort.defaultSCOSCPort() + 2,
			null,
			protocol
		);

		sender.connect();
		testBundleReceiving(false);
	}

	@Test
	public void testBundleReceivingConnectedOutDifferentReceiverUDP()
	throws Exception
	{
		testBundleReceivingConnectedOutDifferentReceiver(NetworkProtocol.UDP);
	}

	@Test
	@Ignore
	public void testBundleReceivingConnectedOutDifferentReceiverTCP()
	throws Exception
	{
		testBundleReceivingConnectedOutDifferentReceiver(NetworkProtocol.TCP);
	}

	private void testBundleReceivingConnectedIn(
		NetworkProtocol protocol)
		throws Exception
	{
		setUp(
			// sender.local == receiver.remote
			OSCPort.defaultSCOSCPort(),
			OSCPort.defaultSCOSCPort(),
			// sender.remote == receiver.local
			OSCPort.defaultSCOSCPort() + 1,
			OSCPort.defaultSCOSCPort() + 1,
			null,
			protocol
		);

		receiver.connect();
		testBundleReceiving(true);
	}

	@Test
	public void testBundleReceivingConnectedInUDP() throws Exception {
		testBundleReceivingConnectedIn(NetworkProtocol.UDP);
	}

	@Test
	@Ignore
	public void testBundleReceivingConnectedInTCP() throws Exception {
		testBundleReceivingConnectedIn(NetworkProtocol.TCP);
	}

	private void testBundleReceivingConnectedInDifferentSender(
		NetworkProtocol protocol)
		throws Exception
	{
		setUp(
			// sender.local != receiver.remote
			OSCPort.defaultSCOSCPort(),
			OSCPort.defaultSCOSCPort() + 1,
			// sender.remote == receiver.local
			OSCPort.defaultSCOSCPort() + 2,
			OSCPort.defaultSCOSCPort() + 2,
			null,
			protocol
		);

		receiver.connect();
		testBundleReceiving(false);
	}

	@Test
	public void testBundleReceivingConnectedInDifferentSenderUDP()
	throws Exception
	{
		testBundleReceivingConnectedInDifferentSender(NetworkProtocol.UDP);
	}

	@Test
	@Ignore
	public void testBundleReceivingConnectedInDifferentSenderTCP()
	throws Exception
	{
		testBundleReceivingConnectedInDifferentSender(NetworkProtocol.TCP);
	}

	private void testBundleReceivingConnectedInDifferentReceiver(
		NetworkProtocol protocol)
		throws Exception
	{
		setUp(
			// sender.local == receiver.remote
			OSCPort.defaultSCOSCPort(),
			OSCPort.defaultSCOSCPort(),
			// sender.remote != receiver.local
			OSCPort.defaultSCOSCPort() + 1,
			OSCPort.defaultSCOSCPort() + 2,
			null,
			protocol
		);

		receiver.connect();
		testBundleReceiving(false);
	}

	@Test
	public void testBundleReceivingConnectedInDifferentReceiverUDP()
	throws Exception
	{
		testBundleReceivingConnectedInDifferentReceiver(NetworkProtocol.UDP);
	}

	@Test
	@Ignore
	public void testBundleReceivingConnectedInDifferentReceiverTCP()
	throws Exception
	{
		testBundleReceivingConnectedInDifferentReceiver(NetworkProtocol.TCP);
	}

	private void testBundleReceivingConnectedBoth(
		NetworkProtocol protocol)
		throws Exception
	{
		setUp(
			// sender.local == receiver.remote
			OSCPort.defaultSCOSCPort(),
			OSCPort.defaultSCOSCPort(),
			// sender.remote == receiver.local
			OSCPort.defaultSCOSCPort() + 1,
			OSCPort.defaultSCOSCPort() + 1,
			null,
			protocol
		);

		receiver.connect();
		sender.connect();
		testBundleReceiving(true);
	}

	@Test
	public void testBundleReceivingConnectedBothUDP() throws Exception {
		testBundleReceivingConnectedBoth(NetworkProtocol.UDP);
	}

	@Test
	@Ignore
	public void testBundleReceivingConnectedBothTCP() throws Exception {
		testBundleReceivingConnectedBoth(NetworkProtocol.TCP);
	}

	/**
	 * Checks if buffers are correctly reset after receiving a message.
	 * @throws Exception if anything goes wrong
	 */
	private void testReceivingLongAfterShort(
		NetworkProtocol protocol)
		throws Exception
	{
		setUp(
			0,
			0,
			OSCPort.defaultSCOSCPort(),
			OSCPort.defaultSCOSCPort(),
			null,
			protocol
		);

		final OSCMessage msgShort = new OSCMessage("/msg/short");
		final List<Object> someArgs = new ArrayList<>(3);
		someArgs.add("all");
		someArgs.add("my");
		someArgs.add("args");
		final OSCMessage msgLong = new OSCMessage("/message/with/very/long/address/receiving", someArgs);
		final SimpleOSCMessageListener listener = new SimpleOSCMessageListener();
		receiver.getDispatcher().addListener(new OSCPatternAddressMessageSelector(
						"/message/with/very/long/address/receiving"),
				listener);
		receiver.startListening();
		sender.send(msgShort);
		sender.send(msgLong);
		Thread.sleep(100); // wait a bit
		receiver.stopListening();
		if (!listener.isMessageReceived()) {
			Assert.fail("Message was not received");
		}
	}

	@Test
	public void testReceivingLongAfterShortUDP() throws Exception {
		testReceivingLongAfterShort(NetworkProtocol.UDP);
	}

	@Test
	@Ignore
	public void testReceivingLongAfterShortTCP() throws Exception {
		testReceivingLongAfterShort(NetworkProtocol.TCP);
	}

	/**
	 * Checks if simultaneous use of packet- and message-listeners works.
	 * @throws Exception if anything goes wrong
	 */
	private void testReceivingMessageAndPacketListeners(
		NetworkProtocol protocol)
		throws Exception
	{
		setUp(
			0,
			0,
			OSCPort.defaultSCOSCPort(),
			OSCPort.defaultSCOSCPort(),
			null,
			protocol
		);

		final OSCMessage msg = new OSCMessage("/msg/short");
		final SimpleOSCPacketListener pkgListener = new SimpleOSCPacketListener();
		final SimpleOSCMessageListener msgListener = new SimpleOSCMessageListener();
		receiver.getDispatcher().addListener(new OSCPatternAddressMessageSelector(
				"/msg/short"),
				msgListener);
		receiver.addPacketListener(pkgListener);
		receiver.startListening();
		sender.send(msg);
		Thread.sleep(100); // wait a bit
		receiver.stopListening();
		if (!pkgListener.isMessageReceived()) {
			Assert.fail("Message was not received by the packet listener");
		}
		if (!msgListener.isMessageReceived()) {
			Assert.fail("Message was not received by the message listener");
		}
	}

	@Test
	public void testReceivingMessageAndPacketListenersUDP() throws Exception {
		testReceivingMessageAndPacketListeners(NetworkProtocol.UDP);
	}

	@Test
	@Ignore
	public void testReceivingMessageAndPacketListenersTCP() throws Exception {
		testReceivingMessageAndPacketListeners(NetworkProtocol.TCP);
	}

	private void testStopListeningAfterReceivingBadAddress(
		NetworkProtocol protocol)
		throws Exception
	{
		setUp(
			0,
			0,
			OSCPort.defaultSCOSCPort(),
			OSCPort.defaultSCOSCPort(),
			null,
			protocol
		);

		final OSCMessage msgBad = OSCMessageTest.createUncheckedAddressMessage(
				"bad", Collections.emptyList(), null);
		final OSCMessage msg = new OSCMessage("/message/receiving");
		final SimpleOSCMessageListener listener = new SimpleOSCMessageListener();
		receiver.getDispatcher().addListener(new OSCPatternAddressMessageSelector(
				"/message/receiving"),
				listener);
		receiver.setResilient(false);
		receiver.startListening();
		sender.send(msgBad);
		sender.send(msg);
		Thread.sleep(100); // wait a bit
		receiver.stopListening();
		if (listener.isMessageReceived()) {
			Assert.fail("Message was received, while it should not have been");
		}
	}

	@Test
	public void testStopListeningAfterReceivingBadAddressUDP() throws Exception {
		testStopListeningAfterReceivingBadAddress(NetworkProtocol.UDP);
	}

	@Test
	@Ignore
	public void testStopListeningAfterReceivingBadAddressTCP() throws Exception {
		testStopListeningAfterReceivingBadAddress(NetworkProtocol.TCP);
	}

	private void testListeningAfterBadAddress(
		NetworkProtocol protocol)
		throws Exception
	{
		setUp(
			0,
			0,
			OSCPort.defaultSCOSCPort(),
			OSCPort.defaultSCOSCPort(),
			null,
			protocol
		);

		final OSCMessage msgBad = OSCMessageTest.createUncheckedAddressMessage(
				"bad", Collections.emptyList(), null);
		final OSCMessage msg = new OSCMessage("/message/receiving");
		final SimpleOSCMessageListener listener = new SimpleOSCMessageListener();
		receiver.getDispatcher().addListener(new OSCPatternAddressMessageSelector(
				"/message/receiving"),
				listener);
		receiver.setResilient(true);
		receiver.startListening();
		sender.send(msgBad);
		sender.send(msg);
		Thread.sleep(100); // wait a bit
		receiver.stopListening();
		if (!listener.isMessageReceived()) {
			Assert.fail("Message was not received");
		}
	}

	@Test
	public void testListeningAfterBadAddressUDP() throws Exception {
		testListeningAfterBadAddress(NetworkProtocol.UDP);
	}

	@Test
	@Ignore
	public void testListeningAfterBadAddressTCP() throws Exception {
		testListeningAfterBadAddress(NetworkProtocol.TCP);
	}
}
