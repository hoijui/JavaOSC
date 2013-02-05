/*
 * Copyright (C) 2003, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.illposed.osc.utility.OSCByteArrayToJavaConverter;
import com.illposed.osc.utility.OSCJavaToByteArrayConverter;

public class OSCMessageTest extends junit.framework.TestCase {

	/**
	 * OscFloatTest constructor comment.
	 * @param name this tests name
	 */
	public OSCMessageTest(String name) {
		super(name);
	}

	/**
	 * @param result received from OSC
	 * @param answer what should have been received
	 */
	public static void checkResultEqualsAnswer(byte[] result, byte[] answer) {
		if (result.length != answer.length) {
			fail(
				"Result and answer aren't the same length "
					+ result.length
					+ " vs "
					+ answer.length);
		}
		for (int i = 0; i < result.length; i++) {
			if (result[i] != answer[i]) {
				String errorString = "Didn't convert correctly: " + i;
				errorString = errorString + " result: " + result[i];
				errorString = errorString + " answer: " + answer[i];
				fail(errorString);
			}
		}
	}

	public void testDecreaseVolume() {
		List<Object> args = new ArrayList<Object>(2);
		args.add(new Integer(1));
		args.add(new Float(0.2));
		OSCMessage message = new OSCMessage("/sc/mixer/volume", args);
		byte[] answer = {
			47, 115, 99, 47, 109, 105, 120, 101, 114, 47, 118, 111,
			108, 117, 109, 101, 0, 0, 0, 0, 44, 105, 102, 0, 0, 0, 0,
			1, 62, 76, -52, -51 };
		byte[] result = message.getByteArray();
		checkResultEqualsAnswer(result, answer);
	}

	/**
	 *
	 * See the comment in TestOscPacketByteArrayConverter::testFloat2OnStream
	 */
	public void testIncreaseVolume() {
		List<Object> args = new ArrayList<Object>(2);
		args.add(new Integer(1));
		args.add(new Float(1.0));
		OSCMessage message = new OSCMessage("/sc/mixer/volume", args);
		byte[] answer =	{
			47, 115, 99, 47, 109, 105, 120, 101, 114, 47, 118, 111, 108,
			117, 109, 101, 0, 0, 0, 0, 44, 105, 102, 0, 0, 0, 0, 1,	63,
			(byte) 128, 0, 0};
		byte[] result = message.getByteArray();
		checkResultEqualsAnswer(result, answer);
	}

	public void testPrintStringOnStream() {
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		stream.write("/example1");
		stream.write(100);
		byte[] answer =
			{47, 101, 120, 97, 109, 112, 108, 101, 49, 0, 0, 0, 0, 0, 0, 100};
		byte[] result = stream.toByteArray();
		checkResultEqualsAnswer(result, answer);
	}

	public void testRun() {
		OSCMessage message = new OSCMessage("/sc/run");
		byte[] answer = {47, 115, 99, 47, 114, 117, 110, 0, 44, 0, 0, 0};
		byte[] result = message.getByteArray();
		checkResultEqualsAnswer(result, answer);
	}

	public void testStop() {
		OSCMessage message = new OSCMessage("/sc/stop");
		byte[] answer = {47, 115, 99, 47, 115, 116, 111, 112, 0, 0, 0, 0, 44, 0, 0, 0};
		byte[] result = message.getByteArray();
		checkResultEqualsAnswer(result, answer);
	}

	public void testCreateSynth() {
		OSCMessage message = new OSCMessage("/s_new");
		message.addArgument(new Integer(1001));
		message.addArgument("freq");
		message.addArgument(new Float(440.0));
		byte[] answer = {0x2F, 0x73, 0x5F, 0x6E, 0x65, 0x77, 0, 0, 0x2C, 0x69, 0x73, 0x66, 0, 0, 0, 0, 0, 0, 0x3, (byte) 0xE9, 0x66, 0x72, 0x65, 0x71, 0, 0, 0, 0, 0x43, (byte) 0xDC, 0, 0};
		byte[] result = message.getByteArray();
		checkResultEqualsAnswer(result, answer);
	}

	public void testSendBigInteger() {
		OSCMessage message = new OSCMessage("/dummy");
		BigInteger one001 = new BigInteger("1001");
		message.addArgument(one001);
		byte[] byteArray = message.getByteArray();
		OSCByteArrayToJavaConverter converter = new OSCByteArrayToJavaConverter();
		OSCMessage packet = (OSCMessage) converter.convert(byteArray, byteArray.length);
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

	public void testSendArray() {
		OSCMessage message = new OSCMessage("/dummy");
		List<Float> floats = new ArrayList<Float>(2);
		floats.add(new Float(10.0));
		floats.add(new Float(100.0));
		message.addArgument(floats);
		byte[] byteArray = message.getByteArray();
		OSCByteArrayToJavaConverter converter = new OSCByteArrayToJavaConverter();
		OSCMessage packet = (OSCMessage) converter.convert(byteArray, byteArray.length);
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
