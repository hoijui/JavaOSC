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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * This implementation is based on Markus Gaelli and
 * Iannis Zannos' OSC implementation in Squeak:
 * http://www.emergent.de/Goodies/
 * @see OSCJavaToByteArrayConverter
 */
public class OSCJavaToByteArrayConverterTest extends junit.framework.TestCase {

	private void checkEncoding(Object toBeEncoded, byte[] expectedBytes) {
		ByteBuffer expected = ByteBuffer.wrap(expectedBytes);
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		stream.write(toBeEncoded);
		ByteBuffer result = stream.toBytes();

		OSCMessageTest.check(expected, result);
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
		Object toBeEncoded = Float.valueOf(0.2f);
		byte[] expected = {62, 76, -52, -51};
		checkEncoding(toBeEncoded, expected);
	}

	public void testPrintFloatOnStream() {
		Object toBeEncoded = Float.valueOf(10.7567f);
		byte[] expected = {65, 44, 27, 113};
		checkEncoding(toBeEncoded, expected);
	}

	public void testPrintIntegerOnStream() {
		Object toBeEncoded = Integer.valueOf(1124);
		byte[] expected = {0, 0, 4, 100};
		checkEncoding(toBeEncoded, expected);
	}

	public void testPrintString2OnStream() {
		Object toBeEncoded = "abcd";
		byte[] expected = {97, 98, 99, 100, 0, 0, 0, 0};
		checkEncoding(toBeEncoded, expected);
	}

	public void testPrintStringOnStream() {
		Object toBeEncoded = "abc";
		byte[] expected = {97, 98, 99, 0};
		checkEncoding(toBeEncoded, expected);
	}

	public void testPrintBigIntegerOnStream() {
		Object toBeEncoded = new java.math.BigInteger("1124");
		byte[] expected = {0, 0, 0, 0, 0, 0, 4, 100};
		checkEncoding(toBeEncoded, expected);
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
			stream.write((ByteBuffer) null);
			fail("No exception thrown on writing (ByteBuffer)null");
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
