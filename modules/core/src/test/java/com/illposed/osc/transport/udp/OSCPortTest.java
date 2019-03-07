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
import com.illposed.osc.OSCMessageTest;
import com.illposed.osc.OSCPacket;
import com.illposed.osc.OSCPacketListener;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.OSCSerializerFactory;
import com.illposed.osc.SimpleOSCMessageListener;
import com.illposed.osc.SimpleOSCPacketListener;
import com.illposed.osc.argument.OSCTimeTag64;
import com.illposed.osc.messageselector.OSCPatternAddressMessageSelector;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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

	private void reSetUp(
			final int portSenderOut,
			final int portSenderIn,
			final int portReceiverOut,
			final int portReceiverIn,
			final OSCPacketListener packetListener)
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
			.setRemoteSocketAddress(senderInAddress);

		if (packetListener != null)
			builder.setPacketListener(packetListener);

		receiver = builder.build();
		listener = packetListener;

		if (sender != null) {
			sender.close();
		}
		sender = new OSCPortOut(
				OSCSerializerFactory.createDefaultFactory(),
				receiverOutAddress,
				senderOutAddress);
	}

	private void reSetUp(
			final int portSenderOut,
			final int portSenderIn,
			final int portReceiverOut,
			final int portReceiverIn)
			throws Exception
	{
		reSetUp(portSenderOut, portSenderIn, portReceiverOut, portReceiverIn, null);
	}

	private void reSetUp(final int portSender, final int portReceiver) throws Exception {
		reSetUp(portSender, portSender, portReceiver, portReceiver, null);
	}

	private void reSetUp(final int portReceiver) throws Exception {
		reSetUp(0, portReceiver);
	}

	@Before
	public void setUp() throws Exception {
		reSetUp(OSCPort.defaultSCOSCPort());
	}

	private void reSetUpWithSimplePacketListener() throws Exception {
		int sender = 0;
		int receiver = OSCPort.defaultSCOSCPort();
		reSetUp(sender, sender, receiver, receiver, new SimpleOSCPacketListener());
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

		try {
			if (receiver.isConnected()) { // HACK This should not be required, as DatagramChannel#disconnect() is supposed to have no effect if a it is not connected, but in certain tests, removing this if clause makes the disconnect call hang forever; could even be a JVM bug -> we should report that (requires a minimal example first, though)
				receiver.disconnect();
			}
		} catch (final IOException ex) {
			log.error("Failed to disconnect test OSC UDP in port", ex);
		}
		try {
			sender.disconnect();
		} catch (final IOException ex) {
			log.error("Failed to disconnect test OSC UDP out port", ex);
		}

		try {
			receiver.close();
		} catch (final IOException ex) {
			log.error("Failed to close test OSC UDP in port", ex);
		}
		try {
			sender.close();
		} catch (final IOException ex) {
			log.error("Failed to close test OSC UDP out port", ex);
		}

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
	public void readWriteReadData400() throws Exception {
		readWriteReadData(400);
	}

	@Test
	public void readWriteReadData600() throws Exception {
		// common minimal maximum UDP buffer size (MTU) is 5xx Bytes
		readWriteReadData(600);
	}

	@Test
	public void readWriteReadData1400() throws Exception {
		readWriteReadData(1400);
	}

	@Test
	public void readWriteReadData2000() throws Exception {
		// default maximum UDP buffer size (MTU) is ~1500 Bytes
		readWriteReadData(2000);
	}

	@Test
	public void readWriteReadData50000() throws Exception {
		readWriteReadData(50000);
	}

	@Test(expected=IOException.class)
	public void readWriteReadData70000() throws Exception {
		// theoretical maximum UDP buffer size (MTU) is 2^16 - 1 = 65535 Bytes
		readWriteReadData(70000);
	}

	private void readWriteReadData(final int sizeInBytes)
			throws Exception
	{
		tearDown();

		final int portSender = 6666;
		final int portReceiver = 7777;

//		final SocketAddress senderSocket = new InetSocketAddress(0);
		final SocketAddress senderSocket = new InetSocketAddress(InetAddress.getLocalHost(), portSender);
//		final SocketAddress receiverSocket = new InetSocketAddress(0);
		final SocketAddress receiverSocket = new InetSocketAddress(InetAddress.getLocalHost(), portReceiver);


		DatagramChannel senderChannel = null;
		DatagramChannel receiverChannel = null;
		try {
			senderChannel = DatagramChannel.open();
			senderChannel.socket().bind(senderSocket);
			senderChannel.socket().setReuseAddress(true);

			receiverChannel = DatagramChannel.open();
			receiverChannel.socket().bind(receiverSocket);
			receiverChannel.socket().setReuseAddress(true);

			senderChannel.connect(receiverSocket);
			receiverChannel.connect(senderSocket);

			final byte[] sourceArray = new byte[sizeInBytes];
			final byte[] targetArray = new byte[sizeInBytes];

			new Random().nextBytes(sourceArray);

			readWriteReadData(senderChannel, sourceArray, receiverChannel, targetArray, sizeInBytes);
		} finally {
			if (receiverChannel != null) {
				try {
					receiverChannel.close();
				} catch (final IOException ex) {
					log.error("Failed to close test OSC UDP in channel", ex);
				}
			}
			if (senderChannel != null) {
				try {
					senderChannel.close();
				} catch (final IOException ex) {
					log.error("Failed to close test OSC UDP out channel", ex);
				}
			}

			// wait a bit after closing the receiver,
			// because (some) operating systems need some time
			// to actually close the underlying socket
			Thread.sleep(WAIT_FOR_SOCKET_CLOSE);
		}
	}

	private void readWriteReadData(
			final DatagramChannel sender,
			final byte[] sourceArray,
			final DatagramChannel receiver,
			byte[] targetArray,
			final int dataSize)
			throws IOException
	{
		// write
		final ByteBuffer sourceBuf = ByteBuffer.wrap(sourceArray);
		Assert.assertEquals(dataSize, sender.write(sourceBuf));

		// read
		final ByteBuffer targetBuf = ByteBuffer.wrap(targetArray);

		int count;
		int total = 0;
		final long beginTime = System.currentTimeMillis();
		while (total < dataSize && (count = receiver.read(targetBuf)) != -1) {
			total = total + count;
			// 3s timeout to avoid dead loop
			if (System.currentTimeMillis() - beginTime > 3000) {
				break;
			}
		}

		Assert.assertEquals(dataSize, total);
		Assert.assertEquals(targetBuf.position(), total);
		targetBuf.flip();
		targetArray = targetBuf.array();
		for (int i = 0; i < targetArray.length; i++) {
			Assert.assertEquals(sourceArray[i], targetArray[i]);
		}
	}

	@Test
	public void testBindChannel() throws Exception {

		final InetSocketAddress bindAddress = new InetSocketAddress(OSCPort.defaultSCOSCPort());

		final DatagramChannel channel;
		if (Inet4Address.class.isInstance(bindAddress.getAddress())) {
			channel = DatagramChannel.open(StandardProtocolFamily.INET);
		} else if (Inet6Address.class.isInstance(bindAddress.getAddress())) {
			channel = DatagramChannel.open(StandardProtocolFamily.INET6);
		} else {
			throw new IllegalArgumentException(
					"Unknown address type: "
					+ bindAddress.getAddress().getClass().getCanonicalName());
		}
		// NOTE StandardSocketOptions is only available since Java 1.7
		channel.setOption(java.net.StandardSocketOptions.SO_REUSEADDR, true);
		channel.socket().bind(bindAddress);

		// NOTE DatagramChannel#getLocalAddress() is only available since Java 1.7
		Assert.assertEquals(bindAddress, channel.getLocalAddress());
	}

	private void testReceivingLoopback(final InetAddress loopbackAddress) throws Exception {

		final InetSocketAddress loopbackSocket = new InetSocketAddress(loopbackAddress, OSCPort.defaultSCOSCPort());

		// close the underlying sockets
		receiver.close();
		sender.close();

		// make sure the old receiver is gone for good
		Thread.sleep(WAIT_FOR_SOCKET_CLOSE);

		// check if the underlying sockets were closed
		// NOTE We can have many (out-)sockets sending
		//   on the same address and port,
		//   but only one receiving per each such tuple.
		sender = new OSCPortOut(loopbackAddress, OSCPort.defaultSCOSCPort());
		receiver = new OSCPortIn(loopbackSocket);

		testReceiving();
	}

	@Test
	public void testReceivingLoopbackIPv4() throws Exception {

		final InetAddress loopbackAddress = InetAddress.getByName("127.0.0.1");
		testReceivingLoopback(loopbackAddress);
	}

	@Test
	public void testReceivingLoopbackIPv6() throws Exception {

		if (supportsIPv6()) {
			final InetAddress loopbackAddress = InetAddress.getByName("::1");
			testReceivingLoopback(loopbackAddress);
		} else {
			log.warn("Skipping IPv6 test because: No IPv6 support available on this system");
		}
	}

	@Test
	public void testStart() throws Exception {

		OSCMessage mesg = new OSCMessage("/sc/stop");
		sender.send(mesg);
	}

	@Test
	public void testMessageWithArgs() throws Exception {

		List<Object> args = new ArrayList<>(2);
		args.add(3);
		args.add("hello");
		OSCMessage mesg = new OSCMessage("/foo/bar", args);
		sender.send(mesg);
	}

	@Test
	public void testBundle() throws Exception {

		List<Object> args = new ArrayList<>(2);
		args.add(3);
		args.add("hello");
		List<OSCPacket> msgs = new ArrayList<>(1);
		msgs.add(new OSCMessage("/foo/bar", args));
		OSCBundle bundle = new OSCBundle(msgs);
		sender.send(bundle);
	}

	@Test
	public void testBundle2() throws Exception {

		final List<Object> arguments = new ArrayList<>(2);
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
		if (!listener.getReceivedEvent().getTime().equals(bundle.getTimestamp())) {
			Assert.fail("Message should have timestamp " + bundle.getTimestamp()
					+ " but has " + listener.getReceivedEvent().getTime());
		}
	}

	@Test
	public void testLowLevelBundleReceiving() throws Exception {
		reSetUpWithSimplePacketListener();
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
		OSCTimeTag64 timetag = packet.getTimestamp();

		if (!timetag.equals(bundle.getTimestamp())) {
			Assert.fail(
				"Message should have timestamp " +
				bundle.getTimestamp() +
				" but has " +
				timetag
			);
		}
	}

	@Test
	public void testLowLevelMessageReceiving() throws Exception {
		reSetUpWithSimplePacketListener();
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
	public void testLowLevelRemovingPacketListener() throws Exception {
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
					+ "'");
		}
	}

	@Test
	public void testReceivingBig() throws Exception {

		// Create a list of arguments of size 1500 bytes,
		// so the resulting UDP packet size is sure to be bigger then the default maximum,
		// which is 1500 bytes (including headers).
		testReceivingBySize(1500);
	}

	@Test(expected=OSCSerializeException.class)
	public void testReceivingHuge() throws Exception {

		// Create a list of arguments of size 66000 bytes,
		// so the resulting UDP packet size is sure to be bigger then the theoretical maximum,
		// which is 65k bytes (including headers).
		testReceivingBySize(66000);
	}

	@Test(expected=OSCSerializeException.class)
	public void testReceivingHugeConnectedOut() throws Exception {

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

	/**
	 * Checks if buffers are correctly reset after receiving a message.
	 * @throws Exception if anything goes wrong
	 */
	@Test
	public void testReceivingLongAfterShort() throws Exception {

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
	public void testStopListeningAfterReceivingBadAddress() throws Exception {

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
	public void testListeningAfterBadAddress() throws Exception {

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
}
