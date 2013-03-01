/*
 * Copyright (C) 2003, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import com.illposed.osc.utility.OSCByteArrayToJavaConverter;
import com.illposed.osc.utility.OSCJavaToByteArrayConverter;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Chandrasekhar Ramakrishnan
 * @see OSCMessage
 */
public class OSCMessageTest extends junit.framework.TestCase {

	private static OSCPacket reencode(OSCPacket packet) {
		ByteBuffer bytes = packet.getBytes();
		OSCByteArrayToJavaConverter converter = new OSCByteArrayToJavaConverter();
		return converter.convert(bytes);
	}

	private static byte[] toByteArray(ByteBuffer bytes) {
		if (bytes.hasArray()) {
			return bytes.array();
		} else {
			byte[] bytesArray = new byte[bytes.limit() - bytes.position()];
			int oldPos = bytes.position();
			bytes.get(bytesArray);
			bytes.position(oldPos);
			return bytesArray;
		}
	}

	public static void check(ByteBuffer expected, ByteBuffer result) {
		if (expected.compareTo(result) != 0) {
			fail("Computed and expected answers differ: "
					+ "\"" + new String(toByteArray(result)) + "\" (length: "
					+ (result.limit() - result.position()) + ") vs "
					+ "\"" + new String(toByteArray(expected)) + "\" (length: "
					+ (expected.limit() - expected.position()) + ")");
		}
	}

	/**
	 * @param result received from OSC
	 * @param expectedBytes what should have been received
	 */
	public static void check(byte[] expectedBytes, ByteBuffer result) {

		ByteBuffer expected = ByteBuffer.wrap(expectedBytes);
		check(expected, result);
	}

	public static void check(byte[] expectedBytes, OSCPacket packet) {
		check(expectedBytes, packet.getBytes());
	}

	public static void check(byte[] expectedBytes, OSCJavaToByteArrayConverter stream) {
		check(expectedBytes, stream.toBytes());
	}

	public void testEmpty() {
		List<Object> args = new ArrayList<Object>(0);
		OSCMessage message = new OSCMessage("/empty", args);
		byte[] answer = { 47, 101, 109, 112, 116, 121, 0, 0, 44, 0, 0, 0 };
		check(answer, message);
	}

	public void testEmpty2() {
		OSCMessage message = new OSCMessage("/empty");
		byte[] answer = { 47, 101, 109, 112, 116, 121, 0, 0, 44, 0, 0, 0 };
		check(answer, message);
	}

	public void testChangingAfterBytesFetch() {
		OSCMessage message = new OSCMessage("/sc/mixer/volume");
		message.addArgument(Integer.valueOf(1));
		message.getBytes();
		boolean exceptionThrown = false;
		try {
			message.addArgument(Float.valueOf(0.2f));
		} catch (RuntimeException ex) {
			// Good!
			// This is acceptable, though it would also be OK
			// to return the correct bytes later on.
			exceptionThrown = true;
		}
		if (!exceptionThrown) {
			byte[] answer = {
				47, 115, 99, 47, 109, 105, 120, 101, 114, 47, 118, 111,
				108, 117, 109, 101, 0, 0, 0, 0, 44, 105, 102, 0, 0, 0, 0,
				1, 62, 76, -52, -51 };
			check(answer, message);
		}
	}

	public void testDecreaseVolume() {
		List<Object> args = new ArrayList<Object>(2);
		args.add(Integer.valueOf(1));
		args.add(Float.valueOf(0.2f));
		OSCMessage message = new OSCMessage("/sc/mixer/volume", args);
		byte[] answer = {
			47, 115, 99, 47, 109, 105, 120, 101, 114, 47, 118, 111,
			108, 117, 109, 101, 0, 0, 0, 0, 44, 105, 102, 0, 0, 0, 0,
			1, 62, 76, -52, -51 };
		check(answer, message);
	}

	/**
	 * See the comment in
	 * {@link OSCJavaToByteArrayConverterTest#testPrintFloat2OnStream}.
	 */
	public void testIncreaseVolume() {
		List<Object> args = new ArrayList<Object>(2);
		args.add(Integer.valueOf(1));
		args.add(Float.valueOf(1.0f));
		OSCMessage message = new OSCMessage("/sc/mixer/volume", args);
		byte[] answer =	{
			47, 115, 99, 47, 109, 105, 120, 101, 114, 47, 118, 111, 108,
			117, 109, 101, 0, 0, 0, 0, 44, 105, 102, 0, 0, 0, 0, 1,	63,
			(byte) 128, 0, 0};
		check(answer, message);
	}

	public void testPrintStringOnStream() {
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		stream.write("/example1");
		stream.write(100);
		byte[] answer =
			{47, 101, 120, 97, 109, 112, 108, 101, 49, 0, 0, 0, 0, 0, 0, 100};
		check(answer, stream);
	}

	public void testRun() {
		OSCMessage message = new OSCMessage("/sc/run");
		byte[] answer = {47, 115, 99, 47, 114, 117, 110, 0, 44, 0, 0, 0};
		check(answer, message);
	}

	public void testStop() {
		OSCMessage message = new OSCMessage("/sc/stop");
		byte[] answer = {47, 115, 99, 47, 115, 116, 111, 112, 0, 0, 0, 0, 44, 0, 0, 0};
		check(answer, message);
	}

	public void testCreateSynth() {
		OSCMessage message = new OSCMessage("/s_new");
		message.addArgument(Integer.valueOf(1001));
		message.addArgument("freq");
		message.addArgument(Float.valueOf(440.0f));
		byte[] answer = {0x2F, 0x73, 0x5F, 0x6E, 0x65, 0x77, 0, 0, 0x2C, 0x69, 0x73, 0x66, 0, 0, 0, 0, 0, 0, 0x3, (byte) 0xE9, 0x66, 0x72, 0x65, 0x71, 0, 0, 0, 0, 0x43, (byte) 0xDC, 0, 0};
		check(answer, message);
	}

	public void testEncodeBigInteger() {
		OSCMessage message = new OSCMessage("/dummy");
		BigInteger one001 = new BigInteger("1001");
		message.addArgument(one001);
		OSCMessage packet = (OSCMessage) reencode(message);
		if (!packet.getAddress().equals("/dummy")) {
			fail("Send Big Integer did not receive the correct address");
		}
		List<Object> arguments = packet.getArguments();
		if (arguments.size() != 1) {
			fail("Send Big Integer should have 1 argument, not " + arguments.size());
		}
		if (!(arguments.get(0) instanceof BigInteger)) {
			fail("arguments.get(0) should be a BigInteger, not " + arguments.get(0));
		}
		if (!(new BigInteger("1001").equals(arguments.get(0)))) {
			fail("Instead of BigInteger(1001), received " + arguments.get(0));
		}
	}

	public void testEncodeArray() {
		OSCMessage message = new OSCMessage("/dummy");
		List<Float> floats = new ArrayList<Float>(2);
		floats.add(Float.valueOf(10.0f));
		floats.add(Float.valueOf(100.0f));
		message.addArgument(floats);
		OSCMessage packet = (OSCMessage) reencode(message);
		if (!packet.getAddress().equals("/dummy")) {
			fail("Send Array did not receive the correct address");
		}
		List<Object> arguments = packet.getArguments();
		if (arguments.size() != 1) {
			fail("Send Array should have 1 argument, not " + arguments.size());
		}
		if (!(arguments.get(0) instanceof List)) {
			fail("arguments.get(0) should be a Object array, not " + arguments.get(0));
		}
		for (int i = 0; i < 2; ++i) {
			List<Object> theArray = (List<Object>) arguments.get(0);
			if (!floats.get(i).equals(theArray.get(i))) {
				fail("Array element " + i + " should be " + floats.get(i) + " not " + theArray.get(i));
			}
		}
	}
}
