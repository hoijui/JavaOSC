/*
 * Copyright (C) 2014, C. Ramakrishnan / Auracle.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.utility;

import java.util.Arrays;
import java.util.List;

/**
 * Checks whether an OSC message address matches a given wildcard expression,
 * as specified in the OSC protocol specification.
 * For details, see "OSC Message Dispatching and Pattern Matching"
 * on {@url http://opensoundcontrol.org/spec-1_0}.
 *
 * <p>
 * A coarse history of the code in the function
 * {@link OSCPatternAddressSelector#match(String, String)},
 * from the origin to JavaOSC:
 * <ol>
 * <li>
 *   <b>robust glob pattern matcher (C)</b>
 *   <br>ozan s. yigit/dec 1994</br>
 *   <br>matching code license: public domain</br>
 *   <br><a href="http://www.cse.yorku.ca/~oz/glob.bun">source location</a></br>
 * </li>
 * <li>
 *   <b>Open SoundControl kit (C++)</b>
 *   <br>matching code license: public domain</br>
 *   <br>library license: LGPL 2.1+</br>
 *   <br><a href="http://archive.cnmat.berkeley.edu/OpenSoundControl/src/OSC-Kit/OSC-pattern-match.c">source location</a></br>
 * </li>
 * <li>
 *   <b>LibLO (C++)</b>
 *   <br>library license: LGPL 2.1+</br>
 *   <br><a href="https://sourceforge.net/p/liblo/git/ci/master/tree/src/pattern_match.c">source location</a></br>
 * </li>
 * <li>
 *   <b>JavaOSC (Java)</b>
 *   <br>matching code license: public domain</br>
 *   <br>library license: BSD 3-Clause</br>
 * </li>
 * </ol>
 * </p>
 */
public class OSCPatternAddressSelector implements AddressSelector {

	private final List<String> patternParts;

	public OSCPatternAddressSelector(String selector) {
		this.patternParts = Arrays.asList(selector.split("/"));
	}

	@Override
	public boolean matches(String messageAddress) {

		List<String> messageAdressParts
				= Arrays.asList(messageAddress.split("/"));
		return matches(patternParts, messageAdressParts);
	}

	/**
	 * Tries to match an OSC <i>Address Pattern</i> to a selector,
	 * both already divided into their parts.
	 * @param patternParts
	 * @param messageAddressParts
	 * @return true if the address matches, false otherwise
	 */
	private static boolean matches(List<String> patternParts, List<String> messageAddressParts) {

		if (patternParts.size() != messageAddressParts.size()) {
			return false;
		}

		for (int pi = 0; pi < patternParts.size(); pi++) {
			if (!matches(messageAddressParts.get(pi), patternParts.get(pi))) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Tries to match an OSC <i>Address Pattern</i> part to a part of
	 * a selector.
	 * This code was copied and adapted from LibLo,
	 * and is licensed under the Public Domain.
	 * For more details see: {@link OSCPatternAddressSelector}.
	 * @param str address part
	 * @param p pattern part
	 * @return true if the address part matches, false otherwise
	 */
	private static boolean matches(String str, String p) {

		boolean negate;
		boolean match;
		char c;

		int si = 0;
		int pi = 0;
		while (pi < p.length()) {
			if ((si == str.length()) && p.charAt(pi) != '*') {
				return false;
			}

			c = p.charAt(pi++);
			switch (c) {
				case '*':
					while ((pi < p.length()) && p.charAt(pi) == '*' && p.charAt(pi) != '/') {
						pi++;
					}

					if (pi == p.length()) {
						return true;
					}

//					if (p.charAt(pi) != '?' && p.charAt(pi) != '[' && p.charAt(pi) != '\\')
					if (p.charAt(pi) != '?' && p.charAt(pi) != '[' && p.charAt(pi) != '{') {
						while (si < str.length() && p.charAt(pi) != str.charAt(si)) {
							si++;
						}
					}

					while (si < str.length()) {
						if (matches(str.substring(si), p.substring(pi))) {
							return true;
						}
						si++;
					}
					return false;

				case '?':
					if (si < str.length()) {
						break;
					}
					return false;

				/*
				 * set specification is inclusive, that is [a-z] is a, z and
				 * everything in between. this means [z-a] may be interpreted
				 * as a set that contains z, a and nothing in between.
				 */
				case '[':
					if (p.charAt(pi) == '!') {
						negate = true;
						pi++;
					} else {
						negate = false;
					}

					match = false;

					while (!match && (pi < p.length())) {
						c = p.charAt(pi++);
						if (pi == p.length()) {
							return false;
						}
						if (p.charAt(pi) == '-') { // c-c
							pi++;
							if (pi == p.length()) {
								return false;
							}
							if (p.charAt(pi) != ']') {
								if (str.charAt(si) == c || str.charAt(si) == p.charAt(pi)
										|| (str.charAt(si) > c && str.charAt(si) < p.charAt(pi)))
								{
									match = true;
								}
							} else { // c-]
								if (str.charAt(si) >= c) {
									match = true;
								}
								break;
							}
						} else { // cc or c]
							if (c == str.charAt(si)) {
								match = true;
							}
							if (p.charAt(pi) != ']') {
								if (p.charAt(pi) == str.charAt(si)) {
									match = true;
								}
							} else {
								break;
							}
						}
					}

					if (negate == match) {
						return false;
					}
					// if there is a match, skip past the cset and continue on
					while (pi < p.length() && p.charAt(pi) != ']') {
						pi++;
					}
					if (pi++ == p.length()) { // oops!
						return false;
					}
					break;

				// {astring,bstring,cstring}
				case '{':
					// p.charAt(pi) is now first character in the {brace list}
					int place = si; // to backtrack
					int remainder = pi; // to forwardtrack

					// find the end of the brace list
					while ((remainder < p.length()) && (p.charAt(remainder) != '}')) {
						remainder++;
					}
					if (remainder == p.length()) /* oops! */ {
						return false;
					}
					remainder++;

					c = p.charAt(pi++);
					while (pi <= p.length()) {
						if (c == ',') {
							if (matches(str.substring(si), p.substring(remainder))) {
								return true;
							} else {
								// backtrack on test string
								si = place;
								// continue testing,
								// skip comma
								if (pi++ == p.length()) { // oops
									return false;
								}
							}
						} else if (c == '}') {
							// continue normal pattern matching
							if ((pi == p.length()) && (si == str.length())) {
								return true;
							}
							si--; // str is incremented again below
							break;
						} else if (c == str.charAt(si)) {
							si++;
							if ((si == str.length()) && (remainder < p.length())) {
								return false;
							}
						} else { // skip to next comma
							si = place;
							while ((pi < p.length()) && (p.charAt(pi) != ',') && (p.charAt(pi) != '}')) {
								pi++;
							}
							if (pi < p.length()) {
								if (p.charAt(pi) == ',') {
									pi++;
								} else if (p.charAt(pi) == '}') {
									return false;
								}
							}
						}
						c = p.charAt(pi++);
					}
					break;

				/*
				 * Not part of OSC pattern matching
					case '\\':
						if (p.charAt(pi)) {
							c = p.charAt(pi)++;
						}
				 */
				default:
					if (c != str.charAt(si)) {
						return false;
					}
					break;
			}
			si++;
		}

		return (si == str.length());
	}
}
