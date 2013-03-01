/*
 * Copyright (C) 2004-2006, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

/**
 * OSCPortOut is the class that sends OSC messages
 * to a specific address and port.
 *
 * To send an OSC message, call send().
 *
 * An example based on
 * {@link com.illposed.osc.OSCPortTest#testMessageWithArgs()}:
 * <pre>
	OSCPortOut sender = new OSCPortOut();
	List<Object> args = new ArrayList<Object>(2);
	args.add(Integer.valueOf(3));
	args.add("hello");
	OSCMessage msg = new OSCMessage("/sayhello", args);
	 try {
		sender.send(msg);
	 } catch (Exception e) {
		 showError("Couldn't send");
	 }
 * </pre>
 *
 * @author Chandrasekhar Ramakrishnan
 */
public class OSCPortOut extends OSCPort {

	public OSCPortOut(SocketAddress address) throws IOException {
		super(address);
//		getChannel().socket().bind(address);
		getChannel().connect(address);
	}

	/**
	 * Create an OSCPort that sends to address:port.
	 * @param address the UDP address to send to
	 * @param port the UDP port to send to
	 */
	public OSCPortOut(InetAddress address, int port)
		throws IOException
	{
		this(new InetSocketAddress(address, port));
	}

	/**
	 * Create an OSCPort that sends to address,
	 * using the standard SuperCollider port.
	 * @param address the UDP address to send to
	 */
	public OSCPortOut(InetAddress address) throws IOException {
		this(address, DEFAULT_SC_OSC_PORT);
	}

	/**
	 * Create an OSCPort that sends to "localhost",
	 * on the standard SuperCollider port.
	 */
	public OSCPortOut() throws UnknownHostException, IOException {
		this(InetAddress.getLocalHost(), DEFAULT_SC_OSC_PORT);
	}

	/**
	 * Send an OSC packet (message or bundle) to the receiver
	 * we are connected to.
	 * @param aPacket the bundle or message to send
	 */
	public void send(OSCPacket aPacket) throws IOException {
		getChannel().write(aPacket.getBytes());
	}
}
