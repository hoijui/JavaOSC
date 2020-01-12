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
import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPacket;
import com.illposed.osc.OSCPacketEvent;
import com.illposed.osc.OSCPacketListener;
import com.illposed.osc.transport.NetworkProtocol;
import com.illposed.osc.transport.OSCPortIn;
import com.illposed.osc.transport.OSCPortInBuilder;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

public class DebugServer
{
	private static String indent(String str, int indentLevel) {
		String indentation = String.join("", Collections.nCopies(indentLevel, "  "));

		return Arrays.asList(str.split("\\R"))
			.stream()
			.map(line -> indentation + line)
			.collect(Collectors.joining("\n"));
	}

	private static String debug(OSCPacket packet, int indentLevel) {
		if (packet instanceof OSCBundle) {
			OSCBundle bundle = (OSCBundle)packet;

			StringBuilder sb = new StringBuilder();
			sb.append(String.format("bundle (%s)", bundle.getTimestamp()));

			for (OSCPacket entry : bundle.getPackets()) {
				sb.append("\n" + indent(debug(entry), indentLevel + 1));
			}

			return sb.toString();
		}

		OSCMessage message = (OSCMessage)packet;

		StringBuilder sb = new StringBuilder();
		sb.append(String.format("message (%s)", message.getAddress()));

		for (Object argument : message.getArguments()) {
			sb.append(
				"\n" +
				indent(
					argument == null
						? "[null]"
						: String.format(
							"[%s] %s", argument.getClass(), argument.toString()
						),
					indentLevel + 1
				)
			);
		}

		return sb.toString();
	}

	private static String debug(OSCPacket packet) {
		return debug(packet, 0);
	}

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
				System.out.println(debug(event.getPacket()) + "\n");
			}

			public void handleBadData(OSCBadDataEvent event) {
				System.out.println("bad data event: " + event);
			}
		};

		OSCPortIn server =
			new OSCPortInBuilder()
				.setNetworkProtocol(protocol)
				.setPort(port)
				.setPacketListener(debugger)
				.build();

		System.out.printf("Listening via %s on port %d...\n", protocol, port);

		server.startListening();

		while (true) {
			Thread.sleep(100);
		}
	}
}
