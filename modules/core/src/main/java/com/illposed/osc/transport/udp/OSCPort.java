/*
 * Copyright (C) 2003-2014, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.transport.udp;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;

/**
 * An abstract superclass.
 * To send OSC messages, use {@link OSCPortOut}.
 * To listen for OSC messages, use {@link OSCPortIn}.
 */
public class OSCPort {

	private final SocketAddress local;
	private final SocketAddress remote;
	private final DatagramChannel channel;

	public static final int DEFAULT_SC_OSC_PORT = 57110;
	public static final int DEFAULT_SC_LANG_OSC_PORT = 57120;

	protected OSCPort(final SocketAddress local, final SocketAddress remote) throws IOException {

		this.local = local;
		this.remote = remote;
		this.channel = DatagramChannel.open();

		this.channel.socket().bind(local);
	}

	/**
	 * The port that the SuperCollider <b>synth</b> engine
	 * usually listens to.
	 * @return default SuperCollider <b>synth</b> UDP port
	 * @see #DEFAULT_SC_OSC_PORT
	 */
	public static int defaultSCOSCPort() {
		return DEFAULT_SC_OSC_PORT;
	}

	/**
	 * The port that the SuperCollider <b>language</b> engine
	 * usually listens to.
	 * @return default SuperCollider <b>language</b> UDP port
	 * @see #DEFAULT_SC_LANG_OSC_PORT
	 */
	public static int defaultSCLangOSCPort() {
		return DEFAULT_SC_LANG_OSC_PORT;
	}

	/**
	 * Returns the channel associated with this port.
	 * @return this ports channel
	 */
	protected DatagramChannel getChannel() {
		return channel;
	}

	public void connect() throws IOException {

		if (getRemoteAddress() == null) {
			throw new IllegalStateException(
					"Can not connect a socket without a remote address specified");
		}
		getChannel().connect(getRemoteAddress());
	}

	public void disconnect() throws IOException {
		getChannel().disconnect();
	}

	public boolean isConnected() {
		return getChannel().isConnected();
	}

	public SocketAddress getLocalAddress() {
		return local;
	}

	public SocketAddress getRemoteAddress() {
		return remote;
	}

	/**
	 * Close the socket and free-up resources.
	 * It is recommended that clients call this when they are done with the
	 * port.
	 * @throws IOException If an I/O error occurs on the channel
	 */
	public void close() throws IOException {
		channel.close();
	}
}
