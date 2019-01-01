package com.illposed.osc;

import com.illposed.osc.argument.OSCTimeTag64;

public class SimpleOSCPacketListener implements OSCPacketListener {

	private int packetReceivedCount;
	private OSCPacket packet;

	public SimpleOSCPacketListener() {

		this.packetReceivedCount = 0;
		this.packet = null;
	}
	public boolean isMessageReceived() {
		return (packetReceivedCount > 0);
	}

	@SuppressWarnings("WeakerAccess")
	public int getMessageReceivedCount() {
		return packetReceivedCount;
	}

	public OSCPacket getReceivedPacket() {
		return packet;
	}

	@Override
	public void handlePacket(Object source, final OSCPacket packet) {
		packetReceivedCount++;
		this.packet = packet;
	}

	@Override
	public void handleBadData(final OSCBadDataEvent event) {}
}
