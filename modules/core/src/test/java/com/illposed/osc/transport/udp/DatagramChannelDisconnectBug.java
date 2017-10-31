/*
 * Public Domain
 * Author: Robin Vobruba <hoijui.quaero@gmail.com
 */

package com.illposed.osc.transport.udp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import org.junit.Test;

public class DatagramChannelDisconnectBug {

	@Test
	public void blockingOnDisconnect() throws Exception {

		final int portSender = 6666;
		final int portReceiver = 7777;

		final SocketAddress senderSocket = new InetSocketAddress(InetAddress.getLocalHost(),
				portSender);
		final SocketAddress receiverSocket = new InetSocketAddress(InetAddress.getLocalHost(),
				portReceiver);


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
			receiverChannel.connect(senderSocket); // NOTE try commenting out this line

			tryReceiving(receiverChannel);
		} finally {
			System.err.println("receiverChannel connected (before): "
					+ receiverChannel.isConnected());
			if (receiverChannel.isConnected()) // NOTE try commenting out this line
			{
				System.err.println("receiverChannel.disconnect() ...");
				receiverChannel.disconnect();
				System.err.println("receiverChannel disconnected.");
			}
			System.err.println("receiverChannel connected (after): "
					+ receiverChannel.isConnected());
			senderChannel.disconnect();

			receiverChannel.close();
			senderChannel.close();
		}
	}

	private void tryReceiving(final DatagramChannel receiver) throws Exception {

		receiver.configureBlocking(true);
		final Thread readingThread = new Thread() {
			@Override
			public void run() {
				try {
					final ByteBuffer targetBuf = ByteBuffer.allocate(100);
					receiver.receive(targetBuf); // NOTE try with a breakpoint on this line
				} catch (final IOException ex) {
					throw new RuntimeException(ex);
				}
			}
		};
		readingThread.start();

		Thread.sleep(1000);
		throw new IOException();
	}
}
