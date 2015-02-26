/*
 * Copyright (C) 2015, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.argument.handler;

import com.illposed.osc.argument.ArgumentHandler;
import com.illposed.osc.OSCParseException;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.SizeTrackingOutputStream;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;

public class ColorArgumentHandlerTest {

	private static final Color[] DEFAULT_COLORS = new Color[] {
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

	public static <T> T reparse(final ArgumentHandler<T> type, final int bufferSize, final T orig)
			throws OSCSerializeException, OSCParseException
	{
		// serialize
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream(bufferSize);
		final SizeTrackingOutputStream trackedBuffer = new SizeTrackingOutputStream(buffer);
		type.serialize(trackedBuffer, orig);
		final ByteBuffer reparsableBuffer
				= ByteBuffer.wrap(buffer.toByteArray()).asReadOnlyBuffer();

		// re-parse
		final T reparsed = type.parse(reparsableBuffer);

		return reparsed;
	}

	private static Color reparse(final Color orig) throws OSCSerializeException, OSCParseException {
		return reparse(ColorArgumentHandler.INSTANCE, 4, orig);
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

			final int createdInt = ColorArgumentHandler.toUnsignedInt(origByte);
			Assert.assertEquals(origInt, createdInt);

			final byte createdByte = ColorArgumentHandler.toSignedByte(createdInt);
			Assert.assertEquals(origByte, createdByte);
		}
	}

	@Test
	public void testReparseDefaultColors() throws OSCSerializeException, OSCParseException {

		for (final Color orig : DEFAULT_COLORS) {
			Assert.assertEquals(orig, reparse(orig));
		}
	}

	/**
	 * Adds random alpha values between 0 and 255 to the default colors,
	 * and then tries to re-parse them.
	 * @throws OSCSerializeException on re-parse failure
	 * @throws OSCParseException on re-parse failure
	 */
	@Test
	public void testReparseDefaultColorsAlphaed() throws OSCSerializeException, OSCParseException {

		final long alphaRandomSeed = new Random().nextLong();
		System.out.println(ColorArgumentHandlerTest.class.getSimpleName()
				+ "#testReparseDefaultColorsAlphaed:alphaRandomSeed: " + alphaRandomSeed);
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
