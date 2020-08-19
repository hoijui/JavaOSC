/*
 * Copyright (C) 2015-2017, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.illposed.osc.transport.channel;

import com.illposed.osc.OSCPacket;
import com.illposed.osc.OSCParseException;
import com.illposed.osc.OSCParser;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.OSCSerializer;
import com.illposed.osc.OSCSerializerAndParserBuilder;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;

/**
 * This class handles just the basic sending and receiving of OSC data
 * over a DatagramChannel.
 * It is mostly for internal use,
 * and will probably not be used directly by users of this library.
 * To send OSC messages, use {@link com.illposed.osc.transport.udp.OSCPortOut}.
 * To listen for OSC messages,
 * use {@link com.illposed.osc.transport.udp.OSCPortIn}.
 */
public class OSCDatagramChannel extends SelectableChannel {

  private final DatagramChannel underlyingChannel;
  private final OSCParser parser;
  private final OSCSerializerAndParserBuilder serializerBuilder;

  public static class OSCPacketWithSource {
    private OSCPacket packet;
    private SocketAddress source;

    public OSCPacketWithSource(OSCPacket packet, SocketAddress source) {
      this.packet = packet;
      this.source = source;
    }

    public OSCPacket getPacket() {
      return packet;
    }

    public SocketAddress getSource() {
      return source;
    }

    @Override
    public boolean equals(Object obj) {
      return ((obj instanceof OSCPacketWithSource))
          && (((OSCPacketWithSource) obj).packet == packet
          && ((OSCPacketWithSource) obj).source == source);
    }
  }

  public OSCDatagramChannel(
      final DatagramChannel underlyingChannel,
      final OSCSerializerAndParserBuilder serializerAndParserBuilder
  ) {
    this.underlyingChannel = underlyingChannel;
    OSCParser tmpParser = null;
    if (serializerAndParserBuilder != null) {
      tmpParser = serializerAndParserBuilder.buildParser();
    }
    this.parser = tmpParser;
    this.serializerBuilder = serializerAndParserBuilder;
  }

  public OSCPacketWithSource read(final ByteBuffer buffer) throws IOException, OSCParseException {
    boolean completed = false;
    OSCPacket oscPacket;
    SocketAddress peer;
    try {
      begin();

			buffer.clear();
			// NOTE From the doc of `read()` and `receive()`:
			// "If there are fewer bytes remaining in the buffer
			// than are required to hold the datagram
			// then the remainder of the datagram is silently discarded."
			if (underlyingChannel.isConnected()) {
			  peer = underlyingChannel.getRemoteAddress();
				underlyingChannel.read(buffer);
			} else {
				peer = underlyingChannel.receive(buffer);
			}
//			final int readBytes = buffer.position();
//			if (readBytes == buffer.capacity()) {
//				// TODO In this case it is very likely that the buffer was actually too small, and the remainder of the datagram/packet was silently discarded. We might want to give a warning, like throw an exception in this case, but whether this happens should probably be user configurable.
//			}
      buffer.flip();
      if (buffer.limit() == 0) {
        throw new OSCParseException("Received a packet without any data");
      } else {
        oscPacket = parser.convert(buffer);
        completed = true;
      }
    } finally {
      end(completed);
    }

    return new OSCPacketWithSource(oscPacket, peer);
  }


  public void send(final ByteBuffer buffer, final OSCPacket packet, final SocketAddress remoteAddress) throws IOException, OSCSerializeException {

    boolean completed = false;
    try {
      begin();

      final OSCSerializer serializer = serializerBuilder.buildSerializer(buffer);
      buffer.rewind();
      serializer.write(packet);
      buffer.flip();
      if (underlyingChannel.isConnected()) {
        underlyingChannel.write(buffer);
      } else if (remoteAddress == null) {
        throw new IllegalStateException("Not connected and no remote address is given");
      } else {
        underlyingChannel.send(buffer, remoteAddress);
      }
      completed = true;
    } finally {
      end(completed);
    }
  }

  public void write(final ByteBuffer buffer, final OSCPacket packet) throws IOException, OSCSerializeException {

    boolean completed = false;
    try {
      begin();
      if (!underlyingChannel.isConnected()) {
        throw new IllegalStateException("Either connect the channel or use write()");
      }
      send(buffer, packet, null);
      completed = true;
    } finally {
      end(completed);
    }
  }

  @Override
  public SelectorProvider provider() {
    return underlyingChannel.provider();
  }

  @Override
  public boolean isRegistered() {
    return underlyingChannel.isRegistered();
  }

  @Override
  public SelectionKey keyFor(final Selector sel) {
    return underlyingChannel.keyFor(sel);
  }

  @Override
  public SelectionKey register(final Selector sel, final int ops, final Object att) throws ClosedChannelException {
    return underlyingChannel.register(sel, ops, att);
  }

  @Override
  public SelectableChannel configureBlocking(final boolean block) throws IOException {
    return underlyingChannel.configureBlocking(block);
  }

  @Override
  public boolean isBlocking() {
    return underlyingChannel.isBlocking();
  }

  @Override
  public Object blockingLock() {
    return underlyingChannel.blockingLock();
  }

  @Override
  protected void implCloseChannel() throws IOException {
    // XXX is this ok?
    underlyingChannel.close();
  }

  @Override
  public int validOps() {
    return underlyingChannel.validOps();
  }
}
