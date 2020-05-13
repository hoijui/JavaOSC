/*
 * Copyright (C) 2014, C. Ramakrishnan / Auracle.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.illposed.osc;

import com.illposed.osc.argument.OSCTimeTag64;
import com.illposed.osc.argument.OSCImpulse;
import com.illposed.osc.argument.OSCUnsigned;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
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

	private static final Comparator<ByteBuffer> BLOB_COMPARATOR
			= new Comparator<ByteBuffer>() {
				@Override
				public int compare(final ByteBuffer obj1, final ByteBuffer obj2) {
					obj1.flip(); // HACK
					return obj1.compareTo(obj2);
				}
			};

	private static BufferBytesReceiver serialize(final OSCPacket packet)
			throws OSCSerializeException
	{
		final ByteBuffer serialized = ByteBuffer.allocate(1024);
		final BufferBytesReceiver bytesReceiver = new BufferBytesReceiver(serialized);
		final OSCSerializer serializer
				= new OSCSerializerAndParserBuilder().buildSerializer(bytesReceiver);
		serializer.write(packet);
		return bytesReceiver;
	}

	private static OSCPacket parse(final ByteBuffer packetBytes)
			throws OSCParseException
	{
		final OSCParser parser = new OSCSerializerAndParserBuilder().buildParser();
		return parser.convert(packetBytes);
	}

	static <T extends OSCPacket> T reparse(final T packet)
			throws OSCParseException, OSCSerializeException
	{
		final ByteBuffer serialized = serialize(packet).getBuffer();
		serialized.flip();
		return (T) parse(serialized);
	}

	private <C, I extends C, O extends C> void reparseSingleArgument(
			final I argument,
			final Comparator<C> comparator)
			throws OSCParseException, OSCSerializeException
	{
		final OSCMessage message = new OSCMessage("/hello/world",
				Collections.singletonList(argument));

		final OSCMessage reparsedMessage = reparse(message);

		final O reparsedArgument = (O) reparsedMessage.getArguments().iterator().next();
		if (comparator.compare(argument, reparsedArgument) != 0) {
			Assert.fail("Failed to reparse argument of type " + argument.getClass()
					+ ". The original was:\n" + argument.toString()
					+ "\nwhile the re-parsed object is:\n" + reparsedArgument.toString());
		}
	}

	private void reparseSingleArgument(final Object argument)
			throws OSCParseException, OSCSerializeException
	{
		reparseSingleArgument(argument, EQUALS_COMPARATOR);
	}

	private void reparseSingleBlobArgument(final byte[] blob)
			throws OSCParseException, OSCSerializeException
	{
		reparseSingleArgument(ByteBuffer.wrap(blob), BLOB_COMPARATOR);
	}

	@Test
	public void testArgumentBlobEmpty() throws Exception {
		reparseSingleBlobArgument(new byte[] {});
	}

	@Test
	public void testArgumentBlobMin() throws Exception {
		reparseSingleBlobArgument(new byte[] {Byte.MIN_VALUE});
	}

	@Test
	public void testArgumentBlobMinus1() throws Exception {
		reparseSingleBlobArgument(new byte[] {-1});
	}

	@Test
	public void testArgumentBlob0() throws Exception {
		reparseSingleBlobArgument(new byte[] {0});
	}

	@Test
	public void testArgumentBlob1() throws Exception {
		reparseSingleBlobArgument(new byte[] {1});
	}

	@Test
	public void testArgumentBlobMax() throws Exception {
		reparseSingleBlobArgument(new byte[] {Byte.MAX_VALUE});
	}

	@Test
	public void testArgumentBlobTwo() throws Exception {
		reparseSingleBlobArgument(new byte[] {-1, 1});
	}

	@Test
	public void testArgumentBlobThree() throws Exception {
		reparseSingleBlobArgument(new byte[] {-1, 0, 1});
	}

	@Test
	public void testArgumentBlobFour() throws Exception {
		reparseSingleBlobArgument(new byte[] {-2, -1, 1, 2});
	}

	@Test
	public void testArgumentBlobFive() throws Exception {
		reparseSingleBlobArgument(new byte[] {-2, -1, 0, 1, 2});
	}

	@Test
	public void testArgumentChar() throws Exception {

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
	public void testArgumentDateImmediate() throws Exception {
		reparseSingleArgument(OSCTimeTag64.valueOf(OSCTimeTag64.immediateDate()));
	}

	@Test
	public void testArgumentDateNow() throws Exception {
		reparseSingleArgument(OSCTimeTag64.valueOf(new Date()));
	}

	@Test
	public void testArgumentDateLongMin() throws Exception {
		// this tests a negative epoch
		reparseSingleArgument(OSCTimeTag64.valueOf(new Date(Long.MIN_VALUE)));
	}

	@Test
	public void testArgumentDateEpochMinMinus1() throws Exception {
		// this tests epoch -1
		reparseSingleArgument(OSCTimeTag64.valueOf(new Date(OSCTimeTag64.EPOCH_START_JAVA_TIME_0 - 1))); // out of NTP time-stamp range
	}

	@Test
	public void testArgumentDateEpochMin() throws Exception {
		reparseSingleArgument(OSCTimeTag64.valueOf(new Date(OSCTimeTag64.EPOCH_START_JAVA_TIME_0)));
	}

	@Test
	public void testArgumentDateEpochMinPlus1() throws Exception {
		reparseSingleArgument(OSCTimeTag64.valueOf(new Date(OSCTimeTag64.EPOCH_START_JAVA_TIME_0 + 1)));
	}

	@Test
	public void testArgumentDateEpochMinPlus2() throws Exception {
		reparseSingleArgument(OSCTimeTag64.valueOf(new Date(OSCTimeTag64.EPOCH_START_JAVA_TIME_0 + 2)));
	}

	@Test
	public void testArgumentDateEpochMinPlus1000000() throws Exception {
		reparseSingleArgument(OSCTimeTag64.valueOf(new Date(OSCTimeTag64.EPOCH_START_JAVA_TIME_0 + 1000000)));
	}

	@Test
	public void testArgumentDateLongMinus1() throws Exception {
		reparseSingleArgument(OSCTimeTag64.valueOf(new Date(-1L)));
	}

	@Test
	public void testArgumentDateLong0() throws Exception {
		reparseSingleArgument(OSCTimeTag64.valueOf(new Date(0L)));
	}

	@Test
	public void testArgumentDateLong1() throws Exception {
		reparseSingleArgument(OSCTimeTag64.valueOf(new Date(1L)));
	}

	@Test
	public void testArgumentDateEpochMaxMinus1000000() throws Exception {
		reparseSingleArgument(OSCTimeTag64.valueOf(new Date(OSCTimeTag64.EPOCH_START_JAVA_TIME_0 - 1000000)));
	}

	@Test
	public void testArgumentDateEpochMaxMinus1() throws Exception {
		reparseSingleArgument(OSCTimeTag64.valueOf(new Date(OSCTimeTag64.EPOCH_START_JAVA_TIME_0 - 1)));
	}

	@Test
	public void testArgumentDateEpochMax() throws Exception {
		reparseSingleArgument(OSCTimeTag64.valueOf(new Date(OSCTimeTag64.EPOCH_START_JAVA_TIME_0)));
	}

	@Test
	public void testArgumentDateEpochMaxPlus1() throws Exception {
		// this tests epoch 1
		reparseSingleArgument(OSCTimeTag64.valueOf(new Date(OSCTimeTag64.EPOCH_START_JAVA_TIME_0 + 1)));
	}

	@Test
	public void testArgumentDateLongMax() throws Exception {
		// this tests a positive epoch
		reparseSingleArgument(OSCTimeTag64.valueOf(new Date(Long.MAX_VALUE)));
	}

	@Test
	public void testArgumentDoubleMin() throws Exception {
		reparseSingleArgument(Double.MIN_VALUE);
	}

	@Test
	public void testArgumentDoubleMinus1() throws Exception {
		reparseSingleArgument(-1.0);
	}

	@Test
	public void testArgumentDouble0() throws Exception {
		reparseSingleArgument(0.0);
	}

	@Test
	public void testArgumentDouble1() throws Exception {
		reparseSingleArgument(1.0);
	}

	@Test
	public void testArgumentDoubleMax() throws Exception {
		reparseSingleArgument(Double.MAX_VALUE);
	}

	@Test
	public void testArgumentDoubleMinNormal() throws Exception {
		reparseSingleArgument(Double.MIN_NORMAL);
	}

	@Test
	public void testArgumentDoubleNegativeInfinity() throws Exception {
		reparseSingleArgument(Double.NEGATIVE_INFINITY);
	}

	@Test
	public void testArgumentDoubleNan() throws Exception {
		reparseSingleArgument(Double.NaN);
	}

	@Test
	public void testArgumentDoublePositiveInfinity() throws Exception {
		reparseSingleArgument(Double.POSITIVE_INFINITY);
	}

	@Test
	public void testArgumentFloatMin() throws Exception {
		reparseSingleArgument(Float.MIN_VALUE);
	}

	@Test
	public void testArgumentFloatMinus1() throws Exception {
		reparseSingleArgument(-1.0f);
	}

	@Test
	public void testArgumentFloat0() throws Exception {
		reparseSingleArgument(0.0f);
	}

	@Test
	public void testArgumentFloat1() throws Exception {
		reparseSingleArgument(1.0f);
	}

	@Test
	public void testArgumentFloatMax() throws Exception {
		reparseSingleArgument(Float.MAX_VALUE);
	}

	@Test
	public void testArgumentFloatMinNormal() throws Exception {
		reparseSingleArgument(Float.MIN_NORMAL);
	}

	@Test
	public void testArgumentFloatNegativeInfinity() throws Exception {
		reparseSingleArgument(Float.NEGATIVE_INFINITY);
	}

	@Test
	public void testArgumentFloatNan() throws Exception {
		reparseSingleArgument(Float.NaN);
	}

	@Test
	public void testArgumentFloatPositiveInfinity() throws Exception {
		reparseSingleArgument(Float.POSITIVE_INFINITY);
	}

	@Test
	public void testArgumentIntegerMin() throws Exception {
		reparseSingleArgument(Integer.MIN_VALUE);
	}

	@Test
	public void testArgumentIntegerMinus1() throws Exception {
		reparseSingleArgument(-1);
	}

	@Test
	public void testArgumentInteger0() throws Exception {
		reparseSingleArgument(0);
	}

	@Test
	public void testArgumentInteger1() throws Exception {
		reparseSingleArgument(1);
	}

	@Test
	public void testArgumentIntegerMax() throws Exception {
		reparseSingleArgument(Integer.MAX_VALUE);
	}

	/**
	 * @see OSCParserTest#testReadUnsignedInteger0()
	 * @throws IOException if something went wrong while serializing or (re-)parsing
	 */
	@Test
	public void testArgumentUnsignedInteger0() throws Exception {
		reparseSingleArgument(OSCUnsigned.valueOf(0x0L));
	}

	@Test
	public void testArgumentUnsignedInteger1() throws Exception {
		reparseSingleArgument(OSCUnsigned.valueOf(0x1L));
	}

	@Test
	public void testArgumentUnsignedIntegerF() throws Exception {
		reparseSingleArgument(OSCUnsigned.valueOf(0xFL));
	}

	@Test
	public void testArgumentUnsignedIntegerFF() throws Exception {
		reparseSingleArgument(OSCUnsigned.valueOf(0xFFL));
	}

	@Test
	public void testArgumentUnsignedIntegerFFF() throws Exception {
		reparseSingleArgument(OSCUnsigned.valueOf(0xFFFL));
	}

	@Test
	public void testArgumentUnsignedIntegerFFFF() throws Exception {
		reparseSingleArgument(OSCUnsigned.valueOf(0xFFFFL));
	}

	@Test
	public void testArgumentUnsignedIntegerFFFFF() throws Exception {
		reparseSingleArgument(OSCUnsigned.valueOf(0xFFFFFL));
	}

	@Test
	public void testArgumentUnsignedIntegerFFFFFF() throws Exception {
		reparseSingleArgument(OSCUnsigned.valueOf(0xFFFFFFL));
	}

	@Test
	public void testArgumentUnsignedIntegerFFFFFFF() throws Exception {
		reparseSingleArgument(OSCUnsigned.valueOf(0xFFFFFFFL));
	}

	@Test
	public void testArgumentUnsignedIntegerFFFFFFFF() throws Exception {
		reparseSingleArgument(OSCUnsigned.valueOf(0xFFFFFFFFL));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testArgumentUnsignedInteger100000000() throws Exception {
		reparseSingleArgument(OSCUnsigned.valueOf(0x100000000L)); // 33bit -> out of range!
	}

	@Test(expected=IllegalArgumentException.class)
	public void testArgumentUnsignedInteger1FFFFFFFF() throws Exception {
		reparseSingleArgument(OSCUnsigned.valueOf(0x1FFFFFFFFL)); // 33bit -> out of range!
	}

	@Test(expected=IllegalArgumentException.class)
	public void testArgumentUnsignedIntegerFFFFFFFFF() throws Exception {
		reparseSingleArgument(OSCUnsigned.valueOf(0xFFFFFFFFFL)); // 36bit -> out of range!
	}

	@Test(expected=IllegalArgumentException.class)
	public void testArgumentUnsignedIntegerMinus1() throws Exception {
		reparseSingleArgument(OSCUnsigned.valueOf(-1L)); // negative/64bit -> out of range!
	}

	@Test(expected=IllegalArgumentException.class)
	public void testArgumentUnsignedIntegerMinLong() throws Exception {
		reparseSingleArgument(OSCUnsigned.valueOf(Long.MIN_VALUE)); // negative -> out of range!
	}

	@Test(expected=IllegalArgumentException.class)
	public void testArgumentUnsignedIntegerMaxLong() throws Exception {
		reparseSingleArgument(OSCUnsigned.valueOf(Long.MAX_VALUE)); // 64bit -> out of range!
	}

	@Test
	public void testArgumentLongMin() throws Exception {
		reparseSingleArgument(Long.MIN_VALUE);
	}

	@Test
	public void testArgumentLongMinus1() throws Exception {
		reparseSingleArgument(-1L);
	}

	@Test
	public void testArgumentLong0() throws Exception {
		reparseSingleArgument(0L);
	}

	@Test
	public void testArgumentLong1() throws Exception {
		reparseSingleArgument(1L);
	}

	@Test
	public void testArgumentLongMax() throws Exception {
		reparseSingleArgument(Long.MAX_VALUE);
	}

	@Test
	public void testArgumentString() throws Exception {

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
	public void testArgumentNull() throws Exception {

		final Comparator<byte[]> nullComparator
				= (obj1, obj2) -> ((obj1 == null) && (obj2 == null)) ? 0 : 1;

		reparseSingleArgument(null, nullComparator);
	}

	@Test
	public void testArgumentBooleanTrue() throws Exception {

		reparseSingleArgument(Boolean.TRUE);
		reparseSingleArgument(true); // uses auto-boxing
	}

	@Test
	public void testArgumentBooleanFalse() throws Exception {

		reparseSingleArgument(Boolean.FALSE);
		reparseSingleArgument(false); // uses auto-boxing
	}

	@Test
	public void testArgumentImpulse() throws Exception {

		reparseSingleArgument(OSCImpulse.INSTANCE);
	}
}
