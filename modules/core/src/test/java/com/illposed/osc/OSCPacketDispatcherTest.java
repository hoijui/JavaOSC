/*
 * Copyright (C) 2003, C. Ramakrishnan / Auracle.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.illposed.osc;

import com.illposed.osc.messageselector.OSCPatternAddressMessageSelector;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @see OSCPacketDispatcher
 */
public class OSCPacketDispatcherTest {

	private OSCPacketDispatcher dispatcher;
	private SimpleOSCMessageListener listener1;
	private SimpleOSCMessageListener listener2;

	@Before
	public void setUp() {
		dispatcher = new OSCPacketDispatcher(new OSCSerializerAndParserBuilder());
		listener1 = new SimpleOSCMessageListener();
		dispatcher.addListener(new OSCPatternAddressMessageSelector("/listener1"), listener1);
		listener2 = new SimpleOSCMessageListener();
		dispatcher.addListener(new OSCPatternAddressMessageSelector("/listener2"), listener2);
	}

	@After
	public void tearDown() {

	}

	@Test
	public void testDispatchToListener1() {
		OSCMessage message = new OSCMessage("/listener1");
		dispatcher.handlePacket(new OSCPacketEvent(this, message));
		if (!listener1.isMessageReceived()) {
			Assert.fail("Message to listener1 didn't get sent to listener1");
		}
		if (listener2.isMessageReceived()) {
			Assert.fail("Message to listener1 got sent to listener2");
		}
	}

	@Test
	public void testDispatchToListener2() {
		OSCMessage message = new OSCMessage("/listener2");
		dispatcher.handlePacket(new OSCPacketEvent(this, message));
		if (listener1.isMessageReceived()) {
			Assert.fail("Message to listener2 got sent to listener1");
		}
		if (!listener2.isMessageReceived()) {
			Assert.fail("Message to listener2 didn't get sent to listener2");
		}
	}

	@Test
	public void testDispatchToNobody() {
		OSCMessage message = new OSCMessage("/nobody");
		dispatcher.handlePacket(new OSCPacketEvent(this, message));
		if (listener1.isMessageReceived() || listener2.isMessageReceived()) {
			Assert.fail("Message to nobody got dispatched incorrectly");
		}
	}

	@Test
	public void testDispatchBundle() {
		OSCBundle bundle = new OSCBundle();
		bundle.addPacket(new OSCMessage("/listener1"));
		bundle.addPacket(new OSCMessage("/listener2"));
		dispatcher.handlePacket(new OSCPacketEvent(this, bundle));
		if (!listener1.isMessageReceived()) {
			Assert.fail("Bundle didn't dispatch message to listener 1");
		}
		if (!listener2.isMessageReceived()) {
			Assert.fail("Bundle didn't dispatch message to listener 2");
		}
	}

	@Test
	public void testDispatchManyArguments() {

		final int numArguments = OSCPacketDispatcher.MAX_ARGUMENTS + 1;
		final List<Object> arguments = new ArrayList<>(numArguments);
		for (int ai = 0; ai < numArguments; ai++) {
			arguments.add(ai);
		}
		OSCMessage message = new OSCMessage("/listener1", arguments);
		dispatcher.handlePacket(new OSCPacketEvent(this, message));
		Assert.assertEquals(numArguments, listener1.getReceivedEvent().getMessage().getArguments().size());
	}

	@Test
	public void testListenerRemoval() {
		// change the setup
		dispatcher.removeListener(new OSCPatternAddressMessageSelector("/listener1"), listener1);

		OSCMessage message = new OSCMessage("/listener1");
		dispatcher.handlePacket(new OSCPacketEvent(this, message));
		if (listener1.isMessageReceived()) {
			Assert.fail("Message listener did not get removed");
		}
	}

	@Test
	public void testDispatchMultipleListenersSameSelector() {
		// change the setup
		dispatcher.removeListener(new OSCPatternAddressMessageSelector("/listener2"), listener2);
		dispatcher.addListener(new OSCPatternAddressMessageSelector("/listener1"), listener2);

		OSCMessage message = new OSCMessage("/listener1");
		dispatcher.handlePacket(new OSCPacketEvent(this, message));
		if (!listener1.isMessageReceived()) {
			Assert.fail("Message did not get sent to listener 1");
		}
		if (!listener2.isMessageReceived()) {
			Assert.fail("Message did not get sent to listener 2");
		}
	}

	@Test
	public void testDispatchMultipleTimesSingleListenersSameSelector() {
		// change the setup
		dispatcher.removeListener(new OSCPatternAddressMessageSelector("/listener2"), listener2);
		dispatcher.addListener(new OSCPatternAddressMessageSelector("/listener1"), listener1);

		OSCMessage message = new OSCMessage("/listener1");
		dispatcher.handlePacket(new OSCPacketEvent(this, message));
		if (listener1.getMessageReceivedCount() != 2) {
			Assert.fail("Message did not get sent two times to listener 1, but "
					+ listener1.getMessageReceivedCount() + " times");
		}
	}

	@Test
	public void testDispatchMultipleTimesSingleListenersDifferentSelectors() {
		// change the setup
		dispatcher.removeListener(new OSCPatternAddressMessageSelector("/listener2"), listener2);
		dispatcher.addListener(new OSCPatternAddressMessageSelector("/*"), listener1);

		OSCMessage message = new OSCMessage("/listener1");
		dispatcher.handlePacket(new OSCPacketEvent(this, message));
		if (listener1.getMessageReceivedCount() != 2) {
			Assert.fail("Message did not get sent two times to listener 1, but "
					+ listener1.getMessageReceivedCount() + " times");
		}
	}
}
