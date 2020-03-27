/*
 * Copyright (C) 2015, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.illposed.osc.argument.handler;

import com.illposed.osc.BufferBytesReceiver;
import com.illposed.osc.OSCParseException;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.argument.ArgumentHandler;
import com.illposed.osc.argument.OSCColor;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

public class ColorArgumentHandlerTest {

	private final Logger log = LoggerFactory.getLogger(ColorArgumentHandlerTest.class);

	static <T> T reparse(final ArgumentHandler<T> type, final int bufferSize, final T orig)
			throws OSCSerializeException, OSCParseException
	{
		// serialize
		final ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
		final BufferBytesReceiver bytesReceiver = new BufferBytesReceiver(buffer);
		type.serialize(bytesReceiver, orig);
		final ByteBuffer reparsableBuffer = (ByteBuffer) buffer.flip();

		// re-parse
		return type.parse(reparsableBuffer);
	}

	private static OSCColor reparse(final OSCColor orig)
			throws OSCSerializeException, OSCParseException
	{
		return reparse(ColorArgumentHandler.INSTANCE, OSCColor.NUM_CONTENT_BYTES, orig);
	}

	@Test
	public void testReconvertBytes() {

		final byte[] testBytes = new byte[] {
			-128,
			-1,
			0,
			1,
			127
		};
		final int[] testInts = new int[] {
			128,
			255,
			0,
			1,
			127
		};

		for (int tni = 0; tni < testBytes.length; tni++) {
			final byte origByte = testBytes[tni];
			final int origInt = testInts[tni];

			final int createdInt = OSCColor.toUnsignedInt(origByte);
			Assert.assertEquals(origInt, createdInt);

			final byte createdByte = OSCColor.toSignedByte(createdInt);
			Assert.assertEquals(origByte, createdByte);
		}
	}

	@Test
	public void testReparseDefaultColors() throws Exception {

		for (final Color origColorAwt : AwtColorArgumentHandlerTest.DEFAULT_COLORS) {
			final OSCColor origColor = AwtColorArgumentHandler.toOsc(origColorAwt);
			Assert.assertEquals(origColor, reparse(origColor));
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
				ColorArgumentHandlerTest.class.getSimpleName(), alphaRandomSeed);
		final Random alphaRandom = new Random(alphaRandomSeed);
		final Color[] alphaedDefaultColors = Arrays.copyOf(
				AwtColorArgumentHandlerTest.DEFAULT_COLORS,
				AwtColorArgumentHandlerTest.DEFAULT_COLORS.length);
		for (int tci = 0; tci < alphaedDefaultColors.length; tci++) {
			final Color orig = alphaedDefaultColors[tci];
			final int alpha = alphaRandom.nextInt(256);
			final Color alphaed = new Color(orig.getRed(), orig.getGreen(), orig.getBlue(), alpha);
			alphaedDefaultColors[tci] = alphaed;
		}

		for (final Color origColorAwt : alphaedDefaultColors) {
			final OSCColor origColor = AwtColorArgumentHandler.toOsc(origColorAwt);
			Assert.assertEquals(origColor, reparse(origColor));
		}
	}
}
