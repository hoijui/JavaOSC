/*
 * Copyright (C) 2004-2006, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

/**
 * JavaOSCRunnerUtility is a simple utility class to run just a specific
 * test when trying to debug a particular problem.
 *
 * @author Chandrasekhar Ramakrishnan
 */
public class JavaOSCRunnerUtility {

	public static void main(String args[]) {
//		TestSuite ts = new TestSuite(TestOSCPort.class);
		junit.textui.TestRunner.run(OSCByteArrayToJavaConverterTest.class);

	}
}
