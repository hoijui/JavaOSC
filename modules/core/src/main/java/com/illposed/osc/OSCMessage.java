/*
 * Copyright (C) 2003-2014, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * An simple (non-bundle) OSC message.
 *
 * An OSC <i>Message</i> is made up of
 * an <i>Address Pattern</i> (the receiver of the message)
 * and <i>Arguments</i> (the content of the message).
 */
public class OSCMessage implements OSCPacket {

	/**
	 * Java regular expression pattern matching a single invalid character.
	 * The invalid characters are:
	 * ' ', '#', '*', ',', '?', '[', ']', '{', '}'
	 */
	private static final Pattern ILLEGAL_ADDRESS_CHAR
			= Pattern.compile("[ \\#\\*\\,\\?\\[\\]\\{\\}]");

	private final String address;
	private final List<Object> arguments;

	/**
	 * Creates an OSCMessage with an address already initialized.
	 * @param address  the recipient of this OSC message
	 */
	public OSCMessage(final String address) {
		this(address, Collections.EMPTY_LIST);
	}

	/**
	 * Creates an OSCMessage with an address
	 * and arguments already initialized.
	 * @param address  the recipient of this OSC message
	 * @param arguments  the data sent to the receiver
	 */
	public OSCMessage(final String address, final List<?> arguments) {

		checkAddress(address);
		this.address = address;
		this.arguments = (List<Object>) Collections.unmodifiableList(arguments);
	}

	/**
	 * The receiver of this message.
	 * @return the receiver of this OSC Message
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * The arguments of this message.
	 * @return the arguments to this message
	 */
	public List<Object> getArguments() {
		return arguments;
	}

	/**
	 * Throws an exception if the given address is invalid.
	 * @param address to be checked for validity
	 */
	private static void checkAddress(final String address) {
		if (!isValidAddress(address)) {
			throw new IllegalArgumentException("Not a valid OSC address: " + address);
		}
	}

	/**
	 * Checks whether a given string is a valid OSC <i>Address Pattern</i>.
	 * @param address to be checked for validity
	 * @return true if the supplied string constitutes a valid OSC address
	 */
	public static boolean isValidAddress(final String address) {
		return (address != null)
				&& !address.isEmpty()
				&& address.charAt(0) == '/'
				&& !address.contains("//")
				&& !ILLEGAL_ADDRESS_CHAR.matcher(address).find();
	}
}
