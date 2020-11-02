/*
 * Copyright (C) 2001, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.illposed.osc;

import com.illposed.osc.argument.handler.StringArgumentHandler;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import org.junit.Test;

/**
 * This implementation is based on Markus Gaelli and Iannis Zannos's
 * <a href="http://www.emergent.de/Goodies/">OSC implementation in Squeak</a>
 * @see OSCSerializer
 */
public class OSCSerializerTest {

	private void checkResultEqualsAnswer(byte[] result, byte[] answer) {
		OSCMessageTest.checkResultEqualsAnswer(result, answer);
	}

	private static int calcTypeIdentifiersStrLength(final int numArguments) {

		if (numArguments == 0) {
			return 0;
		}
		return ((numArguments + 5) / 4) * 4;
	}

	private void checkSerializedArguments(
			final Charset charset,
			final byte[] expected,
			final Object... arguments)
			throws OSCSerializeException
	{
		final ByteBuffer buffer = ByteBuffer.allocate(1024);
		final BufferBytesReceiver bytesReceiver = new BufferBytesReceiver(buffer);
		final OSCSerializerAndParserBuilder serializerBuilder = new OSCSerializerAndParserBuilder();
		if (charset != null) {
			final Map<String, Object> properties = new HashMap<>();
			properties.put(StringArgumentHandler.PROP_NAME_CHARSET, charset);
			serializerBuilder.addProperties(properties);
		}
		final OSCSerializer stream = serializerBuilder.buildSerializer(bytesReceiver);
		final OSCMessage oscMessage = new OSCMessage("/ab", Arrays.asList(arguments));
		stream.write(oscMessage);
		byte[] result = bytesReceiver.toByteArray();
		final int toBeStrippedOffPrefixBytes = 4 + calcTypeIdentifiersStrLength(arguments.length);
		result = Arrays.copyOfRange(result, toBeStrippedOffPrefixBytes, result.length);
		checkResultEqualsAnswer(result, expected);
	}

	private void checkSerializedArguments(
			final byte[] expected,
			final Object... arguments)
			throws OSCSerializeException
	{
		checkSerializedArguments(null, expected, arguments);
	}

	/**
	 * This is different from the SmallTalk implementation.
	 * In Squeak, this produces:
	 * byte[] answer = {62, 76, (byte) 204, (byte) 204};
	 * (i.e. answer= {62, 76, -52, -52})
	 *
	 * The source of this discrepancy is Squeak conversion
	 * routine <code>Float&gt;&gt;asIEEE32BitWord</code> vs. the Java
	 * {@link Float#floatToIntBits(float)}.
	 *
	 * <code>0.2 asIEEE32BitWord</code> yields: 1045220556
	 * {@link Float#floatToIntBits(float)} with parameter 0.2f
	 * yields: (int) 1045220557 (VA Java 3.5)
	 *
	 * Looks like there is an OBO bug somewhere -- either Java or Squeak.
	 * @throws Exception because {@link OSCSerializer#write(Object)} may throw something
	 */
	@Test
	public void testSerializeFloat2() throws Exception {
		checkSerializedArguments(
				new byte[] {62, 76, -52, -51},
				0.2f);
	}

	@Test
	public void testSerializeFloat() throws Exception {
		checkSerializedArguments(
				new byte[] {65, 44, 27, 113},
				10.7567f);
	}

	@Test
	public void testSerializeInteger() throws Exception {
		checkSerializedArguments(
				new byte[] {0, 0, 4, 100},
				1124);
	}

	@Test
	public void testSerializeStringAndInt() throws Exception {
		checkSerializedArguments(
				new byte[] {47, 101, 120, 97, 109, 112, 108, 101, 49, 0, 0, 0, 0, 0, 0, 100},
				"/example1",
				100);
	}

	@Test
	public void testSerializeString2() throws Exception {
		//noinspection SpellCheckingInspection
		checkSerializedArguments(
				new byte[] {97, 98, 99, 100, 0, 0, 0, 0},
				"abcd");
	}

	@Test
	public void testSerializeString3() throws Exception {
		checkSerializedArguments(
				StandardCharsets.UTF_8,
				new byte[] {(byte) 0xc3, (byte) 0xa1, 0, 0},
				"\u00e1"); // latin 'a' with an acute accent
	}

	@Test
	public void testSerializeString() throws Exception {
		checkSerializedArguments(
				new byte[] {97, 98, 99, 0},
				"abc");
	}

	@Test
	public void testSerializeLong() throws Exception {
		checkSerializedArguments(
				new byte[] {0, 0, 0, 0, 0, 0, 4, 100},
				1124L);
	}
}
