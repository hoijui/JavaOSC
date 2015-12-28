/*
 * Copyright (C) 2004-2014, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.transport.udp;

import com.illposed.osc.OSCPacket;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.OSCSerializer;
import com.illposed.osc.OSCSerializerFactory;
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
 * (loosely based on {com.illposed.osc.OSCPortTest#testMessageWithArgs()})
 * <blockquote><pre>{@code
 * OSCPortOut sender = new OSCPortOut();
 * List<Object> args = new ArrayList<Object>(2);
 * args.add(3);
 * args.add("hello");
 * OSCMessage msg = new OSCMessage("/sayhello", args);
 * try {
 * 	sender.send(msg);
 * } catch (Exception ex) {
 * 	System.err.println("Couldn't send");
 * }
 * }</pre></blockquote>
 */
public class OSCPortOut extends OSCPort {

	private final ByteBuffer outputBuffer;
	private final OSCSerializer converter;

	/**
	 * Creates an OSC-Port that sends to {@code remote} from the specified local socket,
	 * using a serializer created from the given factory for converting the packets.
	 * @param serializerFactory used to create a single serializer that is used to convert
	 *   all packets to be sent from this port from Java to their OSC byte array representation
	 * @param remote where we will send the OSC byte array data to
	 * @param local the local address we use to connect to the remote
	 * @throws IOException if we fail to bind a channel to the local address
	 */
	public OSCPortOut(
			final OSCSerializerFactory serializerFactory,
			final SocketAddress remote,
			final SocketAddress local)
			throws IOException
	{
		super(local, remote);

		this.outputBuffer = ByteBuffer.allocate(OSCPortIn.BUFFER_SIZE);
		this.converter = serializerFactory.create(outputBuffer);
	}

	public OSCPortOut(
			final OSCSerializerFactory serializerFactory,
			final SocketAddress remote)
			throws IOException
	{
		this(serializerFactory, remote, new InetSocketAddress(0));
	}

	public OSCPortOut(final SocketAddress remote) throws IOException {
		this(OSCSerializerFactory.createDefaultFactory(), remote);
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
	 * Converts an OSC packet (message or bundle) to its OSC byte array representation.
	 * @param packet the bundle or message to be converted
	 * @return the OSC byte array representation of {@code packet}
	 * @throws IOException if we run out of memory for the conversion buffer
	 * @throws OSCSerializeException if the packet fails to serialize
	 */
	public ByteBuffer convert(final OSCPacket packet) throws IOException, OSCSerializeException {

		outputBuffer.rewind();
		converter.write(packet);
		outputBuffer.flip();
		final ByteBuffer oscPacket = outputBuffer;

		return oscPacket;
	}

	/**
	 * Sends an OSC packet (message or bundle) to the remote address.
	 * This assumes that the given data was previously already converted using {@link #convert}.
	 * @param oscPacket the bundle or message to sent, as OSC byte array
	 * @throws IOException if a socket I/O error occurs
	 */
	public void send(final ByteBuffer oscPacket) throws IOException {

		final DatagramChannel channel = getChannel();
		if (channel.isConnected()) {
			channel.write(oscPacket);
		} else {
			channel.send(oscPacket, getRemoteAddress());
		}
	}

	/**
	 * Converts and sends an OSC packet (message or bundle) to the remote address.
	 * @param packet the bundle or message to be converted and sent
	 * @throws IOException if we run out of memory while converting,
	 *   or a socket I/O error occurs while sending
	 * @throws OSCSerializeException if the packet fails to serialize
	 */
	public void send(final OSCPacket packet) throws IOException, OSCSerializeException {

		final ByteBuffer oscPacket = convert(packet);
		send(oscPacket);
	}

	@Override
	public String toString() {

		final StringBuilder rep = new StringBuilder(64);

		rep
				.append('[')
				.append(getClass().getSimpleName())
				.append(": sending to \"")
				.append(getRemoteAddress().toString())
				.append("\"]");

		return rep.toString();
	}
}
