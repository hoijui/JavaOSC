// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc;

// To run from the command line:
//
// cd modules/core
//
// mvn test-compile exec:java \
//   -Dexec.mainClass=com.illposed.osc.DebugServer \
//   -Dexec.classpathScope=test \
//   -Dexec.args="udp 12345" # protocol and port number

import com.illposed.osc.OSCBadDataEvent;
import com.illposed.osc.OSCPacketEvent;
import com.illposed.osc.OSCPacketListener;
import com.illposed.osc.transport.NetworkProtocol;
import com.illposed.osc.transport.OSCPortIn;
import com.illposed.osc.transport.OSCPortInBuilder;

public class DebugServer
{
	public static void main(String[] args) throws Exception
	{
		if (args.length != 2) {
			System.err.println("Usage: DebugServer PROTOCOL PORT");
			System.exit(1);
		}

		String protocolStr = args[0].toLowerCase();

		NetworkProtocol protocol;
		switch (protocolStr) {
			case "udp":
				protocol = NetworkProtocol.UDP;
				break;
			case "tcp":
				protocol = NetworkProtocol.TCP;
				break;
			default:
				throw new IllegalArgumentException("Invalid protocol: " + protocolStr);
		}

		int port = Integer.parseInt(args[1]);

		OSCPacketListener debugger = new OSCPacketListener() {
			public void handlePacket(OSCPacketEvent event) {
				System.out.println(Debug.debug(event.getPacket()) + "\n");
			}

			public void handleBadData(OSCBadDataEvent event) {
				System.out.println("bad data event: " + event);
			}
		};

		OSCPortIn server =
			new OSCPortInBuilder()
				.setNetworkProtocol(protocol)
				.setLocalPort(port)
				.setPacketListener(debugger)
				.build();

		System.out.printf("Listening via %s on port %d...\n", protocol, port);

		server.startListening();

		while (true) {
			Thread.sleep(100);
		}
	}
}
