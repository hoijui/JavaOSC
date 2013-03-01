/*
 * Copyright (C) 2003-2006, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

/**
 * OSCPort is an abstract superclass, to send OSC messages,
 * use {@link OSCPortOut}.
 * To listen for OSC messages, use {@link OSCPortIn}.
 *
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class OSCPort {

	private DatagramChannel channel;
//	protected DatagramSocket socket;
	private SocketAddress remote;

	public static final int DEFAULT_SC_OSC_PORT = 57110;
	public static final int DEFAULT_SC_LANG_OSC_PORT = 57120;

	protected OSCPort(SocketAddress local) throws IOException {
		this.channel = DatagramChannel.open();

		this.channel.setOption(java.net.StandardSocketOptions.SO_REUSEADDR, true);
		this.channel.socket().bind(local);
//		this.channel.connect(address);
		//this.socket = this.channel.socket();
		this.remote = remote;

//		this.socket.bind(this.remote);
		// this is requried for read() and write() on the channel
		//this.channel.connect(this.remote);
	}

	/**
	 * The port that the SuperCollider <b>synth</b> engine
	 * usually listens to.
	 * @see #DEFAULT_SC_OSC_PORT
	 */
	public static int defaultSCOSCPort() {
		return DEFAULT_SC_OSC_PORT;
	}

	/**
	 * The port that the SuperCollider <b>language</b> engine
	 * usually listens to.
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

	/**
	 * Returns the port number associated with this port.
	 * @return this ports number
	 */
	protected int getPort() {

		if (remote instanceof InetSocketAddress) {
			return ((InetSocketAddress)remote).getPort();
		} else {
			return -1;
		}
	}

	/**
	 * Close the socket and free-up resources.
	 * It is recommended that clients call this when they are done with the
	 * port.
	 */
	public void close() throws IOException {
		channel.close();
	}
}
