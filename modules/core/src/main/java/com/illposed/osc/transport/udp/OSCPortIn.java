/*
 * Copyright (C) 2004-2014, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.transport.udp;

import com.illposed.osc.OSCPacket;
import com.illposed.osc.OSCPacketDispatcher;
import com.illposed.osc.OSCParseException;
import com.illposed.osc.OSCParser;
import com.illposed.osc.OSCParserFactory;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

/**
 * Listens for OSC packets on a UDP/IP port.
 *
 * An example:<br>
 * (loosely based on {com.illposed.osc.OSCPortTest#testReceiving()})
 * <blockquote><pre>{@code
 * OSCPortIn receiver = new OSCPortIn(OSCPort.DEFAULT_SC_OSC_PORT);
 * OSCMessageListener listener = new OSCMessageListener() {
 *   public void acceptMessage(OSCTimeStamp time, OSCMessage message) {
 *     System.out.println("Message received!");
 *   }
 * };
 * MessageSelector selector = new OSCPatternAddressMessageSelector(
 *     "/message/receiving");
 * receiver.getDispatcher().addListener(selector, listener);
 * receiver.startListening();
 * }</pre></blockquote>
 *
 * Then, using a program such as SuperCollider or sendOSC, send a message
 * to this computer, port {@link #DEFAULT_SC_OSC_PORT},
 * with the address "/message/receiving".
 */
public class OSCPortIn extends OSCPort implements Runnable {

	/**
	 * Buffers were 1500 bytes in size, but were increased to 1536, as this is a common MTU,
	 * and then increased to 65507, as this is the maximum incoming datagram data size.
	 */
	static final int BUFFER_SIZE = 65507;

	/** state for listening */
	private boolean listening;
	private final OSCParser converter;
	private final OSCPacketDispatcher dispatcher;

	/**
	 * Create an OSCPort that listens using a specified socket,
	 * using a parser for the specified factory.
	 * @param parserFactory to create the internal parser from
	 * @param socket DatagramSocket to listen on.
	 */
	public OSCPortIn(final OSCParserFactory parserFactory, final DatagramSocket socket) {
		super(socket, socket.getLocalPort());

		this.converter = parserFactory.create();
		this.dispatcher = new OSCPacketDispatcher();
		// NOTE We do this, even though it is against the OSC (1.0) specification,
		//   because this is how it worked in this library until Feb. 2015.,
		//   and thus users of this library expect this behavour by default.
		this.dispatcher.setAlwaysDispatchingImmediatly(true);
	}

	/**
	 * Create an OSCPort that listens using a specified socket.
	 * @param socket DatagramSocket to listen on.
	 */
	public OSCPortIn(final DatagramSocket socket) {
		this(OSCParserFactory.createDefaultFactory(), socket);
	}

	/**
	 * Create an OSCPort that listens on the specified port.
	 * Strings will be decoded using the systems default character set.
	 * @param port UDP port to listen on.
	 * @throws SocketException if the port number is invalid,
	 *   or there is already a socket listening on it
	 */
	public OSCPortIn(final int port) throws SocketException {
		this(new DatagramSocket(port));
	}

	/**
	 * Run the loop that listens for OSC on a socket until
	 * {@link #isListening()} becomes false.
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		final byte[] buffer = new byte[BUFFER_SIZE];
		final DatagramPacket packet = new DatagramPacket(buffer, BUFFER_SIZE);
		final DatagramSocket socket = getSocket();
		while (listening) {
			try {
				try {
					socket.receive(packet);
				} catch (final SocketException ex) {
					if (listening) {
						throw ex;
					} else {
						// if we closed the socket while receiving data,
						// the exception is expected/normal, so we hide it
						continue;
					}
				}
				final ByteBuffer packetBytes
						= ByteBuffer.wrap(buffer, 0, packet.getLength()).asReadOnlyBuffer();
				final OSCPacket oscPacket = converter.convert(packetBytes);
				dispatcher.dispatchPacket(oscPacket);
			} catch (final IOException ex) {
				stopListening(ex);
			} catch (final OSCParseException ex) {
				stopListening(ex);
			}
		}
	}

	private void stopListening(final Exception exception) {

		System.err.println("Error while listening on " + toString() + ": "
				+ exception.getMessage());
		stopListening();
	}

	/**
	 * Start listening for incoming OSCPackets
	 */
	public void startListening() {

		if (!isListening()) { // NOTE This is not thread-save
			listening = true;
			final Thread listeningThread = new Thread(this);
			// The JVM exits when the only threads running are all daemon threads.
			listeningThread.setDaemon(true);
			listeningThread.start();
		}
	}

	/**
	 * Stop listening for incoming OSCPackets
	 */
	public void stopListening() {

		listening = false;
		getSocket().close();
	}

	/**
	 * Am I listening for packets?
	 * @return true if this port is in listening mode
	 */
	public boolean isListening() {
		return listening;
	}

	@Override
	public String toString() {

		final StringBuilder rep = new StringBuilder(32);

		rep
				.append('[')
				.append(getClass().getSimpleName())
				.append(": ");
		if (isListening()) {
			rep
					.append("listening on \"")
					.append(getSocket().getLocalAddress().getHostName())
					.append(':')
					.append(getPort())
					.append('\"');
		} else {
			rep.append("stopped");
		}
		rep.append(']');

		return rep.toString();
	}

	public OSCPacketDispatcher getDispatcher() {
		return dispatcher;
	}
}
