package com.illposed.osc;

// To run from the command line:
//
// cd modules/core
//
// mvn test-compile exec:java \
//   -Dexec.mainClass=com.illposed.osc.DebugClient \
//   -Dexec.classpathScope=test \
//   -Dexec.args="udp 12345" # protocol and port number

import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPacket;
import com.illposed.osc.argument.OSCTimeTag64;
import com.illposed.osc.transport.NetworkProtocol;
import com.illposed.osc.transport.OSCPortOut;
import com.illposed.osc.transport.OSCPortOutBuilder;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.function.Supplier;

public class DebugClient
{
	private static int randInt(int bound) {
		return (int)(Math.floor(Math.random() * bound));
	}

	private static List<Supplier<Object>> argFunctions = Arrays.asList(
		() -> randInt(100000),
		() -> Math.random() * 100000,
		() -> "test string " + randInt(1000),
		() -> ("test string " + randInt(1000)).getBytes(),
		() -> OSCTimeTag64.valueOf(new Date()),
		() -> true,
		() -> false,
		() -> null
	);

	private static OSCMessage randomMessage(String address) {
		List<Object> args = new ArrayList<Object>();

		for (int i = 0; i < 1 + randInt(5); i++) {
			args.add(argFunctions.get(randInt(argFunctions.size())).get());
		}

		return new OSCMessage(address, args);
	}

	private static OSCBundle randomBundle() {
		List<OSCPacket> packets = new ArrayList<OSCPacket>();

		for (int i = 0; i < 1 + randInt(5); i++) {
			if (Math.random() < 0.00000001) {
				packets.add(randomBundle());
			} else {
				packets.add(randomMessage("/bundle/message/" + (i + 1)));
			}
		}

		return new OSCBundle(packets);
	}

	public static void main(String[] args) throws Exception
	{
		if (args.length != 2) {
			System.err.println("Usage: DebugClient PROTOCOL PORT");
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

		OSCPortOut client =
			new OSCPortOutBuilder()
				.setNetworkProtocol(protocol)
				.setRemoteSocketAddress(
					new InetSocketAddress(InetAddress.getLoopbackAddress(), port)
				)
				.build();

		System.out.printf("Protocol: %s, port: %d\n", protocol, port);
		System.out.println("Enter m to send a message, b to send a bundle.");

		try (Scanner scanner = new Scanner(System.in)) {
			while (true) {
				System.out.print("> ");
				String input = scanner.nextLine();

				try {
					switch (input) {
						case "m":
							OSCMessage message = randomMessage("/message/address");
							System.out.println(Debug.debug(message));
							client.send(message);
							break;
						case "b":
							OSCBundle bundle = randomBundle();
							System.out.println(Debug.debug(bundle));
							client.send(bundle);
							break;
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}
}
