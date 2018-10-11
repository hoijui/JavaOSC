/*
 * Copyright (C) 2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import com.illposed.osc.messageselector.JavaRegexAddressMessageSelector;
import com.illposed.osc.transport.udp.OSCPortIn;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

/**
 * Runs a basic OSC server, printing all received OSC content
 * to <code>stdout</code>.
 * Errors and warnings are printed to <code>stderr</code>.
 * @see EchoOSCMessageListener
 */
public class ConsoleEchoServer extends OSCPortIn {

	@SuppressWarnings("WeakerAccess") // Public API
	public static final int DEFAULT_PORT = 7770;
	private static final int ARG_INDEX_ADDRESS = 0;
	private static final int ARG_INDEX_PORT = 1;

	private final PrintStream out;

	private final class PrintBadDataListener implements OSCBadDataListener {

		PrintBadDataListener() {
			// declared only for setting the access level
		}

		@Override
		public void badDataReceived(final OSCBadDataEvent evt) {

			System.err.printf(
					"Warning: Bad packet received while listening on %s ...%n",
					ConsoleEchoServer.this.toString());
			evt.getException().printStackTrace(System.err);
			System.err.printf(
					"### Bad received data: ###%n%s%n###%n%n",
					new String(evt.getData().array(), Charset.forName("UTF-8")));

			evt.getException().printStackTrace(System.err);
		}

	}

	@SuppressWarnings("WeakerAccess") // Public API
	public ConsoleEchoServer(final SocketAddress serverAddress, final PrintStream out) throws IOException {
		super(
				OSCParserFactory.createDefaultFactory(),
				new OSCPacketDispatcher(),
				serverAddress);

		this.out = out;
	}

	@SuppressWarnings("WeakerAccess") // Public API
	public void start() {

		final OSCMessageListener listener = new EchoOSCMessageListener(out);
		// select all messages
		getDispatcher().addListener(
				new JavaRegexAddressMessageSelector(".*"),
				listener);
		// log errors to console
		getDispatcher().addBadDataListener(new PrintBadDataListener());
		// never stop listening
		setResilient(true);
		setDaemonListener(false);
		startListening();
		out.printf("# Listening for OSC Packets on %s ...%n",
				getLocalAddress().toString());
	}

	@SuppressWarnings("WeakerAccess") // Public API
	public static SocketAddress parseServerAddress(final String[] args)
			throws UnknownHostException
	{
		final InetAddress host;
		int port = DEFAULT_PORT;
		if (args.length > ARG_INDEX_ADDRESS) {
			host = InetAddress.getByName(args[ARG_INDEX_ADDRESS]);
			if (args.length > ARG_INDEX_PORT) {
				// try if we just got a port
				try {
					port = Integer.parseInt(args[ARG_INDEX_PORT]);
				} catch (final NumberFormatException ex) {
					System.err.println("# ERROR: Invalid port: " + args[ARG_INDEX_PORT]);
					throw ex;
				}
			}
		} else {
			// this will use the wildcard address "0.0.0.0": all local addresses
			host = null;
		}
		return new InetSocketAddress(host, port);
	}

	public static void main(final String[] args) throws IOException {
		new ConsoleEchoServer(parseServerAddress(args), System.out).start();
	}
}
