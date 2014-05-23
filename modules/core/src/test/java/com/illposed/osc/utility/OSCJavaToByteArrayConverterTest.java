/*
 * Copyright (C) 2001, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.utility;

import com.illposed.osc.OSCMessageTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * This implementation is based on Markus Gaelli and Iannis Zannos's
 * <a href="http://www.emergent.de/Goodies/">OSC implementation in Squeak</a>
 * @see OSCJavaToByteArrayConverter
 */
public class OSCJavaToByteArrayConverterTest {

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
	 * routine <code>Float&gt;&gt;asIEEE32BitWord</code> vs. the Java
	 * {@link Float#floatToIntBits(float)}.
	 *
	 * <code>0.2 asIEEE32BitWord</code> yields: 1045220556
	 * {@link Float#floatToIntBits(float)} with parameter 0.2f
	 * yields: (int) 1045220557 (VA Java 3.5)
	 *
	 * Looks like there is an OBO bug somewhere -- either Java or Squeak.
	 */
	@Test
	public void testPrintFloat2OnStream() {
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		stream.write(0.2f);
		byte[] answer = {62, 76, -52, -51};
		byte[] result = stream.toByteArray();
		checkResultEqualsAnswer(result, answer);
	}

	@Test
	public void testPrintFloatOnStream() {
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		stream.write(10.7567f);
		byte[] answer = {65, 44, 27, 113};
		byte[] result = stream.toByteArray();
		checkResultEqualsAnswer(result, answer);
	}

	@Test
	public void testPrintIntegerOnStream() {
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		stream.write(Integer.valueOf(1124));
		byte[] answer = {0, 0, 4, 100};
		byte[] result = stream.toByteArray();
		checkResultEqualsAnswer(result, answer);
	}

	@Test
	public void testPrintString2OnStream() {
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		stream.write("abcd");
		byte[] answer = {97, 98, 99, 100, 0, 0, 0, 0};
		byte[] result = stream.toByteArray();
		checkResultEqualsAnswer(result, answer);
	}

	@Test
	public void testPrintStringOnStream() {
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		stream.write("abc");
		byte[] answer = {97, 98, 99, 0};
		byte[] result = stream.toByteArray();
		checkResultEqualsAnswer(result, answer);
	}

	@Test
	public void testPrintLongOnStream() {
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		stream.write(1124L);
		byte[] answer = {0, 0, 0, 0, 0, 0, 4, 100};
		byte[] result = stream.toByteArray();
		checkResultEqualsAnswer(result, answer);
	}

	@Test
	public void testIfExceptionOnNullWrite() {
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();

		try {
			Long nullLong = null;
			stream.write(nullLong);
			Assert.fail("No exception thrown on writing (Long)null");
		} catch (RuntimeException ex) {
			// ignore
		}

		try {
			Float nullFloat = null;
			stream.write(nullFloat);
			Assert.fail("No exception thrown on writing (Float)null");
		} catch (RuntimeException ex) {
			// ignore
		}

		try {
			Double nullDouble = null;
			stream.write(nullDouble);
			Assert.fail("No exception thrown on writing (Double)null");
		} catch (RuntimeException ex) {
			// ignore
		}

		try {
			Integer nullInteger = null;
			stream.write(nullInteger);
			Assert.fail("No exception thrown on writing (Integer)null");
		} catch (RuntimeException ex) {
			// ignore
		}

		try {
			String nullString = null;
			stream.write(nullString);
			Assert.fail("No exception thrown on writing (String)null");
		} catch (RuntimeException ex) {
			// ignore
		}

		try {
			Character nullCharacter = null;
			stream.write(nullCharacter);
			Assert.fail("No exception thrown on writing (Character)null");
		} catch (RuntimeException ex) {
			// ignore
		}

		try {
			byte[] nullByteArray = null;
			stream.write(nullByteArray);
			Assert.fail("No exception thrown on writing (byte[])null");
		} catch (RuntimeException ex) {
			// ignore
		}
	}
}
