//  Created by cramakri on Thu Dec 13 2001.
//  Copyright (c) 2001 Illposed Software. All rights reserved.
//

/*
 * This implementation is based on Markus Gaelli and
 * Iannis Zannos' OSC implementation in Squeak:
 * http://www.emergent.de/Goodies/
 */

package com.illposed.osc.test;

import com.illposed.osc.utility.*;

public class OSCJavaToByteArrayConverterTest extends junit.framework.TestCase {

	/**
	 * @param name java.lang.String
	 */
	public OSCJavaToByteArrayConverterTest(String name) {
		super(name);
	}

	/**
	 * @param result byte[]
	 * @param answer byte[]
	 */
	private void checkResultEqualsAnswer(byte[] result, byte[] answer) {
		if (result.length != answer.length)
			fail("Didn't convert correctly");
		for (int i = 0; i < result.length; i++) {
			if (result[i] != answer[i])
				fail("Didn't convert correctly");
		}
	}

	/**
	 *
	 * This is different from the Smalltalk implementation.
	 * In Squeak, this produces:
	 * byte[] answer = { 62, 76, (byte) 204, (byte) 204 };
	 * (i.e. answer= {62, 76, -52, -52})
	 *
	 * The source of this discrepency is Squeak conversion 
	 * routine Float>>asIEEE32BitWord vs. the Java
	 * Float::floatToIntBits(float).
	 *
	 * 0.2 asIEEE32BitWord yields: 1045220556
	 * Float.floatToIntBits((float) 0.2) yields: (int) 1045220557 (VA Java 3.5)
	 *
	 * Looks like there is an OBO bug somwhere -- either Java or Squeak.
	 */
	public void testPrintFloat2OnStream() {
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		stream.write(new Float(0.2));
		byte[] answer = { 62, 76, -52, -51 };
		byte[] result = stream.toByteArray();
		checkResultEqualsAnswer(result, answer);
	}

	public void testPrintFloatOnStream() {
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		stream.write(new Float(10.7567));
		byte[] answer = { 65, 44, 27, 113 };
		byte[] result = stream.toByteArray();
		checkResultEqualsAnswer(result, answer);
	}

	public void testPrintIntegerOnStream() {
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		stream.write(new Integer(1124));
		byte[] answer = { 0, 0, 4, 100 };
		byte[] result = stream.toByteArray();
		checkResultEqualsAnswer(result, answer);
	}

	public void testPrintString2OnStream() {
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		stream.write("abcd");
		byte[] answer = { 97, 98, 99, 100, 0, 0, 0, 0 };
		byte[] result = stream.toByteArray();
		checkResultEqualsAnswer(result, answer);
	}

	public void testPrintStringOnStream() {
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		stream.write("abc");
		byte[] answer = { 97, 98, 99, 0 };
		byte[] result = stream.toByteArray();
		checkResultEqualsAnswer(result, answer);
	}

	public void testPrintBigIntegerOnStream() {
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		stream.write(new java.math.BigInteger("1124"));
		byte[] answer = { 0, 0, 0, 0, 0, 0, 4, 100 };
		byte[] result = stream.toByteArray();
		System.out.println("result length " + result.length);
		for (int i = 0; i < result.length; i++) {
			System.out.print(result[i]);
		}
		System.out.println("");
		checkResultEqualsAnswer(result, answer);
	}
	
}