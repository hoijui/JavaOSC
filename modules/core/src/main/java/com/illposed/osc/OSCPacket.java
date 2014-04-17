/*
 * Copyright (C) 2003-2006, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import com.illposed.osc.utility.OSCJavaToByteArrayConverter;
import java.nio.charset.Charset;

/**
 * OSCPacket is the abstract superclass for the various
 * kinds of OSC Messages.
 *
 * The actual packets are:
 * <ul>
 * <li>{@link OSCMessage}: simple OSC messages
 * <li>{@link OSCBundle}: OSC messages with timestamps
 *   and/or made up of multiple messages
 * </ul>
 *
 * This implementation is based on
 * <a href="http://www.emergent.de/Goodies/">Markus Gaelli</a> and
 * Iannis Zannos' OSC implementation in Squeak Smalltalk.
 */
public abstract class OSCPacket {

	/** Used to encode message addresses and string parameters. */
	private Charset charset;
	private byte[] byteArray;

	public OSCPacket() {
		this.charset = Charset.defaultCharset();
		this.byteArray = null;
	}

	/**
	 * Returns the character set used by this packet.
	 * @return the character set used to encode message addresses and string
	 *   arguments.
	 */
	public Charset getCharset() {
		return charset;
	}

	/**
	 * Sets the character set used by this packet.
	 * @param charset used to encode message addresses and string arguments.
	 */
	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	/**
	 * Generate a representation of this packet conforming to the
	 * the OSC byte stream specification. Used Internally.
	 */
	private byte[] computeByteArray() {
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		stream.setCharset(charset);
		return computeByteArray(stream);
	}

	/**
	 * Produces a byte array representation of this packet.
	 * @param stream where to write the arguments to
	 * @return the OSC specification conform byte array representation
	 *   of this packet
	 */
	protected abstract byte[] computeByteArray(OSCJavaToByteArrayConverter stream);

	/**
	 * Return the OSC byte stream for this packet.
	 * @return byte[]
	 */
	public byte[] getByteArray() {
		if (byteArray == null) {
			byteArray = computeByteArray();
		}
		return byteArray;
	}

	protected void contentChanged() {
		byteArray = null;
	}

	/**
	 * Run any post construction initialization. (By default, do nothing.)
	 */
	protected void init() {

	}
}
