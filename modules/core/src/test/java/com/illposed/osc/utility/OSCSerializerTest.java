/*
 * Copyright (C) 2001, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.utility;

import com.illposed.osc.OSCMessageTest;
import com.illposed.osc.argument.handler.StringArgumentHandler;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
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

	private void checkPrintOnStream(
			final Charset charset,
			final byte[] expected,
			final Object... arguments)
			throws IOException
	{
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final OSCSerializerFactory serializerFactory = OSCSerializerFactory.createDefaultFactory();
		if (charset != null) {
			final Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(StringArgumentHandler.PROP_NAME_CHARSET, charset);
			serializerFactory.setProperties(properties);
		}
		final OSCSerializer stream = serializerFactory.create(buffer);
		for (final Object argument : arguments) {
			stream.write(argument);
		}
		byte[] result = buffer.toByteArray();
		checkResultEqualsAnswer(result, expected);
	}

	private void checkPrintOnStream(
			final byte[] expected,
			final Object... arguments)
			throws IOException
	{
		checkPrintOnStream(null, expected, arguments);
	}

	private OSCSerializer createSimpleTestStream() {
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		return OSCSerializerFactory.createDefaultFactory().create(buffer);
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
	 * @throws IOException because {@link OSCSerializer#write(Object)} may do so
	 */
	@Test
	public void testPrintFloat2OnStream() throws IOException {
		checkPrintOnStream(
				new byte[] {62, 76, -52, -51},
				0.2f);
	}

	@Test
	public void testPrintFloatOnStream() throws IOException {
		checkPrintOnStream(
				new byte[] {65, 44, 27, 113},
				10.7567f);
	}

	@Test
	public void testPrintIntegerOnStream() throws IOException {
		checkPrintOnStream(
				new byte[] {0, 0, 4, 100},
				1124);
	}

	@Test
	public void testPrintStringAndIntOnStream() throws IOException {
		checkPrintOnStream(
				new byte[] {47, 101, 120, 97, 109, 112, 108, 101, 49, 0, 0, 0, 0, 0, 0, 100},
				"/example1",
				100);
	}

	@Test
	public void testPrintString2OnStream() throws IOException {
		checkPrintOnStream(
				new byte[] {97, 98, 99, 100, 0, 0, 0, 0},
				"abcd");
	}

	@Test
	public void testPrintString3OnStream() throws IOException {
		checkPrintOnStream(
				Charset.forName("UTF-8"),
				new byte[] {(byte) 0xc3, (byte) 0xa1, 0, 0},
				"\u00e1"); // latin 'a' with an acute accent
	}

	@Test
	public void testPrintStringOnStream() throws IOException {
		checkPrintOnStream(
				new byte[] {97, 98, 99, 0},
				"abc");
	}

	@Test
	public void testPrintLongOnStream() throws IOException {
		checkPrintOnStream(
				new byte[] {0, 0, 0, 0, 0, 0, 4, 100},
				1124L);
	}
}
