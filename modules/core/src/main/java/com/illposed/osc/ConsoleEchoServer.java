// SPDX-FileCopyrightText: 2017 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc;

import com.illposed.osc.messageselector.JavaRegexAddressMessageSelector;
import com.illposed.osc.transport.OSCPortIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

/**
 * Runs a basic OSC server, printing all received OSC content
 * to <code>stdout</code>.
 * Errors and warnings are printed to <code>stderr</code>.
 * @see EchoOSCMessageListener
 */
public class ConsoleEchoServer extends OSCPortIn {

	// Public API
	@SuppressWarnings("WeakerAccess")
	public static final int DEFAULT_PORT = 7770;
	private static final int ARG_INDEX_ADDRESS = 0;
	private static final int ARG_INDEX_PORT = 1;

	private static final Logger LOG = LoggerFactory.getLogger(ConsoleEchoServer.class);

	private final Logger log;

	private final class PrintBadDataListener implements OSCBadDataListener {

		PrintBadDataListener() {
			// declared only for setting the access level
		}

		@Override
		public void badDataReceived(final OSCBadDataEvent evt) {

			if (log.isWarnEnabled()) {
				log.warn("Bad packet received while listening on " + ConsoleEchoServer.this.toString() + " ...",
						evt.getException());
				log.warn(
						"### Received data (bad): ###\n{}\n###\n\n",
						new String(evt.getData().array(), StandardCharsets.UTF_8));
			}
		}
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public ConsoleEchoServer(final SocketAddress serverAddress, final Logger log) throws IOException {
		super(serverAddress);
		this.log = log;
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public void start() {

		final OSCMessageListener listener = new EchoOSCMessageListener(log);
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
		log.info("# Listening for OSC Packets via {} ...", getTransport());
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
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
					LOG.error("# ERROR: Invalid port: {}", args[ARG_INDEX_PORT]);
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
		new ConsoleEchoServer(parseServerAddress(args), LoggerFactory.getLogger(ConsoleEchoServer.class)).start();
	}
}
