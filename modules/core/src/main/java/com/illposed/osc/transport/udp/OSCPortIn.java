/*
 * Copyright (C) 2004-2019, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.illposed.osc.transport.udp;

import com.illposed.osc.OSCBadDataEvent;
import com.illposed.osc.OSCPacket;
import com.illposed.osc.OSCPacketDispatcher;
import com.illposed.osc.OSCPacketEvent;
import com.illposed.osc.OSCPacketListener;
import com.illposed.osc.OSCParseException;
import com.illposed.osc.OSCSerializerAndParserBuilder;
import com.illposed.osc.transport.channel.OSCDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Listens for OSC packets on a UDP/IP port.
 * <p>
 * An example:<br>
 * <blockquote><pre>{@code
 * // listens on the wildcard address (all local network interfaces)
 * // on the given port (the default one)
 * OSCPortIn receiver = new OSCPortIn(OSCPort.DEFAULT_SC_OSC_PORT);
 * OSCMessageListener listener = new OSCMessageListener() {
 *   public void acceptMessage(OSCMessageEvent event) {
 *     System.out.println("Message received: " + event.getMessage().getAddress());
 *   }
 * };
 * MessageSelector selector = new OSCPatternAddressMessageSelector(
 *     "/message/receiving");
 * receiver.getDispatcher().addListener(selector, listener);
 * // NOTE You might want to use this code, in case you have bundles
 * //      with time-stamps in the future, which you still want
 * //      to process immediately.
 * //receiver.getDispatcher().setAlwaysDispatchingImmediately(true);
 * receiver.startListening();
 * }</pre></blockquote>
 * <p>
 * Then, using a program such as SuperCollider or sendOSC, send a message
 * to this computer, port {@link #DEFAULT_SC_OSC_PORT},
 * with the address "/message/receiving".
 */
public class OSCPortIn extends OSCPort implements Runnable {

  private final Logger log = LoggerFactory.getLogger(OSCPortIn.class);

  // Public API
  /**
   * Buffers were 1500 bytes in size, but were increased to 1536, as this is a common MTU,
   * and then increased to 65507, as this is the maximum incoming datagram data size.
   */
  @SuppressWarnings("WeakerAccess")
  public static final int BUFFER_SIZE = 65507;

  private volatile boolean listening;
  private boolean daemonListener;
  private boolean resilient;
  private Thread listeningThread;
  private final OSCSerializerAndParserBuilder parserBuilder;
  private final List<OSCPacketListener> packetListeners;

  public static class OSCPortInSource {
    private OSCPortIn port;
    private SocketAddress sender;

    OSCPortInSource(OSCPortIn port, SocketAddress sender) {
      this.port = port;
      this.sender = sender;
    }

    public OSCPortIn getPort() {
      return port;
    }

    public SocketAddress getSender() {
      return sender;
    }
  }

  public static OSCPacketDispatcher getDispatcher(
      final List<OSCPacketListener> listeners) {
    return listeners.stream()
        .filter(OSCPacketDispatcher.class::isInstance)
        .findFirst()
        .map(OSCPacketDispatcher.class::cast)
        .orElse(null);
  }

  public static OSCPacketListener defaultPacketListener() {
    final OSCPacketDispatcher dispatcher = new OSCPacketDispatcher();
    // Until version 0.4, we did the following property to <code>true</code>,
    // because this is how it always worked in this library until Feb. 2015.,
    // and thus users of this library expected this behaviour by default.
    // It is against the OSC (1.0) specification though,
    // so since version 0.5 of this library, we set it to <code>false</code>.
    dispatcher.setAlwaysDispatchingImmediately(false);

    return dispatcher;
  }

  public static List<OSCPacketListener> defaultPacketListeners() {
    final List<OSCPacketListener> listeners = new ArrayList<>();
    listeners.add(defaultPacketListener());
    return listeners;
  }

  /**
   * Create an OSC-Port that listens on the given local socket for packets from {@code remote},
   * using a parser created with the given factory,
   * and with {@link #isResilient() resilient} set to true.
   *
   * @param parserBuilder   to create the internal parser from
   * @param packetListeners to handle received and serialized OSC packets
   * @param local           address to listen on
   * @param remote          address to listen to
   * @throws IOException if we fail to bind a channel to the local address
   */
  public OSCPortIn(
      final OSCSerializerAndParserBuilder parserBuilder,
      final List<OSCPacketListener> packetListeners,
      final SocketAddress local,
      final SocketAddress remote)
      throws IOException {
    super(local, remote);

    this.listening = false;
    this.daemonListener = true;
    this.resilient = true;
    this.parserBuilder = parserBuilder;
    this.packetListeners = packetListeners;
  }

  public OSCPortIn(
      final OSCSerializerAndParserBuilder parserBuilder,
      final List<OSCPacketListener> packetListeners,
      final SocketAddress local)
      throws IOException {
    this(
        parserBuilder,
        packetListeners,
        local,
        new InetSocketAddress(OSCPort.generateWildcard(local), 0)
    );
  }

  public OSCPortIn(
      final OSCSerializerAndParserBuilder parserBuilder,
      final SocketAddress local)
      throws IOException {
    this(parserBuilder, defaultPacketListeners(), local);
  }

  public OSCPortIn(final OSCSerializerAndParserBuilder parserBuilder, final int port)
      throws IOException {
    this(parserBuilder, new InetSocketAddress(port));
  }

  /**
   * Creates an OSC-Port that listens on the given local socket.
   *
   * @param local address to listen on
   * @throws IOException if we fail to bind a channel to the local address
   */
  public OSCPortIn(final SocketAddress local) throws IOException {
    this(new OSCSerializerAndParserBuilder(), local);
  }

  /**
   * Creates an OSC-Port that listens on the wildcard address
   * (all local network interfaces) on the specified local port.
   *
   * @param port port number to listen on
   * @throws IOException if we fail to bind a channel to the local address
   */
  public OSCPortIn(final int port) throws IOException {
    this(new InetSocketAddress(port));
  }

  /**
   * Creates an OSC-Port that listens on the wildcard address
   * (all local network interfaces) on the default local port
   * {@link #DEFAULT_SC_OSC_PORT}.
   *
   * @throws IOException if we fail to bind a channel to the local address
   */
  public OSCPortIn() throws IOException {
    this(defaultSCOSCPort());
  }

  /**
   * Run the loop that listens for OSC on a socket until
   * {@link #isListening()} becomes false.
   *
   * @see Runnable#run()
   */
  @Override
  public void run() {

    final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
    final DatagramChannel channel = getChannel();
    final OSCDatagramChannel oscChannel = new OSCDatagramChannel(channel, parserBuilder);
    while (listening) {
      try {
        final OSCDatagramChannel.OSCPacketWithSource packetWithSource = oscChannel.read(buffer);

        final OSCPacketEvent event = new OSCPacketEvent(new OSCPortInSource(this, packetWithSource.getSource()), packetWithSource.getPacket());
        for (final OSCPacketListener listener : packetListeners) {
          listener.handlePacket(event);
        }
      } catch (final IOException ex) {
        if (isListening()) {
          stopListening(ex);
        } else {
          stopListening();
        }
      } catch (final OSCParseException ex) {
        badPacketReceived(ex, buffer);
      }
    }
  }

  private void stopListening(final Exception exception) {

    // TODO This implies a low-level problem (for example network IO), but it could just aswell be a high-level one (for example a parse exception)
    final String errorMsg = "Error while listening on " + toString() + "...";
    log.error(errorMsg);
    if (exception instanceof OSCParseException) {
      log.error(errorMsg);
    } else {
      log.error(errorMsg, exception);
    }
    stopListening();
  }

  private void badPacketReceived(final OSCParseException exception, final ByteBuffer data) {

    final OSCBadDataEvent badDataEvt = new OSCBadDataEvent(this, data, exception);

    for (final OSCPacketListener listener : packetListeners) {
      listener.handleBadData(badDataEvt);
    }

    if (!isResilient()) {
      stopListening(exception);
    }
  }

  // Public API

  /**
   * Start listening for incoming OSCPackets
   */
  @SuppressWarnings("WeakerAccess")
  public void startListening() {

    // NOTE This is not thread-save
    if (!isListening()) {
      listening = true;
      listeningThread = new Thread(this);
      // The JVM exits when the only threads running are all daemon threads.
      listeningThread.setDaemon(daemonListener);
      listeningThread.start();
    }
  }

  // Public API

  /**
   * Stop listening for incoming OSCPackets
   */
  @SuppressWarnings("WeakerAccess")
  public void stopListening() {

    listening = false;
    // NOTE This is not thread-save
    if (getChannel().isBlocking()) {
      try {
        getChannel().close();
      } catch (final IOException ex) {
        log.error("Failed to close OSC UDP channel", ex);
      }
    }
  }

  // Public API

  /**
   * Is this port listening for packets?
   *
   * @return true if this port is in listening mode
   */
  @SuppressWarnings("WeakerAccess")
  public boolean isListening() {
    return listening;
  }

  // Public API

  /**
   * Is this port listening for packets in daemon mode?
   *
   * @return <code>true</code> if this ports listening thread is/would be in daemon mode
   * @see #setDaemonListener
   */
  @SuppressWarnings({"WeakerAccess", "unused"})
  public boolean isDaemonListener() {
    return daemonListener;
  }

  // Public API

  /**
   * Set whether this port should be listening for packets in daemon mode.
   * The Java Virtual Machine exits when the only threads running are all daemon threads.
   * This is <code>true</code> by default.
   * Probably the only feasible reason to set this to <code>false</code>,
   * is if the code in the listener is very small,
   * and the application consists of nothing more then this listening thread.
   *
   * @param daemonListener whether this ports listening thread should be in daemon mode
   * @see Thread#setDaemon(boolean)
   */
  @SuppressWarnings("WeakerAccess")
  public void setDaemonListener(final boolean daemonListener) {

    if (isListening()) {
      listeningThread.setDaemon(daemonListener);
    }
    this.daemonListener = daemonListener;
  }

  // Public API

  /**
   * Whether this port continues listening and throws
   * a {@link OSCParseException} after receiving a bad packet.
   *
   * @return <code>true</code> if this port will continue listening
   * after a parse exception
   */
  @SuppressWarnings("WeakerAccess")
  public boolean isResilient() {
    return resilient;
  }

  // Public API

  /**
   * Set whether this port continues listening and throws
   * a {@link OSCParseException} after receiving a bad packet.
   *
   * @param resilient whether this port should continue listening
   *                  after a parse exception
   */
  @SuppressWarnings("WeakerAccess")
  public void setResilient(final boolean resilient) {
    this.resilient = resilient;
  }

  @Override
  public void close() throws IOException {

    if (isListening()) {
      stopListening();
    }
    super.close();
  }

  @Override
  public String toString() {

    final StringBuilder rep = new StringBuilder(32);

    rep
        .append('[')
        .append(getClass().getSimpleName())
        .append(": ");
    if (isListening()) {
      rep
          .append("listening on \"")
          .append(getLocalAddress().toString())
          .append('\"');
    } else {
      rep.append("stopped");
    }
    rep.append(']');

    return rep.toString();
  }

  // Public API
  @SuppressWarnings("WeakerAccess")
  public OSCPacketDispatcher getDispatcher() {
    final OSCPacketDispatcher dispatcher = getDispatcher(packetListeners);

    if (dispatcher == null) {
      throw new IllegalStateException(
          "OSCPortIn packet listeners do not include a dispatcher.");
    }

    return dispatcher;
  }

  public List<OSCPacketListener> getPacketListeners() {
    return packetListeners;
  }

  /**
   * Adds a listener that will handle all packets received.
   * This includes bundles and individual (non-bundled) messages.
   * Registered listeners will be notified of packets in the order they were
   * added to the dispatcher.
   * A listener can be registered multiple times, and will consequently be
   * notified as many times as it was added.
   *
   * @param listener receives and handles packets
   */
  public void addPacketListener(final OSCPacketListener listener) {
    packetListeners.add(listener);
  }

  /**
   * Removes a packet listener, which will no longer be notified of incoming
   * packets.
   * Removes only the first occurrence of the listener.
   *
   * @param listener will no longer receive packets
   */
  public void removePacketListener(final OSCPacketListener listener) {
    packetListeners.remove(listener);
  }

}
