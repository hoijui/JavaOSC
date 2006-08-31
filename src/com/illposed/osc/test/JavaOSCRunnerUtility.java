package com.illposed.osc.test;

/**
 * JavaOSCRunnerUtility is a simpler utility class to run just a specific
 * test when trying to debug a particular problem. It's used by the ant
 * "runtests" task.
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
