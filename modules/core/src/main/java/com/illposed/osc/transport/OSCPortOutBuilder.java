// SPDX-FileCopyrightText: 2019 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc.transport;

import com.illposed.osc.OSCSerializerAndParserBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class OSCPortOutBuilder {

	private OSCSerializerAndParserBuilder serializerBuilder;
	private SocketAddress remote;
	private SocketAddress local;
	private NetworkProtocol networkProtocol = NetworkProtocol.UDP;
	private Transport transport;

	public OSCPortOut build() throws IOException {

		// If transport is set, other settings cannot be used
		if (transport != null) {
			if (remote != null) {
				throw new IllegalArgumentException(
					"Cannot use remote socket address / port in conjunction with transport.");
			}

			if (local != null) {
				throw new IllegalArgumentException(
					"Cannot use local socket address / port in conjunction with transport.");
			}

			if (serializerBuilder != null) {
				throw new IllegalArgumentException(
					"Cannot use serializerBuilder in conjunction with transport.");
			}

			return new OSCPortOut(
				transport
			);
		}

		if (remote == null) {
			throw new IllegalArgumentException(
				"Missing remote socket address / port.");
		}

		if (local == null) {
			local = new InetSocketAddress(OSCPort.generateWildcard(remote), 0);
		}

		if (serializerBuilder == null) {
			serializerBuilder = new OSCSerializerAndParserBuilder();
		}

		return new OSCPortOut(
			serializerBuilder, remote, local, networkProtocol
		);
	}

	public OSCPortOutBuilder setPort(final int port) {
		final SocketAddress address = new InetSocketAddress(port);
		local = address;
		remote = address;
		return this;
	}

	public OSCPortOutBuilder setRemotePort(final int port) {
		remote = new InetSocketAddress(port);
		return this;
	}

	public OSCPortOutBuilder setLocalPort(final int port) {
		local = new InetSocketAddress(port);
		return this;
	}

	public OSCPortOutBuilder setSocketAddress(final SocketAddress address) {
		local = address;
		remote = address;
		return this;
	}

	public OSCPortOutBuilder setLocalSocketAddress(final SocketAddress address) {
		local = address;
		return this;
	}

	public OSCPortOutBuilder setRemoteSocketAddress(final SocketAddress address) {
		remote = address;
		return this;
	}

	public OSCPortOutBuilder setNetworkProtocol(final NetworkProtocol protocol) {
		networkProtocol = protocol;
		return this;
	}

	public OSCPortOutBuilder setTransport(final Transport networkTransport) {
		transport = networkTransport;
		return this;
	}
}
