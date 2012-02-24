/*
 * Copyright (C) 2001, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import com.illposed.osc.utility.OSCJavaToByteArrayConverter;

/**
 * This implementation is based on Markus Gaelli and
 * Iannis Zannos' OSC implementation in Squeak:
 * http://www.emergent.de/Goodies/
 */
public class OSCJavaToByteArrayConverterTest extends junit.framework.TestCase {

	/**
	 * @param name this tests name
	 */
	public OSCJavaToByteArrayConverterTest(String name) {
		super(name);
	}

	private void checkResultEqualsAnswer(byte[] result, byte[] answer) {
		OSCMessageTest.checkResultEqualsAnswer(result, answer);
	}

	/**
	 *
	 * This is different from the SmallTalk implementation.
	 * In Squeak, this produces:
	 * byte[] answer = {62, 76, (byte) 204, (byte) 204};
	 * (i.e. answer= {62, 76, -52, -52})
	 *
	 * The source of this discrepancy is Squeak conversion
	 * routine Float>>asIEEE32BitWord vs. the Java
	 * Float::floatToIntBits(float).
	 *
	 * 0.2 asIEEE32BitWord yields: 1045220556
	 * Float.floatToIntBits((float) 0.2) yields: (int) 1045220557 (VA Java 3.5)
	 *
	 * Looks like there is an OBO bug somewhere -- either Java or Squeak.
	 */
	public void testPrintFloat2OnStream() {
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		stream.write(new Float(0.2));
		byte[] answer = {62, 76, -52, -51};
		byte[] result = stream.toByteArray();
		checkResultEqualsAnswer(result, answer);
	}

	public void testPrintFloatOnStream() {
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		stream.write(new Float(10.7567));
		byte[] answer = {65, 44, 27, 113};
		byte[] result = stream.toByteArray();
		checkResultEqualsAnswer(result, answer);
	}

	public void testPrintIntegerOnStream() {
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		stream.write(new Integer(1124));
		byte[] answer = {0, 0, 4, 100};
		byte[] result = stream.toByteArray();
		checkResultEqualsAnswer(result, answer);
	}

	public void testPrintString2OnStream() {
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		stream.write("abcd");
		byte[] answer = {97, 98, 99, 100, 0, 0, 0, 0};
		byte[] result = stream.toByteArray();
		checkResultEqualsAnswer(result, answer);
	}

	public void testPrintStringOnStream() {
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		stream.write("abc");
		byte[] answer = {97, 98, 99, 0};
		byte[] result = stream.toByteArray();
		checkResultEqualsAnswer(result, answer);
	}

	public void testPrintBigIntegerOnStream() {
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		stream.write(new java.math.BigInteger("1124"));
		byte[] answer = {0, 0, 0, 0, 0, 0, 4, 100};
		byte[] result = stream.toByteArray();
		System.out.println("result length " + result.length);
		for (int i = 0; i < result.length; i++) {
			System.out.print(result[i]);
		}
		System.out.println("");
		checkResultEqualsAnswer(result, answer);
	}
}
