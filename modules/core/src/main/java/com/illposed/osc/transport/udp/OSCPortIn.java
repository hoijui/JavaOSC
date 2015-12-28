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
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;

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
	 * Create an OSC-Port that listens on the given local socket for packets from {@code remote},
	 * using a parser created with the given factory.
	 * @param parserFactory to create the internal parser from
	 * @param local address to listen on
	 * @param remote address to listen to
	 * @throws IOException if we fail to bind a channel to the local address
	 */
	public OSCPortIn(
			final OSCParserFactory parserFactory,
			final SocketAddress local,
			final SocketAddress remote)
			throws IOException
	{
		super(local, remote);

		this.converter = parserFactory.create();
		this.dispatcher = new OSCPacketDispatcher();
		// NOTE We do this, even though it is against the OSC (1.0) specification,
		//   because this is how it worked in this library until Feb. 2015.,
		//   and thus users of this library expect this behavour by default.
		this.dispatcher.setAlwaysDispatchingImmediatly(true);
	}

	public OSCPortIn(final OSCParserFactory parserFactory, final SocketAddress local)
			throws IOException
	{
		this(parserFactory, local, new InetSocketAddress(0));
	}

	/**
	 * Creates an OSC-Port that listens on the given local socket.
	 * @param local address to listen on
	 * @throws IOException if we fail to bind a channel to the local address
	 */
	public OSCPortIn(final SocketAddress local) throws IOException {
		this(OSCParserFactory.createDefaultFactory(), local);
	}

	public OSCPortIn(final OSCParserFactory parserFactory, final int port) throws IOException {
		this(parserFactory, new InetSocketAddress(port));
	}

	/**
	 * Creates an OSC-Port that listens on the specified local port.
	 * @param port port number to listen on
	 * @throws IOException if we fail to bind a channel to the local address
	 */
	public OSCPortIn(final int port) throws IOException {
		this(new InetSocketAddress(port));
	}

	/**
	 * Creates an OSC-Port that listens on local port {@link #DEFAULT_SC_OSC_PORT}..
	 * @throws IOException if we fail to bind a channel to the local address
	 */
	public OSCPortIn() throws IOException {
		this(defaultSCOSCPort());
	}

	/**
	 * Run the loop that listens for OSC on a socket until
	 * {@link #isListening()} becomes false.
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
		final DatagramChannel channel = getChannel();
		while (listening) {
			try {
				buffer.clear();
				try {
					if (channel.isConnected()) {
						/*final int readBytes = */channel.read(buffer);
					} else {
						channel.receive(buffer);
					}
//					final int readBytes = buffer.position();
				} catch (final ClosedChannelException ex) {
					if (listening) {
						throw ex;
					} else {
						// if we closed the channel while receiving data,
						// the exception is expected/normal, so we hide it
						continue;
					}
				}
				buffer.flip();
				if (buffer.limit() == 0) {
					if (isListening()) {
						throw new OSCParseException("Received a packet without any data");
					} else {
						// normal exit: we just get a no-data package becasue we stopped listening
						break;
					}
				} else {
					// convert from OSC byte array -> Java object
					// FIXME BAAAAAAAD!! - the overflow would happen on the receiving end, up there, not down here!
					OSCPacket oscPacket = null;
					do {
						try {
							oscPacket = converter.convert(buffer);
						} catch (final BufferOverflowException ex) {
							buffer = ByteBuffer.allocate(buffer.capacity() + BUFFER_SIZE);
						}
					} while (oscPacket == null);

					// dispatch the Java object
					dispatcher.dispatchPacket(oscPacket);
				}
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
		// NOTE This is not thread-save
		if (getChannel().isBlocking()) {
			try {
				getChannel().close();
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		}
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
					.append(getLocalAddress().toString())
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
