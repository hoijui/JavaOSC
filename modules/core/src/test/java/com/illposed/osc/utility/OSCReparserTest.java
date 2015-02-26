/*
 * Copyright (C) 2014, C. Ramakrishnan / Auracle.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.utility;

import com.illposed.osc.argument.OSCTimeStamp;
import com.illposed.osc.argument.OSCImpulse;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPacket;
import com.illposed.osc.argument.OSCUnsigned;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;

/**
 * @see OSCSerializer
 * @see OSCParser
 */
public class OSCReparserTest {

	private static final Comparator<Object> EQUALS_COMPARATOR = new Comparator<Object>() {
				@Override
				public int compare(final Object obj1, final Object obj2) {
					return obj1.equals(obj2) ? 0 : 1;
				}
			};

	private static final Comparator<byte[]> BLOB_COMPARATOR
			= new Comparator<byte[]>() {
				@Override
				public int compare(final byte[] obj1, final byte[] obj2) {
					return Arrays.equals(obj1, obj2) ? 0 : 1;
				}
			};

	private static byte[] serialize(final OSCPacket packet) throws IOException {

		final ByteArrayOutputStream serializedStream = new ByteArrayOutputStream();
		final OSCSerializer serializer = new OSCSerializer(serializedStream);
		serializer.write(packet);
		return serializedStream.toByteArray();
	}

	private static OSCPacket parse(final byte[] packetBytes) throws IOException {

		final OSCParser parser = new OSCParser();
		return parser.convert(packetBytes, packetBytes.length);
	}

	public static <T extends OSCPacket> T reparse(final T packet) throws IOException {

		return (T) parse(serialize(packet));
	}

	private <C, I extends C, O extends C> void reparseSingleArgument(final I argument, final Comparator<C> comparator)
			throws IOException
	{
		final OSCMessage message = new OSCMessage("/hello/world");
		message.addArgument(argument);

		final OSCMessage reparsedMessage = reparse(message);

		final O reparsedArgument = (O) reparsedMessage.getArguments().iterator().next();
		if (comparator.compare(argument, reparsedArgument) != 0) {
			Assert.fail("Failed to reparse argument of type " + argument.getClass()
					+ ". The original was:\n" + argument.toString()
					+ "\nwhile the re-parsed object is:\n" + reparsedArgument.toString());
		}
	}

	private void reparseSingleArgument(final Object argument) throws IOException {
		reparseSingleArgument(argument, EQUALS_COMPARATOR);
	}

	private void reparseSingleBlobArgument(final byte[] blob) throws IOException {
		reparseSingleArgument(blob, BLOB_COMPARATOR);
	}

	@Test
	public void testArgumentBlobEmpty() throws IOException {
		reparseSingleBlobArgument(new byte[] {});
	}

	@Test
	public void testArgumentBlobMin() throws IOException {
		reparseSingleBlobArgument(new byte[] {Byte.MIN_VALUE});
	}

	@Test
	public void testArgumentBlobMinus1() throws IOException {
		reparseSingleBlobArgument(new byte[] {-1});
	}

	@Test
	public void testArgumentBlob0() throws IOException {
		reparseSingleBlobArgument(new byte[] {0});
	}

	@Test
	public void testArgumentBlob1() throws IOException {
		reparseSingleBlobArgument(new byte[] {1});
	}

	@Test
	public void testArgumentBlobMax() throws IOException {
		reparseSingleBlobArgument(new byte[] {Byte.MAX_VALUE});
	}

	@Test
	public void testArgumentBlobTwo() throws IOException {
		reparseSingleBlobArgument(new byte[] {-1, 1});
	}

	@Test
	public void testArgumentBlobThree() throws IOException {
		reparseSingleBlobArgument(new byte[] {-1, 0, 1});
	}

	@Test
	public void testArgumentBlobFour() throws IOException {
		reparseSingleBlobArgument(new byte[] {-2, -1, 1, 2});
	}

	@Test
	public void testArgumentBlobFive() throws IOException {
		reparseSingleBlobArgument(new byte[] {-2, -1, 0, 1, 2});
	}

	@Test
	public void testArgumentChar() throws IOException {

		reparseSingleArgument('0');
		reparseSingleArgument('1');
		reparseSingleArgument('2');
		reparseSingleArgument('3');
		reparseSingleArgument('a');
		reparseSingleArgument('b');
		reparseSingleArgument('c');
		reparseSingleArgument('A');
		reparseSingleArgument('B');
		reparseSingleArgument('C');
		reparseSingleArgument('~');
		reparseSingleArgument('!');
		reparseSingleArgument('@');
		reparseSingleArgument('#');
		reparseSingleArgument('$');
		reparseSingleArgument('%');
		reparseSingleArgument('^');
		reparseSingleArgument('&');
		reparseSingleArgument('*');
		reparseSingleArgument('(');
		reparseSingleArgument(')');
		reparseSingleArgument('_');
		reparseSingleArgument('+');
		// TODO test different charset encodings
	}

	@Test
	public void testArgumentDateImmediate() throws IOException {
		reparseSingleArgument(OSCTimeStamp.valueOf(OSCTimeStamp.IMMEDIATE_DATE));
	}

	@Test
	public void testArgumentDateNow() throws IOException {
		reparseSingleArgument(OSCTimeStamp.valueOf(new Date()));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testArgumentDateLongMin() throws IOException {
		reparseSingleArgument(OSCTimeStamp.valueOf(new Date(Long.MIN_VALUE))); // out of NTP time-stamp range
	}

	@Test(expected=IllegalArgumentException.class)
	public void testArgumentDateMinMinus1() throws IOException {
		reparseSingleArgument(OSCTimeStamp.valueOf(new Date(OSCTimeStamp.OSC_RANGE_DATE_MIN.getTime() - 1))); // out of NTP time-stamp range
	}

	@Test
	public void testArgumentDateMin() throws IOException {
		reparseSingleArgument(OSCTimeStamp.valueOf(new Date(OSCTimeStamp.OSC_RANGE_DATE_MIN.getTime())));
	}

	@Test
	public void testArgumentDateMinPlus1() throws IOException {
		reparseSingleArgument(OSCTimeStamp.valueOf(new Date(OSCTimeStamp.OSC_RANGE_DATE_MIN.getTime() + 1)));
	}

	@Test
	public void testArgumentDateMinPlus2() throws IOException {
		reparseSingleArgument(OSCTimeStamp.valueOf(new Date(OSCTimeStamp.OSC_RANGE_DATE_MIN.getTime() + 2)));
	}

	@Test
	public void testArgumentDateMinPlus1000000() throws IOException {
		reparseSingleArgument(OSCTimeStamp.valueOf(new Date(OSCTimeStamp.OSC_RANGE_DATE_MIN.getTime() + 1000000)));
	}

	@Test
	public void testArgumentDateLongMinus1() throws IOException {
		reparseSingleArgument(OSCTimeStamp.valueOf(new Date(-1L)));
	}

	@Test
	public void testArgumentDateLong0() throws IOException {
		reparseSingleArgument(OSCTimeStamp.valueOf(new Date(0L)));
	}

	@Test
	public void testArgumentDateLong1() throws IOException {
		reparseSingleArgument(OSCTimeStamp.valueOf(new Date(1L)));
	}

	@Test
	public void testArgumentDateMaxMinus1000000() throws IOException {
		reparseSingleArgument(OSCTimeStamp.valueOf(new Date(OSCTimeStamp.OSC_RANGE_DATE_MAX.getTime() - 1000000)));
	}

	@Test
	public void testArgumentDateMaxMinus1() throws IOException {
		reparseSingleArgument(OSCTimeStamp.valueOf(new Date(OSCTimeStamp.OSC_RANGE_DATE_MAX.getTime() - 1)));
	}

	@Test
	public void testArgumentDateMax() throws IOException {
		reparseSingleArgument(OSCTimeStamp.valueOf(new Date(OSCTimeStamp.OSC_RANGE_DATE_MAX.getTime())));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testArgumentDateMaxPlus1() throws IOException {
		reparseSingleArgument(OSCTimeStamp.valueOf(new Date(OSCTimeStamp.OSC_RANGE_DATE_MAX.getTime() + 1))); // out of NTP time-stamp range
	}

	@Test(expected=IllegalArgumentException.class)
	public void testArgumentDateLongMax() throws IOException {
		reparseSingleArgument(OSCTimeStamp.valueOf(new Date(Long.MAX_VALUE))); // out of NTP time-stamp range
	}

	@Test
	public void testArgumentDoubleMin() throws IOException {
		reparseSingleArgument(Double.MIN_VALUE);
	}

	@Test
	public void testArgumentDoubleMinus1() throws IOException {
		reparseSingleArgument(-1.0);
	}

	@Test
	public void testArgumentDouble0() throws IOException {
		reparseSingleArgument(0.0);
	}

	@Test
	public void testArgumentDouble1() throws IOException {
		reparseSingleArgument(1.0);
	}

	@Test
	public void testArgumentDoubleMax() throws IOException {
		reparseSingleArgument(Double.MAX_VALUE);
	}

	@Test
	public void testArgumentDoubleMinNormal() throws IOException {
		reparseSingleArgument(Double.MIN_NORMAL);
	}

	@Test
	public void testArgumentDoubleNegativeInfinity() throws IOException {
		reparseSingleArgument(Double.NEGATIVE_INFINITY);
	}

	@Test
	public void testArgumentDoubleNan() throws IOException {
		reparseSingleArgument(Double.NaN);
	}

	@Test
	public void testArgumentDoublePositiveInfinity() throws IOException {
		reparseSingleArgument(Double.POSITIVE_INFINITY);
	}

	@Test
	public void testArgumentFloatMin() throws IOException {
		reparseSingleArgument(Float.MIN_VALUE);
	}

	@Test
	public void testArgumentFloatMinus1() throws IOException {
		reparseSingleArgument(-1.0f);
	}

	@Test
	public void testArgumentFloat0() throws IOException {
		reparseSingleArgument(0.0f);
	}

	@Test
	public void testArgumentFloat1() throws IOException {
		reparseSingleArgument(1.0f);
	}

	@Test
	public void testArgumentFloatMax() throws IOException {
		reparseSingleArgument(Float.MAX_VALUE);
	}

	@Test
	public void testArgumentFloatMinNormal() throws IOException {
		reparseSingleArgument(Float.MIN_NORMAL);
	}

	@Test
	public void testArgumentFloatNegativeInfinity() throws IOException {
		reparseSingleArgument(Float.NEGATIVE_INFINITY);
	}

	@Test
	public void testArgumentFloatNan() throws IOException {
		reparseSingleArgument(Float.NaN);
	}

	@Test
	public void testArgumentFloatPositiveInfinity() throws IOException {
		reparseSingleArgument(Float.POSITIVE_INFINITY);
	}

	@Test
	public void testArgumentIntegerMin() throws IOException {
		reparseSingleArgument(Integer.MIN_VALUE);
	}

	@Test
	public void testArgumentIntegerMinus1() throws IOException {
		reparseSingleArgument(-1);
	}

	@Test
	public void testArgumentInteger0() throws IOException {
		reparseSingleArgument(0);
	}

	@Test
	public void testArgumentInteger1() throws IOException {
		reparseSingleArgument(1);
	}

	@Test
	public void testArgumentIntegerMax() throws IOException {
		reparseSingleArgument(Integer.MAX_VALUE);
	}

	/**
	 * @see OSCParserTest#testReadUnsignedInteger0()
	 * @throws IOException if something went wrong while serializing or (re-)parsing
	 */
	@Test
	public void testArgumentUnsignedInteger0() throws IOException {
		reparseSingleArgument(OSCUnsigned.valueOf(0x0L));
	}

	@Test
	public void testArgumentUnsignedInteger1() throws IOException {
		reparseSingleArgument(OSCUnsigned.valueOf(0x1L));
	}

	@Test
	public void testArgumentUnsignedIntegerF() throws IOException {
		reparseSingleArgument(OSCUnsigned.valueOf(0xFL));
	}

	@Test
	public void testArgumentUnsignedIntegerFF() throws IOException {
		reparseSingleArgument(OSCUnsigned.valueOf(0xFFL));
	}

	@Test
	public void testArgumentUnsignedIntegerFFF() throws IOException {
		reparseSingleArgument(OSCUnsigned.valueOf(0xFFFL));
	}

	@Test
	public void testArgumentUnsignedIntegerFFFF() throws IOException {
		reparseSingleArgument(OSCUnsigned.valueOf(0xFFFFL));
	}

	@Test
	public void testArgumentUnsignedIntegerFFFFF() throws IOException {
		reparseSingleArgument(OSCUnsigned.valueOf(0xFFFFFL));
	}

	@Test
	public void testArgumentUnsignedIntegerFFFFFF() throws IOException {
		reparseSingleArgument(OSCUnsigned.valueOf(0xFFFFFFL));
	}

	@Test
	public void testArgumentUnsignedIntegerFFFFFFF() throws IOException {
		reparseSingleArgument(OSCUnsigned.valueOf(0xFFFFFFFL));
	}

	@Test
	public void testArgumentUnsignedIntegerFFFFFFFF() throws IOException {
		reparseSingleArgument(OSCUnsigned.valueOf(0xFFFFFFFFL));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testArgumentUnsignedInteger100000000() throws IOException {
		reparseSingleArgument(OSCUnsigned.valueOf(0x100000000L)); // 33bit -> out of range!
	}

	@Test(expected=IllegalArgumentException.class)
	public void testArgumentUnsignedInteger1FFFFFFFF() throws IOException {
		reparseSingleArgument(OSCUnsigned.valueOf(0x1FFFFFFFFL)); // 33bit -> out of range!
	}

	@Test(expected=IllegalArgumentException.class)
	public void testArgumentUnsignedIntegerFFFFFFFFF() throws IOException {
		reparseSingleArgument(OSCUnsigned.valueOf(0xFFFFFFFFFL)); // 36bit -> out of range!
	}

	@Test(expected=IllegalArgumentException.class)
	public void testArgumentUnsignedIntegerMinus1() throws IOException {
		reparseSingleArgument(OSCUnsigned.valueOf(-1L)); // negative/64bit -> out of range!
	}

	@Test(expected=IllegalArgumentException.class)
	public void testArgumentUnsignedIntegerMinLong() throws IOException {
		reparseSingleArgument(OSCUnsigned.valueOf(Long.MIN_VALUE)); // negative -> out of range!
	}

	@Test(expected=IllegalArgumentException.class)
	public void testArgumentUnsignedIntegerMaxLong() throws IOException {
		reparseSingleArgument(OSCUnsigned.valueOf(Long.MAX_VALUE)); // 64bit -> out of range!
	}

	@Test
	public void testArgumentLongMin() throws IOException {
		reparseSingleArgument(Long.MIN_VALUE);
	}

	@Test
	public void testArgumentLongMinus1() throws IOException {
		reparseSingleArgument(-1L);
	}

	@Test
	public void testArgumentLong0() throws IOException {
		reparseSingleArgument(0L);
	}

	@Test
	public void testArgumentLong1() throws IOException {
		reparseSingleArgument(1L);
	}

	@Test
	public void testArgumentLongMax() throws IOException {
		reparseSingleArgument(Long.MAX_VALUE);
	}

	@Test
	public void testArgumentString() throws IOException {

		// test the empty string
		reparseSingleArgument("");
		// test different "character types"
		reparseSingleArgument("hello");
		reparseSingleArgument("HELLO");
		reparseSingleArgument("12345");
		reparseSingleArgument("!@#$%");
		// test different lengths
		reparseSingleArgument("");
		reparseSingleArgument("1");
		reparseSingleArgument("12");
		reparseSingleArgument("123");
		reparseSingleArgument("1234");
		reparseSingleArgument("12345");
		// TODO test different charset encodings
	}

	@Test
	public void testArgumentNull() throws IOException {

		final Comparator<byte[]> nullComparator
				= new Comparator<byte[]>() {
					@Override
					public int compare(final byte[] obj1, final byte[] obj2) {
						return ((obj1 == null) && (obj2 == null)) ? 0 : 1;
					}
				};

		reparseSingleArgument(null, nullComparator);
	}

	@Test
	public void testArgumentBooleanTrue() throws IOException {

		reparseSingleArgument(Boolean.TRUE);
		reparseSingleArgument(true); // uses auto-boxing
	}

	@Test
	public void testArgumentBooleanFalse() throws IOException {

		reparseSingleArgument(Boolean.FALSE);
		reparseSingleArgument(false); // uses auto-boxing
	}

	@Test
	public void testArgumentImpulse() throws IOException {

		reparseSingleArgument(OSCImpulse.INSTANCE);
	}
}
