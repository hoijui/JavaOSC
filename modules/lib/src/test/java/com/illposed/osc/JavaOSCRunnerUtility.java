package com.illposed.osc;

/**
 * JavaOSCRunnerUtility is a simple utility class to run just a specific
 * test when trying to debug a particular problem.
 *
 * <p>
 * Copyright (C) 2004-2006, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 * <p>
 * See license.txt (or license.rtf) for license information.
 *
 * @author Chandrasekhar Ramakrishnan
 * @version 1.0
 */
public class JavaOSCRunnerUtility {

	public static void main(String args[]) {
//		TestSuite ts = new TestSuite(TestOSCPort.class);
		junit.textui.TestRunner.run(OSCByteArrayToJavaConverterTest.class);

	}
}
