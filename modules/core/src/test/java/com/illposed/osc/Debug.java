// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc;

import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPacket;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

public class Debug
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

	public static String debug(OSCPacket packet) {
		return debug(packet, 0);
	}
}
