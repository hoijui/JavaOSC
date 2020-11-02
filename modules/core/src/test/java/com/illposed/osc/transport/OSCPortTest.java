/*
 * Copyright (C) 2001-2020, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
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
import com.illposed.osc.transport.tcp.TCPTransport;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OSCPortTest {

	private static final long WAIT_FOR_SOCKET_CLOSE_MS = 30;
	private static final int WAIT_FOR_RECEIVE_MS = 1000;

	private final Logger log = LoggerFactory.getLogger(OSCPortTest.class);

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private OSCPortOut sender;
	private OSCPortIn receiver;
	private OSCPacketListener listener;

	private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(ie);
		}
	}

	private static boolean supportsIPv6() throws SocketException {
		return Stream.of(NetworkInterface.getNetworkInterfaces().nextElement())
				.map(NetworkInterface::getInterfaceAddresses)
				.flatMap(Collection::stream)
				.map(InterfaceAddress::getAddress)
				.anyMatch(((Predicate<InetAddress>) InetAddress::isLoopbackAddress).negate().and(address -> address instanceof Inet6Address));
	}

	private int findAvailablePort() throws IOException {
		try (ServerSocket socket = new ServerSocket(0)) {
			return socket.getLocalPort();
		}
	}

	private void retryUntilTrue(
			int interval, int timeout, String timeoutMsg, BooleanSupplier test)
			throws TimeoutException {
		long deadline = System.currentTimeMillis() + timeout;
		while (System.currentTimeMillis() < deadline) {
			if (test.getAsBoolean()) {
				return;
			}
			sleep(interval);
		}

		throw new TimeoutException(timeoutMsg);
	}

	private void assertEventuallyTrue(
			int interval, int timeout, String failureMsg, BooleanSupplier test) {
		try {
			retryUntilTrue(interval, timeout, "", test);
		} catch (TimeoutException te) {
			Assert.fail(failureMsg);
		}
	}

	private void assertNeverTrue(
			int interval, int timeout, String failureMsg, BooleanSupplier test) {
		try {
			retryUntilTrue(interval, timeout, "", test);
			Assert.fail(failureMsg);
		} catch (TimeoutException te) {
			// Reaching the timeout without `test` ever returning true is the success
			// condition, so we return successfully here.
		}
	}

	private boolean isTcpTransportListening(TCPTransport transport) {
		try {
			return transport.isListening();
		} catch (IOException ex) {
			return false;
		}
	}

	private void waitForSocketState(boolean open)
			throws IOException, TimeoutException {
		if (receiver.getTransport() instanceof TCPTransport) {
			// Wait until the receiver is listening.
			TCPTransport transport = (TCPTransport) receiver.getTransport();
			retryUntilTrue(
					100,
					WAIT_FOR_RECEIVE_MS,
					open
							? "Transport not listening."
							: "Transport still listening.",
					open
							? () -> isTcpTransportListening(transport)
							: () -> !isTcpTransportListening(transport)
			);
		} else {
			// wait a bit after closing the receiver, because (some) operating systems
			// need some time to actually close the underlying socket
			sleep(WAIT_FOR_SOCKET_CLOSE_MS);
		}
	}

	private void waitForSocketOpen() throws IOException, TimeoutException {
		waitForSocketState(true);
	}

	private void waitForSocketClose() throws IOException, TimeoutException {
		waitForSocketState(false);
	}

	private void setUp(
			final int portSenderOut,   // sender.local
			final int portSenderIn,    // receiver.remote
			final int portReceiverOut, // sender.remote
			final int portReceiverIn,  // receiver.local
			final OSCPacketListener packetListener,
			final NetworkProtocol protocol)
			throws Exception {
		final SocketAddress senderOutAddress = new InetSocketAddress(portSenderOut);
		final SocketAddress senderInAddress = new InetSocketAddress(portSenderIn);
		final SocketAddress receiverOutAddress = new InetSocketAddress(portReceiverOut);
		final SocketAddress receiverInAddress = new InetSocketAddress(portReceiverIn);

		if (receiver != null) {
			receiver.close();
			waitForSocketClose();
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

		if (protocol == NetworkProtocol.TCP) {
			receiver.startListening();
			waitForSocketOpen();
		}

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
			throws Exception {
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

		waitForSocketClose();
	}

	private void testSocketClose(NetworkProtocol protocol, int receiverPort)
			throws Exception {
		setUp(0, 0, receiverPort, receiverPort, null, protocol);

		receiver.close();
		sender.close();

		waitForSocketClose();

		// check if the underlying sockets were closed
		// NOTE We can have many (out-)sockets sending
		//   on the same address and port,
		//   but only one receiving per each such tuple.
		sender = new OSCPortOutBuilder()
				.setRemotePort(receiverPort)
				.setLocalPort(0)
				.setNetworkProtocol(protocol)
				.build();

		receiver = new OSCPortInBuilder()
				.setLocalPort(receiverPort)
				.setRemotePort(0)
				.setNetworkProtocol(protocol)
				.build();
	}

	@Test
	public void testSocketCloseUDP() throws Exception {
		testSocketClose(NetworkProtocol.UDP, OSCPort.defaultSCOSCPort());
	}

	@Test
	public void testSocketCloseTCP() throws Exception {
		testSocketClose(NetworkProtocol.TCP, findAvailablePort());
	}

	@Test
	public void testSocketAutoClose() throws Exception {
		setUp(
				0,
				0,
				OSCPort.defaultSCOSCPort(),
				OSCPort.defaultSCOSCPort(),
				null,
				NetworkProtocol.UDP
		);

		// DANGEROUS! here we forget to close the underlying sockets!
		receiver = null;
		sender = null;

		// make sure the old receiver is gone for good
		System.gc();
		Thread.sleep(WAIT_FOR_SOCKET_CLOSE_MS);

		// check if the underlying sockets were closed
		// NOTE We can have many (out-)sockets sending
		//   on the same address and port,
		//   but only one receiving per each such tuple.
		sender = new OSCPortOutBuilder()
				.setRemotePort(OSCPort.defaultSCOSCPort())
				.setLocalPort(0)
				.setNetworkProtocol(NetworkProtocol.UDP)
				.build();

		receiver = new OSCPortInBuilder()
				.setLocalPort(OSCPort.defaultSCOSCPort())
				.setRemotePort(0)
				.setNetworkProtocol(NetworkProtocol.UDP)
				.build();
	}

	private void assertMessageReceived(
			SimpleOSCMessageListener listener, int timeout) {
		assertEventuallyTrue(
				100,
				timeout,
				"Message was not received.",
				() -> listener.isMessageReceived()
		);
	}

	private void assertMessageNotReceived(
			SimpleOSCMessageListener listener, int timeout, String failMessage) {
		assertNeverTrue(
				100,
				timeout,
				failMessage,
				() -> listener.isMessageReceived()
		);
	}

	private void assertPacketReceived(
			SimpleOSCPacketListener listener, int timeout) {
		assertEventuallyTrue(
				100,
				timeout,
				"Packet was not received.",
				() -> listener.isMessageReceived()
		);
	}

	private void assertPacketNotReceived(
			SimpleOSCPacketListener listener, int timeout, String failMessage) {
		assertNeverTrue(
				100,
				timeout,
				failMessage,
				() -> listener.isMessageReceived()
		);
	}

	private void testReceivingImpl() throws Exception {
		OSCMessage message = new OSCMessage("/message/receiving");
		SimpleOSCMessageListener listener = new SimpleOSCMessageListener();
		receiver.getDispatcher().addListener(new OSCPatternAddressMessageSelector("/message/receiving"),
				listener);
		receiver.startListening();
		sender.send(message);
		try {
			assertMessageReceived(listener, WAIT_FOR_RECEIVE_MS);
		} finally {
			receiver.stopListening();
		}
	}

	private void testReceiving(NetworkProtocol protocol, int receiverPort)
			throws Exception {
		setUp(0, 0, receiverPort, receiverPort, null, protocol);
		testReceivingImpl();
	}

	@Test
	public void testReceivingUDP() throws Exception {
		testReceiving(NetworkProtocol.UDP, OSCPort.defaultSCOSCPort());
	}

	@Test
	public void testReceivingTCP() throws Exception {
		testReceiving(NetworkProtocol.TCP, findAvailablePort());
	}

	private void testReceivingLoopback(
			final InetAddress loopbackAddress,
			int loopbackPort,
			NetworkProtocol protocol)
			throws Exception {
		final InetSocketAddress loopbackSocket =
				new InetSocketAddress(loopbackAddress, loopbackPort);

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

		if (protocol == NetworkProtocol.TCP) {
			receiver.startListening();
			waitForSocketOpen();
		}

		testReceivingImpl();
	}

	@Test
	public void testReceivingLoopbackIPv4UDP() throws Exception {
		final InetAddress loopbackAddress = InetAddress.getByName("127.0.0.1");
		testReceivingLoopback(
				loopbackAddress, OSCPort.defaultSCOSCPort(), NetworkProtocol.UDP
		);
	}

	@Test
	public void testReceivingLoopbackIPv4TCP() throws Exception {
		final InetAddress loopbackAddress = InetAddress.getByName("127.0.0.1");
		testReceivingLoopback(
				loopbackAddress, findAvailablePort(), NetworkProtocol.TCP
		);
	}

	@Test
	public void testReceivingLoopbackIPv6UDP() throws Exception {
		if (supportsIPv6()) {
			final InetAddress loopbackAddress = InetAddress.getByName("::1");
			testReceivingLoopback(
					loopbackAddress, OSCPort.defaultSCOSCPort(), NetworkProtocol.UDP
			);
		} else {
			log.warn("Skipping IPv6 test because: No IPv6 support available on this system");
		}
	}

	@Test
	public void testReceivingLoopbackIPv6TCP() throws Exception {
		if (supportsIPv6()) {
			final InetAddress loopbackAddress = InetAddress.getByName("::1");
			testReceivingLoopback(
					loopbackAddress, findAvailablePort(), NetworkProtocol.TCP
			);
		} else {
			log.warn("Skipping IPv6 test because: No IPv6 support available on this system");
		}
	}

	@Test
	public void testReceivingBroadcast() throws Exception {
		sender = new OSCPortOutBuilder()
				.setRemoteSocketAddress(
						new InetSocketAddress(
								InetAddress.getByName("255.255.255.255"),
								OSCPort.defaultSCOSCPort()
						)
				)
				.setLocalPort(0)
				.setNetworkProtocol(NetworkProtocol.UDP)
				.build();

		receiver = new OSCPortInBuilder()
				.setLocalPort(OSCPort.defaultSCOSCPort())
				.setRemotePort(0)
				.setNetworkProtocol(NetworkProtocol.UDP)
				.build();

		testReceivingImpl();
	}

	private void testStart(NetworkProtocol protocol, int receiverPort)
			throws Exception {
		setUp(0, 0, receiverPort, receiverPort, null, protocol);
		OSCMessage message = new OSCMessage("/sc/stop");
		sender.send(message);
	}

	@Test
	public void testStartUDP() throws Exception {
		testStart(NetworkProtocol.UDP, OSCPort.defaultSCOSCPort());
	}

	@Test
	public void testStartTCP() throws Exception {
		testStart(NetworkProtocol.TCP, findAvailablePort());
	}

	private void testMessageWithArgs(NetworkProtocol protocol, int receiverPort)
			throws Exception {
		setUp(0, 0, receiverPort, receiverPort, null, protocol);
		List<Object> args = new ArrayList<>(2);
		args.add(3);
		args.add("hello");
		OSCMessage message = new OSCMessage("/foo/bar", args);
		sender.send(message);
	}

	@Test
	public void testMessageWithArgsUDP() throws Exception {
		testMessageWithArgs(NetworkProtocol.UDP, OSCPort.defaultSCOSCPort());
	}

	@Test
	public void testMessageWithArgsTCP() throws Exception {
		testMessageWithArgs(NetworkProtocol.TCP, findAvailablePort());
	}

	private void testBundle(NetworkProtocol protocol, int receiverPort)
			throws Exception {
		setUp(0, 0, receiverPort, receiverPort, null, protocol);
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
		testBundle(NetworkProtocol.UDP, OSCPort.defaultSCOSCPort());
	}

	@Test
	public void testBundleTCP() throws Exception {
		testBundle(NetworkProtocol.TCP, findAvailablePort());
	}

	private void testBundle2(NetworkProtocol protocol, int receiverPort)
			throws Exception {
		setUp(0, 0, receiverPort, receiverPort, null, protocol);
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
		testBundle2(NetworkProtocol.UDP, OSCPort.defaultSCOSCPort());
	}

	@Test
	public void testBundle2TCP() throws Exception {
		testBundle2(NetworkProtocol.TCP, findAvailablePort());
	}

	private void testBundleReceiving(NetworkProtocol protocol, int receiverPort)
			throws Exception {
		setUp(0, 0, receiverPort, receiverPort, null, protocol);

		OSCBundle bundle = new OSCBundle();
		bundle.addPacket(new OSCMessage("/bundle/receiving"));
		SimpleOSCMessageListener listener = new SimpleOSCMessageListener();
		receiver.getDispatcher().addListener(new OSCPatternAddressMessageSelector("/bundle/receiving"),
				listener);
		receiver.startListening();
		sender.send(bundle);

		try {
			assertMessageReceived(listener, WAIT_FOR_RECEIVE_MS);
		} finally {
			receiver.stopListening();
		}

		if (!listener.getReceivedEvent().getTime().equals(bundle.getTimestamp())) {
			Assert.fail("Message should have timestamp " + bundle.getTimestamp()
					+ " but has " + listener.getReceivedEvent().getTime());
		}
	}

	@Test
	public void testBundleReceivingUDP() throws Exception {
		testBundleReceiving(NetworkProtocol.UDP, OSCPort.defaultSCOSCPort());
	}

	@Test
	public void testBundleReceivingTCP() throws Exception {
		testBundleReceiving(NetworkProtocol.TCP, findAvailablePort());
	}

	private void testLowLevelBundleReceiving(
			NetworkProtocol protocol, int receiverPort)
			throws Exception {
		setUp(
				0,
				0,
				receiverPort,
				receiverPort,
				new SimpleOSCPacketListener(),
				protocol
		);

		SimpleOSCPacketListener simpleListener = (SimpleOSCPacketListener) listener;

		receiver.startListening();

		OSCBundle bundle = new OSCBundle();
		bundle.addPacket(new OSCMessage("/low-level/bundle/receiving"));
		sender.send(bundle);

		try {
			assertPacketReceived(simpleListener, WAIT_FOR_RECEIVE_MS);
		} finally {
			receiver.stopListening();
		}

		OSCBundle packet = (OSCBundle) simpleListener.getReceivedPacket();
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
		testLowLevelBundleReceiving(
				NetworkProtocol.UDP, OSCPort.defaultSCOSCPort()
		);
	}

	@Test
	public void testLowLevelBundleReceivingTCP() throws Exception {
		testLowLevelBundleReceiving(NetworkProtocol.TCP, findAvailablePort());
	}

	private void testLowLevelMessageReceiving(
			NetworkProtocol protocol, int receiverPort)
			throws Exception {
		setUp(
				0,
				0,
				receiverPort,
				receiverPort,
				new SimpleOSCPacketListener(),
				protocol
		);

		SimpleOSCPacketListener simpleListener = (SimpleOSCPacketListener) listener;

		receiver.startListening();

		String expectedAddress = "/low-level/message/receiving";

		OSCMessage message = new OSCMessage(expectedAddress);
		sender.send(message);

		try {
			assertPacketReceived(simpleListener, WAIT_FOR_RECEIVE_MS);
		} finally {
			receiver.stopListening();
		}

		OSCMessage packet = (OSCMessage) simpleListener.getReceivedPacket();
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
		testLowLevelMessageReceiving(
				NetworkProtocol.UDP, OSCPort.defaultSCOSCPort()
		);
	}

	@Test
	public void testLowLevelMessageReceivingTCP() throws Exception {
		testLowLevelMessageReceiving(NetworkProtocol.TCP, findAvailablePort());
	}

	private void testLowLevelRemovingPacketListener(
			NetworkProtocol protocol, int receiverPort)
			throws Exception {
		setUp(0, 0, receiverPort, receiverPort, null, protocol);

		SimpleOSCPacketListener listener = new SimpleOSCPacketListener();
		receiver.addPacketListener(listener);
		receiver.removePacketListener(listener);
		receiver.startListening();

		OSCMessage message = new OSCMessage("/low-level/removing-packet-listener");
		sender.send(message);

		try {
			assertPacketNotReceived(
					listener,
					WAIT_FOR_RECEIVE_MS,
					"Packet was received, despite removePacketListener having been called."
			);
		} finally {
			receiver.stopListening();
		}
	}

	@Test
	public void testLowLevelRemovingPacketListenerUDP() throws Exception {
		testLowLevelRemovingPacketListener(
				NetworkProtocol.UDP, OSCPort.defaultSCOSCPort()
		);
	}

	@Test
	public void testLowLevelRemovingPacketListenerTCP() throws Exception {
		testLowLevelRemovingPacketListener(
				NetworkProtocol.TCP, findAvailablePort()
		);
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

		try {
			assertMessageReceived(listener, WAIT_FOR_RECEIVE_MS);
		} finally {
			receiver.stopListening();
		}

		List<Object> sentArgs = message.getArguments();
		List<Object> receivedArgs = listener.getReceivedEvent()
				.getMessage()
				.getArguments();

		if (sentArgs.size() != receivedArgs.size()) {
			Assert.fail("Message received #arguments differs from #arguments sent");
		}

		for (int i = 0; i < sentArgs.size(); i++) {
			Object sentArg = sentArgs.get(i);
			Object receivedArg = receivedArgs.get(i);
			if (!sentArg.equals(receivedArg)) {
				Assert.fail(
						String.format(
								"Message received argument #%d ('%s') " +
										"differs from the one sent ('%s')",
								i + 1,
								receivedArg,
								sentArg
						)
				);
			}
		}
	}

	@Test
	public void testReceivingUDP1500() throws Exception {
		setUp(OSCPort.defaultSCOSCPort());

		// Create a list of arguments of size 1500 bytes,
		// so the resulting UDP packet size is sure to be bigger then the default maximum,
		// which is 1500 bytes (including headers).
		testReceivingBySize(1500);
	}

	// OSCSerializer throws OSCSerializeException,
	// caused by java.nio.BufferOverflowException
	@Test(expected = OSCSerializeException.class)
	public void testReceivingUDP66K() throws Exception {
		setUp(OSCPort.defaultSCOSCPort());

		// Create a list of arguments of size 66000 bytes,
		// so the resulting UDP packet size is sure to be bigger then the theoretical maximum,
		// which is 65k bytes (including headers).
		testReceivingBySize(66000);
	}

	@Test
	public void testReceivingTCP1500() throws Exception {
		int receiverPort = findAvailablePort();
		setUp(0, 0, receiverPort, receiverPort, null, NetworkProtocol.TCP);
		testReceivingBySize(1500);
	}

	@Test
	public void testReceivingTCP100K() throws Exception {
		int receiverPort = findAvailablePort();
		setUp(0, 0, receiverPort, receiverPort, null, NetworkProtocol.TCP);
		testReceivingBySize(100000);
	}

	@Test
	public void testReceivingTCP1M() throws Exception {
		int receiverPort = findAvailablePort();
		setUp(0, 0, receiverPort, receiverPort, null, NetworkProtocol.TCP);
		testReceivingBySize(1000000);
	}

	// OSCSerializer throws OSCSerializeException,
	// caused by java.nio.BufferOverflowException
	@Test(expected = OSCSerializeException.class)
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

		try {
			if (shouldReceive) {
				assertMessageReceived(listener, WAIT_FOR_RECEIVE_MS);
			} else {
				assertMessageNotReceived(
						listener,
						WAIT_FOR_RECEIVE_MS,
						"Message was received while it should not have!"
				);
			}
		} finally {
			receiver.stopListening();
			// receiver.disconnect();
		}
	}

	@Test
	public void testBundleReceivingConnectedOut() throws Exception {
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
				NetworkProtocol.UDP
		);

		sender.connect();
		testBundleReceiving(true);
	}

	@Test
	public void testBundleReceivingConnectedOutDifferentSender()
			throws Exception {
		setUp(
				// sender.local != receiver.remote
				OSCPort.defaultSCOSCPort(),
				OSCPort.defaultSCOSCPort() + 1,
				// sender.remote == receiver.local
				OSCPort.defaultSCOSCPort() + 2,
				OSCPort.defaultSCOSCPort() + 2,
				null,
				NetworkProtocol.UDP
		);

		sender.connect();
		testBundleReceiving(true);
	}

	@Test
	public void testBundleReceivingConnectedOutDifferentReceiver()
			throws Exception {
		setUp(
				// sender.local == receiver.remote
				OSCPort.defaultSCOSCPort(),
				OSCPort.defaultSCOSCPort(),
				// sender.remote != receiver.local
				OSCPort.defaultSCOSCPort() + 1,
				OSCPort.defaultSCOSCPort() + 2,
				null,
				NetworkProtocol.UDP
		);

		sender.connect();
		testBundleReceiving(false);
	}

	@Test
	public void testBundleReceivingConnectedIn() throws Exception {
		setUp(
				// sender.local == receiver.remote
				OSCPort.defaultSCOSCPort(),
				OSCPort.defaultSCOSCPort(),
				// sender.remote == receiver.local
				OSCPort.defaultSCOSCPort() + 1,
				OSCPort.defaultSCOSCPort() + 1,
				null,
				NetworkProtocol.UDP
		);

		receiver.connect();
		testBundleReceiving(true);
	}

	@Test
	public void testBundleReceivingConnectedInDifferentSender() throws Exception {
		setUp(
				// sender.local != receiver.remote
				OSCPort.defaultSCOSCPort(),
				OSCPort.defaultSCOSCPort() + 1,
				// sender.remote == receiver.local
				OSCPort.defaultSCOSCPort() + 2,
				OSCPort.defaultSCOSCPort() + 2,
				null,
				NetworkProtocol.UDP
		);

		receiver.connect();
		testBundleReceiving(false);
	}

	@Test
	public void testBundleReceivingConnectedInDifferentReceiver()
			throws Exception {
		setUp(
				// sender.local == receiver.remote
				OSCPort.defaultSCOSCPort(),
				OSCPort.defaultSCOSCPort(),
				// sender.remote != receiver.local
				OSCPort.defaultSCOSCPort() + 1,
				OSCPort.defaultSCOSCPort() + 2,
				null,
				NetworkProtocol.UDP
		);

		receiver.connect();
		testBundleReceiving(false);
	}

	@Test
	public void testBundleReceivingConnectedBoth() throws Exception {
		setUp(
				// sender.local == receiver.remote
				OSCPort.defaultSCOSCPort(),
				OSCPort.defaultSCOSCPort(),
				// sender.remote == receiver.local
				OSCPort.defaultSCOSCPort() + 1,
				OSCPort.defaultSCOSCPort() + 1,
				null,
				NetworkProtocol.UDP
		);

		receiver.connect();
		sender.connect();
		testBundleReceiving(true);
	}

	/**
	 * Checks if buffers are correctly reset after receiving a message.
	 *
	 * @throws Exception if anything goes wrong
	 */
	private void testReceivingLongAfterShort(
			NetworkProtocol protocol, int receiverPort)
			throws Exception {
		setUp(0, 0, receiverPort, receiverPort, null, protocol);

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

		try {
			assertMessageReceived(listener, WAIT_FOR_RECEIVE_MS);
		} finally {
			receiver.stopListening();
		}
	}

	@Test
	public void testReceivingLongAfterShortUDP() throws Exception {
		testReceivingLongAfterShort(
				NetworkProtocol.UDP, OSCPort.defaultSCOSCPort()
		);
	}

	@Test
	public void testReceivingLongAfterShortTCP() throws Exception {
		testReceivingLongAfterShort(NetworkProtocol.TCP, findAvailablePort());
	}

	/**
	 * Checks if simultaneous use of packet- and message-listeners works.
	 *
	 * @throws Exception if anything goes wrong
	 */
	private void testReceivingMessageAndPacketListeners(
			NetworkProtocol protocol, int receiverPort)
			throws Exception {
		setUp(0, 0, receiverPort, receiverPort, null, protocol);

		final OSCMessage msg = new OSCMessage("/msg/short");
		final SimpleOSCPacketListener pkgListener = new SimpleOSCPacketListener();
		final SimpleOSCMessageListener msgListener = new SimpleOSCMessageListener();
		receiver.getDispatcher().addListener(new OSCPatternAddressMessageSelector(
						"/msg/short"),
				msgListener);
		receiver.addPacketListener(pkgListener);
		receiver.startListening();
		sender.send(msg);

		try {
			assertPacketReceived(pkgListener, WAIT_FOR_RECEIVE_MS);
			assertMessageReceived(msgListener, WAIT_FOR_RECEIVE_MS);
		} finally {
			receiver.stopListening();
		}
	}

	@Test
	public void testReceivingMessageAndPacketListenersUDP() throws Exception {
		testReceivingMessageAndPacketListeners(
				NetworkProtocol.UDP, OSCPort.defaultSCOSCPort()
		);
	}

	@Test
	public void testReceivingMessageAndPacketListenersTCP() throws Exception {
		testReceivingMessageAndPacketListeners(
				NetworkProtocol.TCP, findAvailablePort()
		);
	}

	private void testStopListeningAfterReceivingBadAddress(
			NetworkProtocol protocol, int receiverPort)
			throws Exception {
		setUp(0, 0, receiverPort, receiverPort, null, protocol);

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

		try {
			assertMessageNotReceived(
					listener,
					WAIT_FOR_RECEIVE_MS,
					"Message was received, while it should not have been"
			);
		} finally {
			receiver.stopListening();
		}
	}

	@Test
	public void testStopListeningAfterReceivingBadAddressUDP() throws Exception {
		testStopListeningAfterReceivingBadAddress(
				NetworkProtocol.UDP, OSCPort.defaultSCOSCPort()
		);
	}

	@Test
	public void testStopListeningAfterReceivingBadAddressTCP() throws Exception {
		testStopListeningAfterReceivingBadAddress(
				NetworkProtocol.TCP, findAvailablePort()
		);
	}

	private void testListeningAfterBadAddress(
			NetworkProtocol protocol, int receiverPort)
			throws Exception {
		setUp(0, 0, receiverPort, receiverPort, null, protocol);

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

		try {
			assertMessageReceived(listener, WAIT_FOR_RECEIVE_MS);
		} finally {
			receiver.stopListening();
		}
	}

	@Test
	public void testListeningAfterBadAddressUDP() throws Exception {
		testListeningAfterBadAddress(
				NetworkProtocol.UDP, OSCPort.defaultSCOSCPort()
		);
	}

	@Test
	public void testListeningAfterBadAddressTCP() throws Exception {
		testListeningAfterBadAddress(NetworkProtocol.TCP, findAvailablePort());
	}
}
