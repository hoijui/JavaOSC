// SPDX-FileCopyrightText: 2015 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

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

import java.nio.ByteBuffer;
import java.util.Random;

public class ColorArgumentHandlerTest {

	private final Logger log = LoggerFactory.getLogger(ColorArgumentHandlerTest.class);
	private static final OSCColor[] DEFAULT_COLORS = {
		OSCColor.BLACK,
		OSCColor.BLUE,
		OSCColor.CYAN,
		OSCColor.DARK_GRAY,
		OSCColor.GRAY,
		OSCColor.GREEN,
		OSCColor.LIGHT_GRAY,
		OSCColor.MAGENTA,
		OSCColor.ORANGE,
		OSCColor.PINK,
		OSCColor.RED,
		OSCColor.WHITE,
		OSCColor.YELLOW,
	};

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

		for (final OSCColor origColor: DEFAULT_COLORS) {
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
		for (final OSCColor orig: DEFAULT_COLORS) {
			final int alpha = alphaRandom.nextInt(256);
			final OSCColor alphaed = new OSCColor(orig.getRed(), orig.getGreen(), orig.getBlue(), alpha);
			Assert.assertEquals(alphaed, reparse(alphaed));
		}
	}
}
