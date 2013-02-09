/*
 * Copyright (C) 2001, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import com.illposed.osc.utility.OSCJavaToByteArrayConverter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * This implementation is based on Markus Gaelli and
 * Iannis Zannos' OSC implementation in Squeak:
 * http://www.emergent.de/Goodies/
 * @see OSCJavaToByteArrayConverter
 */
public class OSCJavaToByteArrayConverterTest extends junit.framework.TestCase {

	private void checkResultEqualsAnswer(byte[] result, byte[] answer) {
		OSCMessageTest.checkResultEqualsAnswer(result, answer);
	}

	/**
	 * This is different from the SmallTalk implementation.
	 * In Squeak, this produces:
	 * byte[] answer = {62, 76, (byte) 204, (byte) 204};
	 * (i.e. answer= {62, 76, -52, -52})
	 *
	 * The source of this discrepancy is Squeak conversion
	 * routine Float>>asIEEE32BitWord vs. the Java
	 * {@link Float#floatToIntBits(float)}.
	 *
	 * 0.2 asIEEE32BitWord yields: 1045220556
	 * {@link Float#floatToIntBits(float)} with parameter 0.2f
	 * yields: (int) 1045220557 (VA Java 3.5)
	 *
	 * Looks like there is an OBO bug somewhere -- either Java or Squeak.
	 */
	public void testPrintFloat2OnStream() {
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		stream.write(Float.valueOf(0.2f));
		byte[] answer = {62, 76, -52, -51};
		byte[] result = stream.toByteArray();
		checkResultEqualsAnswer(result, answer);
	}

	public void testPrintFloatOnStream() {
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		stream.write(Float.valueOf(10.7567f));
		byte[] answer = {65, 44, 27, 113};
		byte[] result = stream.toByteArray();
		checkResultEqualsAnswer(result, answer);
	}

	public void testPrintIntegerOnStream() {
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		stream.write(Integer.valueOf(1124));
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
		checkResultEqualsAnswer(result, answer);
	}

	public void testIfExceptionOnNullWrite() {
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();

		try {
			stream.write((BigInteger) null);
			fail("No exception thrown on writing (BigInteger)null");
		} catch (RuntimeException ex) {
			// ignore
		}

		try {
			stream.write((Float) null);
			fail("No exception thrown on writing (Float)null");
		} catch (RuntimeException ex) {
			// ignore
		}

		try {
			stream.write((Integer) null);
			fail("No exception thrown on writing (Integer)null");
		} catch (RuntimeException ex) {
			// ignore
		}

		try {
			stream.write((Object) null);
			fail("No exception thrown on writing (Object)null");
		} catch (RuntimeException ex) {
			// ignore
		}

		try {
			stream.write((String) null);
			fail("No exception thrown on writing (String)null");
		} catch (RuntimeException ex) {
			// ignore
		}

		try {
			stream.write((byte[]) null);
			fail("No exception thrown on writing (byte[])null");
		} catch (RuntimeException ex) {
			// ignore
		}

		try {
			List<Float> nullFloats = new ArrayList<Float>(1);
			nullFloats.add(null);
			stream.write(nullFloats);
			fail("No exception thrown on writing Float[] {null}");
		} catch (RuntimeException ex) {
			// ignore
		}
	}
}
