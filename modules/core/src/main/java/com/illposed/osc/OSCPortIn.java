/*
 * Copyright (C) 2004-2006, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import com.illposed.osc.utility.OSCByteArrayToJavaConverter;
import com.illposed.osc.utility.OSCPacketDispatcher;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;

/**
 * OSCPortIn is the class that listens for OSC messages.
 *
 * An example based on
 * {@link com.illposed.osc.OSCPortTest#testReceiving()}:
 * <pre>
	OSCPortIn receiver = new OSCPortIn(OSCPort.DEFAULT_SC_OSC_PORT());
	OSCListener listener = new OSCListener() {
		public void acceptMessage(java.util.Date time, OSCMessage message) {
			System.out.println("Message received!");
		}
	};
	receiver.addListener("/message/receiving", listener);
	receiver.startListening();
 * </pre>
 *
 * Then, using a program such as SuperCollider or sendOSC, send a message
 * to this computer, port {@link #DEFAULT_SC_OSC_PORT},
 * with the address "/message/receiving".
 *
 * @author Chandrasekhar Ramakrishnan
 */
public class OSCPortIn extends OSCPort implements Runnable {

	// state for listening
	private boolean listening;
	private final OSCByteArrayToJavaConverter converter;
	private final OSCPacketDispatcher dispatcher;

	public OSCPortIn(SocketAddress local) throws IOException {
		super(local);

		this.converter = new OSCByteArrayToJavaConverter();
		this.dispatcher = new OSCPacketDispatcher();
	}

	/**
	 * Create an OSCPort that listens on the specified port.
	 * Strings will be decoded using the systems default character set.
	 * @param port UDP port to listen on.
	 * @throws IOException if the port number is invalid,
	 *   or there is already something else listening on it
	 */
	public OSCPortIn(int port) throws IOException {
		this(new InetSocketAddress(port));
	}

	/**
	 * Create an OSCPort that listens on the specified port,
	 * and decodes strings with a specific character set.
	 * @param port UDP port to listen on.
	 * @param charset how to decode strings read from incoming packages.
	 *   This includes message addresses and string parameters.
	 * @throws IOException if the port number is invalid,
	 *   or there is already something else listening on it
	 */
	public OSCPortIn(int port, Charset charset) throws IOException {
		this(port);

		this.converter.setCharset(charset);
	}

	/**
	 * Buffers were 1500 bytes in size, but were
	 * increased to 1536, as this is a common MTU.
	 */
	private static final int BUFFER_SIZE = 1536;

	/**
	 * Run the loop that listens for OSC on a socket until
	 * {@link #isListening()} becomes false.
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
		DatagramChannel channel = getChannel();
		while (listening) {
			try {
				buffer.clear();
				long bt = System.currentTimeMillis();
				try {
					System.err.println("XXX reading ...");
//					int read = channel.read(buffer);
					channel.receive(buffer);
					int read = buffer.limit();
					System.err.println("XXX read: " + read);
				} catch (SocketException ex) {
					if (listening) {
						throw ex;
					} else {
						// if we closed the channel while receiving data,
						// the exception is expected/normal, so we hide it
						continue;
					}
				} catch (java.nio.channels.AsynchronousCloseException ex) {
					System.err.println("XXX tried reading for: " + (System.currentTimeMillis() - bt));
				}
				buffer.flip();
				// FIXME this will fail if the packet to be received is larger then BUFFER_SIZE
				OSCPacket oscPacket = converter.convert(buffer);
				dispatcher.dispatchPacket(oscPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Start listening for incoming OSCPackets
	 */
	public void startListening() {
		listening = true;
		Thread thread = new Thread(this); // FIXME store this thread, so it can be shut down in stopListening()
		thread.start();
	}

	/**
	 * Stop listening for incoming OSCPackets
	 */
	public void stopListening() {
		listening = false;
	}

	/**
	 * Am I listening for packets?
	 */
	public boolean isListening() {
		return listening;
	}

	/**
	 * Register the listener for incoming OSCPackets addressed to an Address
	 * @param anAddress  the address to listen for
	 * @param listener   the object to invoke when a message comes in
	 */
	public void addListener(String anAddress, OSCListener listener) {
		dispatcher.addListener(anAddress, listener);
	}
}
