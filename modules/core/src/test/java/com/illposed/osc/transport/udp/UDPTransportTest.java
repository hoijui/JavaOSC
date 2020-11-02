/*
 * Copyright (C) 2020, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.illposed.osc.transport.udp;

import com.illposed.osc.transport.OSCPort;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Random;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UDPTransportTest {

	private static final long WAIT_FOR_SOCKET_CLOSE = 30;

	private final Logger log = LoggerFactory.getLogger(UDPTransportTest.class);

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

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
		final int portSender = 6666;
		final int portReceiver = 7777;

		final SocketAddress senderSocket = new InetSocketAddress(InetAddress.getLocalHost(), portSender);
		final SocketAddress receiverSocket = new InetSocketAddress(InetAddress.getLocalHost(), portReceiver);


		DatagramChannel senderChannel = null;
		DatagramChannel receiverChannel = null;
		try {
			senderChannel = DatagramChannel.open();
			senderChannel.socket().bind(senderSocket);
			senderChannel.socket().setReuseAddress(true);
			senderChannel.socket().setSendBufferSize(UDPTransport.BUFFER_SIZE);

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
					log.error("Failed to close test OSC in channel", ex);
				}
			}
			if (senderChannel != null) {
				try {
					senderChannel.close();
				} catch (final IOException ex) {
					log.error("Failed to close test OSC out channel", ex);
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
		while ((total < dataSize) && (((count = receiver.read(targetBuf))) != -1)) {
			total = total + count;
			// 3s timeout to avoid dead loop
			if ((System.currentTimeMillis() - beginTime) > 3000) {
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
		if (bindAddress.getAddress() instanceof Inet4Address) {
			channel = DatagramChannel.open(StandardProtocolFamily.INET);
		} else if (bindAddress.getAddress() instanceof Inet6Address) {
			channel = DatagramChannel.open(StandardProtocolFamily.INET6);
		} else {
			throw new IllegalArgumentException(
					"Unknown address type: "
					+ bindAddress.getAddress().getClass().getCanonicalName());
		}
		channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		channel.socket().bind(bindAddress);

		Assert.assertEquals(bindAddress, channel.getLocalAddress());
	}
}
