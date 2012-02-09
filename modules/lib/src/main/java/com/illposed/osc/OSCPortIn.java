package com.illposed.osc;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.IOException;
import java.net.SocketException;

import com.illposed.osc.utility.OSCByteArrayToJavaConverter;
import com.illposed.osc.utility.OSCPacketDispatcher;

/**
 * OSCPortIn is the class that listens for OSC messages.
 * <p>
 * An example based on com.illposed.osc.test.OSCPortTest::testReceiving() :
 * <pre>

	receiver = new OSCPortIn(OSCPort.DEFAULT_SC_OSC_PORT());
	OSCListener listener = new OSCListener() {
		public void acceptMessage(java.util.Date time, OSCMessage message) {
			System.out.println("Message received!");
		}
	};
	receiver.addListener("/message/receiving", listener);
	receiver.startListening();

 * </pre>
 * <p>
 * Then, using a program such as SuperCollider or sendOSC, send a message
 * to this computer, port 57110 (DEFAULT_SC_OSC_PORT), with the address
 * /message/receiving
 * <p>
 * Copyright (C) 2004-2006, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 * <p>
 * See license.txt (or license.rtf) for license information.
 *
 * @author Chandrasekhar Ramakrishnan
 * @version 1.0
 */
public class OSCPortIn extends OSCPort implements Runnable {

	// state for listening
	protected boolean isListening;
	protected OSCByteArrayToJavaConverter converter
			= new OSCByteArrayToJavaConverter();
	protected OSCPacketDispatcher dispatcher = new OSCPacketDispatcher();

	/**
	 * Create an OSCPort that listens on the specified port.
	 * @param port UDP port to listen on.
	 * @throws SocketException
	 */
	public OSCPortIn(int port) throws SocketException {
		socket = new DatagramSocket(port);
		this.port = port;
	}

	/**
	 * Buffers were 1500 bytes in size, but were
	 * increased to 1536, as this is a common MTU.
	 */
	private static final int BUFFER_SIZE = 1536;

	/**
	 * Run the loop that listens for OSC on a socket until
	 * @{link #isListening()} becomes false.
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		byte[] buffer = new byte[BUFFER_SIZE];
		DatagramPacket packet = new DatagramPacket(buffer, BUFFER_SIZE);
		while (isListening) {
			try {
				socket.receive(packet);
				OSCPacket oscPacket = converter.convert(buffer,
						packet.getLength());
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
		isListening = true;
		Thread thread = new Thread(this);
		thread.start();
	}

	/**
	 * Stop listening for incoming OSCPackets
	 */
	public void stopListening() {
		isListening = false;
	}

	/**
	 * Am I listening for packets?
	 */
	public boolean isListening() {
		return isListening;
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
