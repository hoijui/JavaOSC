//
//  TestOscPort.java
//  JavaOSC
//
//  Created by cramakri on Thu Dec 13 2001.
//  Copyright (c) 2001 Illposed Software. All rights reserved.
//

package com.illposed.osc.test;

import java.util.Date;

import com.illposed.osc.*;

public class OSCPortTest extends junit.framework.TestCase {
	boolean messageReceived;
	Date    receivedTimestamp;
	OSCPortOut sender;
	OSCPortIn  receiver;

	public OSCPortTest(String name) {
		super(name);
	}
	
	

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		sender = new OSCPortOut();
		receiver = new OSCPortIn(OSCPort.defaultSCOSCPort());
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		sender.close();
		receiver.close();
	}

	public void testStart() throws Exception {
		OSCMessage mesg = new OSCMessage("/sc/stop");
		sender.send(mesg);
	}

	public void testMessageWithArgs() throws Exception {
		Object args[] = new Object[2];
		args[0] = new Integer(3);
		args[1] = "hello";
		OSCMessage mesg = new OSCMessage("/foo/bar", args);
		sender.send(mesg);
	}

	public void testBundle() throws Exception {
		Object args[] = new Object[2];
		OSCPacket mesgs[] = new OSCPacket[1];
		args[0] = new Integer(3);
		args[1] = "hello";
		OSCMessage mesg = new OSCMessage("/foo/bar", args);
		mesgs[0] = mesg;
		OSCBundle bundle = new OSCBundle(mesgs);
		sender.send(bundle);
	}
	
	public void testBundle2() throws Exception {
		OSCMessage mesg = new OSCMessage("/foo/bar");
		mesg.addArgument(new Integer(3));
		mesg.addArgument("hello");
		OSCBundle bundle = new OSCBundle();
		bundle.addPacket(mesg);
		sender.send(bundle);
	}
	
	public void testReceiving() throws Exception {
		OSCMessage mesg = new OSCMessage("/message/receiving");
		messageReceived = false;
		OSCListener listener = new OSCListener() {
			public void acceptMessage(java.util.Date time, OSCMessage message) {
				messageReceived = true; 
			}
		};
		receiver.addListener("/message/receiving", listener);
		receiver.startListening();
		sender.send(mesg);
		Thread.sleep(100); // wait a bit
		receiver.stopListening();
		if (!messageReceived)
			fail("Message was not received");
	}
	
	public void testBundleReceiving() throws Exception {
		OSCBundle bundle = new OSCBundle();
		bundle.addPacket(new OSCMessage("/bundle/receiving"));
		messageReceived = false;
		receivedTimestamp = null;
		OSCListener listener = new OSCListener() {
			public void acceptMessage(Date time, OSCMessage message) {
				messageReceived = true; 
				receivedTimestamp = time;
			}
		};
		receiver.addListener("/bundle/receiving", listener);
		receiver.startListening();
		sender.send(bundle);
		Thread.sleep(100); // wait a bit
		receiver.stopListening();
		if (!messageReceived)
			fail("Message was not received");
		if (!receivedTimestamp.equals(bundle.getTimestamp()))
			fail("Message should have timestamp " + bundle.getTimestamp() + " but has " + receivedTimestamp);
	}

}
