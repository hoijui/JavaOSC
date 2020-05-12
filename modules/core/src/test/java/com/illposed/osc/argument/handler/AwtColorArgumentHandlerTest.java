/*
 * Copyright (C) 2015, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.illposed.osc.argument.handler;

import com.illposed.osc.argument.ArgumentHandler;
import com.illposed.osc.OSCParseException;
import com.illposed.osc.OSCSerializeException;
import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

import com.illposed.osc.argument.OSCColor;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AwtColorArgumentHandlerTest {

	private final Logger log = LoggerFactory.getLogger(AwtColorArgumentHandlerTest.class);

	public static final Color[] DEFAULT_COLORS = new Color[] {
		Color.BLACK,
		Color.BLUE,
		Color.CYAN,
		Color.DARK_GRAY,
		Color.GRAY,
		Color.GREEN,
		Color.LIGHT_GRAY,
		Color.MAGENTA,
		Color.ORANGE,
		Color.PINK,
		Color.RED,
		Color.WHITE,
		Color.YELLOW};

	private static Color reparse(final Color orig)
			throws OSCSerializeException, OSCParseException
	{
		return ColorArgumentHandlerTest.reparse(
				AwtColorArgumentHandler.INSTANCE, OSCColor.NUM_CONTENT_BYTES, orig);
	}

	@Test
	public void testReparseDefaultColors() throws Exception {

		for (final Color orig : DEFAULT_COLORS) {
			Assert.assertEquals(orig, reparse(orig));
		}
	}

	/**
	 * Adds random alpha values between 0 and 255 to the default colors,
	 * and then tries to re-parse them.
	 * @throws Exception on re-parse failure
	 */
	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testReparseDefaultColorsAlphaed() throws Exception {

		final long alphaRandomSeed = new Random().nextLong();
		log.debug("{}#testReparseDefaultColorsAlphaed:alphaRandomSeed: {}",
				AwtColorArgumentHandlerTest.class.getSimpleName(), alphaRandomSeed);
		final Random alphaRandom = new Random(alphaRandomSeed);
		final Color[] alphaedDefaultColors = Arrays.copyOf(DEFAULT_COLORS, DEFAULT_COLORS.length);
		for (int tci = 0; tci < alphaedDefaultColors.length; tci++) {
			final Color orig = alphaedDefaultColors[tci];
			final int alpha = alphaRandom.nextInt(256);
			final Color alphaed = new Color(orig.getRed(), orig.getGreen(), orig.getBlue(), alpha);
			alphaedDefaultColors[tci] = alphaed;
		}

		for (final Color origColor : alphaedDefaultColors) {
			Assert.assertEquals(origColor, reparse(origColor));
		}
	}
}
