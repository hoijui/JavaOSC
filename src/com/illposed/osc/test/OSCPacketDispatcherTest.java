/* $Id$
 * Created on 28.10.2003
 */
package com.illposed.osc.test;


import java.util.Date;

import com.illposed.osc.*;
import com.illposed.osc.utility.*;

/**
 * @author cramakrishnan
 *
 * Copyright (C) 2003, C. Ramakrishnan / Auracle
 * All rights reserved.
 * 
 * See license.txt (or license.rtf) for license information.
 */
public class OSCPacketDispatcherTest extends junit.framework.TestCase {
	
	OSCPacketDispatcher dispatcher;
	boolean receivedOnListener1;
	boolean receivedOnListener2;

	/**
	 * Run the OSCPacketDispatcher through its paces
	 */
	public OSCPacketDispatcherTest() {
		super();
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		dispatcher = new OSCPacketDispatcher();
		OSCListener myListener;
		myListener = new OSCListener() {
			public void acceptMessage(Date time, OSCMessage message) {
				receivedOnListener1 = true;
			}
		};
		dispatcher.addListener("/listener1", myListener);
		myListener = new OSCListener() {
			public void acceptMessage(Date time, OSCMessage message) {
				receivedOnListener2 = true;
			}
		};
		dispatcher.addListener("/listener2", myListener);
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		
	}
	
	public void testDispatchToListener1() throws Exception {
		receivedOnListener1 = false;
		receivedOnListener2 = false;
		OSCMessage message = new OSCMessage("/listener1");
		dispatcher.dispatchPacket(message);
		if (!receivedOnListener1)
			fail("Message to listener1 didn't get sent to listener1");
		if (receivedOnListener2)
			fail("Message to listener1 got sent to listener2");
	}
	
	public void testDispatchToListener2() throws Exception {
		receivedOnListener1 = false;
		receivedOnListener2 = false;
		OSCMessage message = new OSCMessage("/listener2");
		dispatcher.dispatchPacket(message);
		if (receivedOnListener1)
			fail("Message to listener2 got sent to listener1");
		if (!receivedOnListener2)
			fail("Message to listener2 didn't get sent to listener2");
	}

	public void testDispatchToNobody() throws Exception {
		receivedOnListener1 = false;
		receivedOnListener2 = false;
		OSCMessage message = new OSCMessage("/nobody");
		dispatcher.dispatchPacket(message);
		if (receivedOnListener1 || receivedOnListener2)
			fail("Message to nobody got dispatched incorrectly");
	}
	
	public void testDispatchBundle() throws Exception {
		receivedOnListener1 = false;
		receivedOnListener2 = false;
		OSCBundle bundle = new OSCBundle();
		bundle.addPacket(new OSCMessage("/listener1"));
		bundle.addPacket(new OSCMessage("/listener2"));
		dispatcher.dispatchPacket(bundle);
		if (!receivedOnListener1)
			fail("Bundle didn't dispatch message to listener 1");
		if (!receivedOnListener2)
			fail("Bundle didn't dispatch message to listener 2");		
	}
}
