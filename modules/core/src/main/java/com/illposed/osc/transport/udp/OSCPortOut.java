/*
 * Copyright (C) 2004-2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.illposed.osc.transport.udp;

import com.illposed.osc.OSCPacket;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.OSCSerializerAndParserBuilder;
import com.illposed.osc.transport.channel.OSCDatagramChannel;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Sends OSC packets to a specific UDP/IP address and port.
 *
 * To send an OSC message, call {@link #send(OSCPacket)}.
 *
 * An example:<br>
 * <blockquote><pre>{@code
 * // sends from "localhost"
 * OSCPortOut sender = new OSCPortOut();
 * List<Object> args = new ArrayList<Object>(2);
 * args.add(3);
 * args.add("hello");
 * OSCMessage msg = new OSCMessage("/sayHello", args);
 * try {
 * 	sender.send(msg);
 * } catch (Exception ex) {
 * 	System.err.println("Couldn't send");
 * }
 * }</pre></blockquote>
 */
public class OSCPortOut extends OSCPort {

	private final ByteBuffer outputBuffer;
	private final OSCSerializerAndParserBuilder serializerBuilder;

	/**
	 * Creates an OSC-Port that sends to {@code remote} from the specified local socket,
	 * using an {@link com.illposed.osc.OSCSerializer}
	 * created from the given factory for converting the packets.
	 * @param serializerBuilder used to create a single
	 *   {@link com.illposed.osc.OSCSerializer} that is used to convert
	 *   all packets to be sent from this port,
	 *   from Java objects to their OSC byte array representations
	 * @param remote where we will send the OSC byte array data to
	 * @param local the local address we use to connect to the remote
	 * @throws IOException if we fail to bind a channel to the local address
	 */
	public OSCPortOut(
			final OSCSerializerAndParserBuilder serializerBuilder,
			final SocketAddress remote,
			final SocketAddress local)
			throws IOException
	{
		super(local, remote);

		this.outputBuffer = ByteBuffer.allocate(OSCPortIn.BUFFER_SIZE);
		this.serializerBuilder = serializerBuilder;
	}

	public OSCPortOut(
			final OSCSerializerAndParserBuilder serializerFactory,
			final SocketAddress remote)
			throws IOException
	{
		this(serializerFactory, remote, new InetSocketAddress(
				OSCPort.generateWildcard(remote), 0));
	}

	public OSCPortOut(final SocketAddress remote) throws IOException {
		this(new OSCSerializerAndParserBuilder(), remote);
	}

	/**
	 * Creates an OSC-Port that sends to {@code remote}:{@code port}.
	 * @param remote the address to send to
	 * @param port the port number to send to
	 * @throws IOException if we fail to bind a channel to the local address
	 */
	public OSCPortOut(final InetAddress remote, final int port) throws IOException {
		this(new InetSocketAddress(remote, port));
	}

	/**
	 * Creates an OSC-Port that sends to {@code remote}:{@link #DEFAULT_SC_OSC_PORT}.
	 * @param remote the address to send to
	 * @throws IOException if we fail to bind a channel to the local address
	 */
	public OSCPortOut(final InetAddress remote) throws IOException {
		this(remote, DEFAULT_SC_OSC_PORT);
	}

	/**
	 * Creates an OSC-Port that sends to "localhost":{@link #DEFAULT_SC_OSC_PORT}.
	 * @throws IOException if we fail to bind a channel to the local address,
	 *   or if the local host name could not be resolved into an address
	 */
	public OSCPortOut() throws IOException {
		this(new InetSocketAddress(InetAddress.getLocalHost(), DEFAULT_SC_OSC_PORT));
	}

	/**
	 * Converts and sends an OSC packet (message or bundle) to the remote address.
	 * @param packet the bundle or message to be converted and sent
	 * @throws IOException if we run out of memory while converting,
	 *   or a socket I/O error occurs while sending
	 * @throws OSCSerializeException if the packet fails to serialize
	 */
	public void send(final OSCPacket packet) throws IOException, OSCSerializeException {

		final DatagramChannel channel = getChannel();
		final OSCDatagramChannel oscChannel = new OSCDatagramChannel(channel, serializerBuilder);
		oscChannel.send(outputBuffer, packet, getRemoteAddress());
	}

	@Override
	public String toString() {

		return '[' + getClass().getSimpleName() + ": sending to \"" + getRemoteAddress().toString() + "\"]";
	}
}
