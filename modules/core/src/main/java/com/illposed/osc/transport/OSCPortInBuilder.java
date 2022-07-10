// SPDX-FileCopyrightText: 2019 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc.transport;

import com.illposed.osc.MessageSelector;
import com.illposed.osc.OSCMessageListener;
import com.illposed.osc.OSCPacketDispatcher;
import com.illposed.osc.OSCPacketListener;
import com.illposed.osc.OSCSerializerAndParserBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

public class OSCPortInBuilder {

	private OSCSerializerAndParserBuilder parserBuilder;
	private List<OSCPacketListener> packetListeners;
	private SocketAddress local;
	private SocketAddress remote;
	private NetworkProtocol networkProtocol = NetworkProtocol.UDP;
	private Transport transport;

	private OSCPacketListener addDefaultPacketListener() {
		if (packetListeners == null) {
			packetListeners = new ArrayList<>();
		}

		final OSCPacketListener listener = OSCPortIn.defaultPacketListener();
		packetListeners.add(listener);

		return listener;
	}

	public OSCPortIn build() throws IOException {

		if (packetListeners == null) {
			addDefaultPacketListener();
		}

		// If transport is set, other settings cannot be used
		if (transport != null) {
			if (remote != null) {
				throw new IllegalArgumentException(
					"Cannot use remote socket address / port in conjunction with transport object.");
			}

			if (local != null) {
				throw new IllegalArgumentException(
					"Cannot use local socket address / port in conjunction with transport object.");
			}

			if (parserBuilder != null) {
				throw new IllegalArgumentException(
					"Cannot use parserBuilder in conjunction with transport object.");
			}

			return new OSCPortIn(
				transport, packetListeners
			);
		}

		if (local == null) {
			throw new IllegalArgumentException(
				"Missing local socket address / port.");
		}

		if (remote == null) {
			remote = new InetSocketAddress(OSCPort.generateWildcard(local), 0);
		}

		if (parserBuilder == null) {
			parserBuilder = new OSCSerializerAndParserBuilder();
		}

		return new OSCPortIn(
			parserBuilder, packetListeners, local, remote, networkProtocol
		);
	}

	public OSCPortInBuilder setPort(final int port) {
		final SocketAddress address = new InetSocketAddress(port);
		local = address;
		remote = address;
		return this;
	}

	public OSCPortInBuilder setLocalPort(final int port) {
		local = new InetSocketAddress(port);
		return this;
	}

	public OSCPortInBuilder setRemotePort(final int port) {
		remote = new InetSocketAddress(port);
		return this;
	}

	public OSCPortInBuilder setSocketAddress(final SocketAddress address) {
		local = address;
		remote = address;
		return this;
	}

	public OSCPortInBuilder setLocalSocketAddress(final SocketAddress address) {
		local = address;
		return this;
	}

	public OSCPortInBuilder setRemoteSocketAddress(final SocketAddress address) {
		remote = address;
		return this;
	}

	public OSCPortInBuilder setNetworkProtocol(final NetworkProtocol protocol) {
		networkProtocol = protocol;
		return this;
	}

	public OSCPortInBuilder setTransport(final Transport networkTransport) {
		transport = networkTransport;
		return this;
	}

	public OSCPortInBuilder setPacketListeners(
			final List<OSCPacketListener> listeners)
	{
		packetListeners = listeners;
		return this;
	}

	public OSCPortInBuilder setPacketListener(final OSCPacketListener listener) {
		packetListeners = new ArrayList<>();
		packetListeners.add(listener);
		return this;
	}

	public OSCPortInBuilder addPacketListener(final OSCPacketListener listener) {
		if (packetListeners == null) {
			packetListeners = new ArrayList<>();
		}

		packetListeners.add(listener);
		return this;
	}

	public OSCPortInBuilder addMessageListener(
		final MessageSelector selector, final OSCMessageListener listener)
	{
		OSCPacketDispatcher dispatcher = OSCPortIn.getDispatcher(packetListeners);

		if (dispatcher == null) {
			dispatcher = (OSCPacketDispatcher)addDefaultPacketListener();
		}

		dispatcher.addListener(selector, listener);

		return this;
	}
}
