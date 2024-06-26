// SPDX-FileCopyrightText: 2003-2017 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 - 2024 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc;

import com.illposed.osc.argument.OSCImpulse;
import com.illposed.osc.argument.OSCTimeTag64;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @see OSCMessage
 */
public class OSCMessageTest {

	public static OSCMessage createUncheckedAddressMessage(final String address, final List<?> arguments, final OSCMessageInfo info) {
		return new OSCMessage(address, arguments, info, false);
	}

	/**
	 * Creates verbose assertion failures if the result does not conform to what is expected.
	 * @param result received from OSC
	 * @param expected what should have been received
	 * @param optBytes number of optional bytes in the expected array,
	 *   meaning the result may either be {@code expected.length}
	 *   or {@code expected.length - optBytes} in size
	 */
	private static void checkResultEqualsAnswer(byte[] result, byte[] expected, final int optBytes)
	{
		if ((result.length != expected.length) && (result.length != (expected.length - optBytes))) {
			Assertions.fail(createErrorString("Result and expected answer aren't the same length, "
					+ result.length + " vs " + expected.length + '.', result, expected));
		}
		for (int i = 0; i < result.length; i++) {
			if (result[i] != expected[i]) {
				Assertions.fail(createErrorString("Failed to convert correctly at position: " + i,
						result, expected));
			}
		}
	}

	/**
	 * Creates verbose assertion failures if the result does not conform to what is expected.
	 * The OSC 1.0 specification states, that the type tag string should to be present
	 * for all messages, even the ones without arguments, and thus at least the initial ','
	 * (plus 3 padding zeros) should always be present.
	 * Some outdated OSC implementations though, may omit it, which is why for now,
	 * we accept that as valid too.
	 * @param result received from OSC
	 * @param expected what should have been received
	 */
	public static void checkResultEqualsAnswerOptionalComma(byte[] result, byte[] expected) {
		checkResultEqualsAnswer(result, expected, 4);
	}

	static void checkResultEqualsAnswer(byte[] result, byte[] expected) {
		checkResultEqualsAnswer(result, expected, 0);
	}

	public static String createErrorString(
			final String description,
			final byte[] result,
			final byte[] expected)
	{
		return description
				+ "\n result   (str): \"" + new String(result) + '"'
				+ "\n expected (str): \"" + new String(expected) + '"'
				+ "\n result   (raw): \"" + convertByteArrayToJavaCode(result) + '"'
				+ "\n expected (raw): \"" + convertByteArrayToJavaCode(expected) + '"';
	}

	/**
	 * Can be used when creating new test cases.
	 * @param data to be converted to java code that (re-)creates this same byte array
	 * @return one line of code that creates the given data in Java
	 */
	private static String convertByteArrayToJavaCode(final byte[] data) {

		StringBuilder javaCode = new StringBuilder();

		javaCode.append("{ ");
		for (byte b : data) {
			javaCode.append((int) b).append(", ");
		}
		javaCode.delete(javaCode.length() - 2, javaCode.length());
		javaCode.append(" };");

		return javaCode.toString();
	}

	private BytesReceiver convertMessageToBytes(final OSCMessage message) {

		final ByteBuffer buffer = ByteBuffer.allocate(1024);
		final BytesReceiver bytesReceiver = new BufferBytesReceiver(buffer);
		final OSCSerializer stream = new OSCSerializerAndParserBuilder().buildSerializer(bytesReceiver);
		try {
			stream.write(message);
		} catch (final OSCSerializeException ex) {
			throw new RuntimeException(ex);
		}
		return bytesReceiver;
	}

	private byte[] convertMessageToByteArray(final OSCMessage message) {
		return convertMessageToBytes(message).toByteArray();
	}

	@Test
	public void testEmpty() {
		OSCMessage message = new OSCMessage("/empty");
		byte[] answer = { 47, 101, 109, 112, 116, 121, 0, 0, 44, 0, 0, 0 };
		byte[] result = convertMessageToByteArray(message);
		checkResultEqualsAnswerOptionalComma(result, answer);
	}

	@Test
	public void testFillerBeforeCommaNone() {
		final OSCMessage message = new OSCMessage("/abcdef");
		// here we only have the addresses string terminator (0) before the ',' (44),
		// so the comma is 4 byte aligned
		final byte[] answer = { 47, 97, 98, 99, 100, 101, 102, 0, 44, 0, 0, 0 };
		final byte[] result = convertMessageToByteArray(message);
		checkResultEqualsAnswerOptionalComma(result, answer);
	}

	@Test
	public void testFillerBeforeCommaOne() {
		final OSCMessage message = new OSCMessage("/abcde");
		// here we have one padding 0 after the addresses string terminator (also 0)
		// and before the ',' (44), so the comma is 4 byte aligned
		final byte[] answer = { 47, 97, 98, 99, 100, 101, 0, 0, 44, 0, 0, 0 };
		final byte[] result = convertMessageToByteArray(message);
		checkResultEqualsAnswerOptionalComma(result, answer);
	}

	@Test
	public void testFillerBeforeCommaTwo() {
		final OSCMessage message = new OSCMessage("/abcd");
		// here we have two padding 0's after the addresses string terminator (also 0)
		// and before the ',' (44), so the comma is 4 byte aligned
		final byte[] answer = { 47, 97, 98, 99, 100, 0, 0, 0, 44, 0, 0, 0 };
		final byte[] result = convertMessageToByteArray(message);
		checkResultEqualsAnswerOptionalComma(result, answer);
	}

	@Test
	public void testFillerBeforeCommaThree() {
		final OSCMessage message = new OSCMessage("/abcdefg");
		// here we have three padding 0's after the addresses string terminator (also 0)
		// and before the ',' (44), so the comma is 4 byte aligned
		final byte[] answer = { 47, 97, 98, 99, 100, 101, 102, 103, 0, 0, 0, 0, 44, 0, 0, 0 };
		final byte[] result = convertMessageToByteArray(message);
		checkResultEqualsAnswerOptionalComma(result, answer);
	}

	@Test
	public void testArgumentInteger() {
		final List<Object> args = new ArrayList<>(1);
		args.add(99);
		final OSCMessage message = new OSCMessage("/int", args);
		final byte[] answer = { 47, 105, 110, 116, 0, 0, 0, 0, 44, 105, 0, 0, 0, 0, 0, 99 };
		final byte[] result = convertMessageToByteArray(message);
		checkResultEqualsAnswer(result, answer);
	}

	@Test
	public void testArgumentFloat() {
		final List<Object> args = new ArrayList<>(1);
		args.add(999.9f);
		final OSCMessage message = new OSCMessage("/float", args);
		final byte[] answer
				= { 47, 102, 108, 111, 97, 116, 0, 0, 44, 102, 0, 0, 68, 121, -7, -102 };
		final byte[] result = convertMessageToByteArray(message);
		checkResultEqualsAnswer(result, answer);
	}

	@Test
	public void testArgumentDouble() {
		final List<Object> args = new ArrayList<>(1);
		args.add(777777.777);
		final OSCMessage message = new OSCMessage("/double", args);
		final byte[] answer = {
			47, 100, 111, 117, 98, 108, 101, 0, 44, 100, 0, 0, 65, 39, -68, 99, -115, -46, -15, -86
		};
		final byte[] result = convertMessageToByteArray(message);
		checkResultEqualsAnswer(result, answer);
	}

	@Test
	public void testArgumentCharacter() {
		final List<Object> args = new ArrayList<>(1);
		args.add('x');
		final OSCMessage message = new OSCMessage("/char", args);
		final byte[] answer = { 47, 99, 104, 97, 114, 0, 0, 0, 44, 99, 0, 0, 0, 0, 0, 120 };
		final byte[] result = convertMessageToByteArray(message);
		checkResultEqualsAnswer(result, answer);
	}

	@Test
	public void testArgumentBlob() {
		final List<Object> args = new ArrayList<>(1);
		args.add(new byte[] { -1, 0, 1 });
		final OSCMessage message = new OSCMessage("/blob", args);
		final byte[] answer
				= { 47, 98, 108, 111, 98, 0, 0, 0, 44, 98, 0, 0, 0, 0, 0, 3, -1, 0, 1, 0 };
		final byte[] result = convertMessageToByteArray(message);
		checkResultEqualsAnswer(result, answer);
	}

	@Test
	public void testArgumentImpulse() {
		final List<Object> args = new ArrayList<>(1);
		args.add(OSCImpulse.INSTANCE);
		final OSCMessage message = new OSCMessage("/impulse", args);
		final byte[] answer = { 47, 105, 109, 112, 117, 108, 115, 101, 0, 0, 0, 0, 44, 73, 0, 0 };
		final byte[] result = convertMessageToByteArray(message);
		checkResultEqualsAnswer(result, answer);
	}

	@Test
	public void testArgumentLong() {
		final List<Object> args = new ArrayList<>(1);
		args.add(Long.MAX_VALUE);
		final OSCMessage message = new OSCMessage("/long", args);
		final byte[] answer = {
			47, 108, 111, 110, 103, 0, 0, 0, 44, 104, 0, 0, 127, -1, -1, -1, -1, -1, -1, -1 };
		final byte[] result = convertMessageToByteArray(message);
		checkResultEqualsAnswer(result, answer);
	}

	private static Calendar createCalendar() {
		return Calendar.getInstance(TimeZone.getTimeZone("Z"));
	}

	@Test
	public void testArgumentTimestamp0() {
		final List<Object> args = new ArrayList<>(1);
		args.add(OSCTimeTag64.valueOf(new Date(0L)));
		final OSCMessage message = new OSCMessage("/ts/0", args);
		final byte[] answer
				= { 47, 116, 115, 47, 48, 0, 0, 0, 44, 116, 0, 0, -125, -86, 126, -128, 0, 0, 0, 0
				};
		final byte[] result = convertMessageToByteArray(message);
		checkResultEqualsAnswer(result, answer);
	}

	@Test
	public void testArgumentTimestamp2000() {
		final List<Object> args = new ArrayList<>(1);
		final Calendar calendar = createCalendar();
		calendar.clear();
		calendar.set(2000, Calendar.JANUARY, 0);
		args.add(OSCTimeTag64.valueOf(calendar.getTime()));
		final OSCMessage message = new OSCMessage("/ts/2000", args);
		final byte[] answer
				= { 47, 116, 115, 47, 50, 48, 48, 48, 0, 0, 0, 0, 44, 116, 0, 0, -68, 22, 112, -128,
					0, 0, 0, 0 };
		final byte[] result = convertMessageToByteArray(message);
		checkResultEqualsAnswer(result, answer);
	}

	@Test
	public void testArgumentTimestampAfterFeb2036() {
		final List<Object> args = new ArrayList<>(1);
		final Calendar calendar = createCalendar();
		calendar.clear();
		calendar.set(2037, Calendar.JANUARY, 0);
		args.add(OSCTimeTag64.valueOf(calendar.getTime()));
		final OSCMessage message = new OSCMessage("/ts/afterFeb2036", args);
		final byte[] answer
				= { 47, 116, 115, 47, 97, 102, 116, 101, 114, 70, 101, 98, 50, 48, 51, 54, 0, 0, 0,
					0, 44, 116, 0, 0, 1, -80, 17, 0, 0, 0, 0, 0 };
		final byte[] result = convertMessageToByteArray(message);
		checkResultEqualsAnswer(result, answer);
	}

	@Test
	public void testAtLeastOneZeroAfterAddressAndTypesAndArgumentStrings() {
		final List<Object> args = new ArrayList<>(3);
		// We add 3 arguments.
		// Together with the comma before the types,
		// this creates a 4 byte aligned stream again (",sii").
		// In order to separate the types from the argument data,
		// an other four zeros have to appear on the stream.
		// This is what we check for here.
		//
		// We also test for at least one zero after argument strings here (8 % 4 = 0)
		args.add("iiffsstt"); // This would be interpreted as a continuation of the types if they were not zero terminated.
		args.add(-2);
		args.add(-3);
		// We do the same with the address.
		final OSCMessage message = new OSCMessage("/ZAT", args);
		final byte[] answer = {
			47, 90, 65, 84, 0, 0, 0, 0,
			44, 115, 105, 105, 0, 0, 0, 0,
			105, 105, 102, 102, 115, 115, 116, 116, 0, 0, 0, 0,
			-1, -1, -1, -2,
			-1, -1, -1, -3 };
		final byte[] result = convertMessageToByteArray(message);
		checkResultEqualsAnswer(result, answer);
	}

	@Test
	public void testArgumentTrue() {
		final List<Object> args = new ArrayList<>(1);
		args.add(true);
		final OSCMessage message = new OSCMessage("/true", args);
		final byte[] answer = { 47, 116, 114, 117, 101, 0, 0, 0, 44, 84, 0, 0 };
		final byte[] result = convertMessageToByteArray(message);
		checkResultEqualsAnswer(result, answer);
	}

	@Test
	public void testArgumentFalse() {
		final List<Object> args = new ArrayList<>(1);
		args.add(false);
		final OSCMessage message = new OSCMessage("/false", args);
		final byte[] answer = { 47, 102, 97, 108, 115, 101, 0, 0, 44, 70, 0, 0 };
		final byte[] result = convertMessageToByteArray(message);
		checkResultEqualsAnswer(result, answer);
	}

	@Test
	public void testArgumentNull() {
		final List<Object> args = new ArrayList<>(1);
		args.add(null);
		final OSCMessage message = new OSCMessage("/null", args);
		final byte[] answer = { 47, 110, 117, 108, 108, 0, 0, 0, 44, 78, 0, 0 };
		final byte[] result = convertMessageToByteArray(message);
		checkResultEqualsAnswer(result, answer);
	}

	@Test
	public void testDecreaseVolume() {
		List<Object> args = new ArrayList<>(2);
		args.add(1);
		args.add(0.2f);
		OSCMessage message = new OSCMessage("/sc/mixer/volume", args);
		byte[] answer = {
			47, 115, 99, 47, 109, 105, 120, 101, 114, 47, 118, 111,
			108, 117, 109, 101, 0, 0, 0, 0, 44, 105, 102, 0, 0, 0, 0,
			1, 62, 76, -52, -51 };
		byte[] result = convertMessageToByteArray(message);
		checkResultEqualsAnswer(result, answer);
	}

	/**
	 * See the comment in
	 * {@link OSCSerializerTest#testPrintFloat2OnStream}.
	 */
	@Test
	public void testIncreaseVolume() {
		List<Object> args = new ArrayList<>(2);
		args.add(1);
		args.add(1.0f);
		OSCMessage message = new OSCMessage("/sc/mixer/volume", args);
		byte[] answer =	{
			47, 115, 99, 47, 109, 105, 120, 101, 114, 47, 118, 111, 108,
			117, 109, 101, 0, 0, 0, 0, 44, 105, 102, 0, 0, 0, 0, 1,	63,
			(byte) 128, 0, 0};
		byte[] result = convertMessageToByteArray(message);
		checkResultEqualsAnswer(result, answer);
	}

	@Test
	public void testRun() {
		OSCMessage message = new OSCMessage("/sc/run");
		byte[] answer = {47, 115, 99, 47, 114, 117, 110, 0, 44, 0, 0, 0};
		byte[] result = convertMessageToByteArray(message);
		checkResultEqualsAnswerOptionalComma(result, answer);
	}

	@Test
	public void testStop() {
		OSCMessage message = new OSCMessage("/sc/stop");
		byte[] answer = {47, 115, 99, 47, 115, 116, 111, 112, 0, 0, 0, 0, 44, 0, 0, 0};
		byte[] result = convertMessageToByteArray(message);
		checkResultEqualsAnswerOptionalComma(result, answer);
	}

	@Test
	public void testCreateSynth() {

		final List<Object> origArguments = new ArrayList<>(3);
		origArguments.add(1001);
		origArguments.add("freq");
		origArguments.add(440.0f);
		OSCMessage message = new OSCMessage("/s_new", origArguments);
		byte[] answer
				= {0x2F, 0x73, 0x5F, 0x6E, 0x65, 0x77, 0, 0, 0x2C, 0x69, 0x73, 0x66, 0, 0, 0, 0, 0,
					0, 0x3, (byte) 0xE9, 0x66, 0x72, 0x65, 0x71, 0, 0, 0, 0, 0x43, (byte) 0xDC, 0,
					0};
		byte[] result = convertMessageToByteArray(message);
		checkResultEqualsAnswer(result, answer);
	}

	@Test
	public void testArgumentCollectionsMixed() {
		final List<Object> args = new ArrayList<>(5);
		final Collection<Integer> singleType = new LinkedHashSet<>();
		singleType.add(-1);
		singleType.add(0);
		singleType.add(1);
		singleType.add(2);
		singleType.add(-1); // double entry; discarded because we have a Set
		singleType.add(99);
		final List<Integer> singleTypeList = new ArrayList<>(singleType);
		final List<Object> allTypes = new LinkedList<>();
		allTypes.add(null);
		allTypes.add(Boolean.TRUE);
		allTypes.add(Boolean.FALSE);
		allTypes.add(OSCImpulse.INSTANCE);
		allTypes.add(1);
		allTypes.add(1.0f);
		allTypes.add(1.0);
		allTypes.add(new byte[] { -99, -1, 0, 1, 99 });
		allTypes.add(1L);
		allTypes.add('h');
		allTypes.add("hello world!");
		allTypes.add(OSCTimeTag64.valueOf(new Date(0L)));
		args.add("firstArg");
		args.add(singleTypeList);
		args.add("middleArg");
		args.add(allTypes);
		args.add("lastArg");
		final OSCMessage message = new OSCMessage("/collectionsMixed", args);
		final byte[] answer
				= { 47, 99, 111, 108, 108, 101, 99, 116, 105, 111, 110, 115, 77, 105, 120, 101, 100,
					0, 0, 0, 44, 115, 91, 105, 105, 105, 105, 105, 93, 115, 91, 78, 84, 70, 73, 105,
					102, 100, 98, 104, 99, 115, 116, 93, 115, 0, 0, 0, 102, 105, 114, 115, 116, 65,
					114, 103, 0, 0, 0, 0, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0,
					0, 99, 109, 105, 100, 100, 108, 101, 65, 114, 103, 0, 0, 0, 0, 0, 0, 1, 63,
					-128, 0, 0, 63, -16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, -99, -1, 0, 1, 99, 0, 0, 0,
					0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 104, 104, 101, 108, 108, 111, 32, 119, 111,
					114, 108, 100, 33, 0, 0, 0, 0, -125, -86, 126, -128, 0, 0, 0, 0, 108, 97, 115,
					116, 65, 114, 103, 0 };
		final byte[] result = convertMessageToByteArray(message);
		checkResultEqualsAnswer(result, answer);
	}

	@Test
	public void testArgumentCollectionsRecursiveMixed() {
		final List<Object> args = new ArrayList<>(5);
		final Collection<Object> fourthLevel = new LinkedList<>();
		fourthLevel.add(null);
		fourthLevel.add(Boolean.TRUE);
		fourthLevel.add(Boolean.FALSE);
		fourthLevel.add(OSCImpulse.INSTANCE);
		fourthLevel.add(1);
		fourthLevel.add(1.0f);
		fourthLevel.add(1.0);
		fourthLevel.add(new byte[] { -99, -1, 0, 1, 99 });
		fourthLevel.add(1L);
		fourthLevel.add('h');
		fourthLevel.add("hello world!");
		fourthLevel.add(OSCTimeTag64.valueOf(new Date(0L)));
		final Collection<Object> thirdLevel = new LinkedList<>();
		thirdLevel.add(fourthLevel);
		thirdLevel.add(-1);
		thirdLevel.add('h');
		thirdLevel.add(9.9);
		final Collection<Object> secondLevel = new LinkedList<>();
		secondLevel.add(0);
		secondLevel.add(thirdLevel);
		secondLevel.add('e');
		secondLevel.add(8.8);
		final Collection<Object> firstLevel = new LinkedList<>();
		firstLevel.add(1);
		firstLevel.add('l');
		firstLevel.add(secondLevel);
		firstLevel.add(7.7);
		args.add("firstArg");
		args.add(firstLevel);
		args.add("lastArg");
		final OSCMessage message = new OSCMessage("/collectionsRecursive", args);
		final byte[] answer
				= { 47, 99, 111, 108, 108, 101, 99, 116, 105, 111, 110, 115, 82, 101, 99, 117, 114,
					115, 105, 118, 101, 0, 0, 0, 44, 115, 91, 105, 99, 91, 105, 91, 91, 78, 84, 70,
					73, 105, 102, 100, 98, 104, 99, 115, 116, 93, 105, 99, 100, 93, 99, 100, 93,
					100, 93, 115, 0, 0, 0, 0, 102, 105, 114, 115, 116, 65, 114, 103, 0, 0, 0, 0, 0,
					0, 0, 1, 0, 0, 0, 108, 0, 0, 0, 0, 0, 0, 0, 1, 63, -128, 0, 0, 63, -16, 0, 0, 0,
					0, 0, 0, 0, 0, 0, 5, -99, -1, 0, 1, 99, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0,
					0, 104, 104, 101, 108, 108, 111, 32, 119, 111, 114, 108, 100, 33, 0, 0, 0, 0,
					-125, -86, 126, -128, 0, 0, 0, 0, -1, -1, -1, -1, 0, 0, 0, 104, 64, 35, -52,
					-52, -52, -52, -52, -51, 0, 0, 0, 101, 64, 33, -103, -103, -103, -103, -103,
					-102, 64, 30, -52, -52, -52, -52, -52, -51, 108, 97, 115, 116, 65, 114, 103,
					0 };
		final byte[] result = convertMessageToByteArray(message);
		checkResultEqualsAnswer(result, answer);
	}

	@Test
	public void testEncodeLong() throws OSCParseException {
		Long one001 = 1001L;
		final List<?> origArguments = Collections.singletonList(one001);
		OSCMessage message = new OSCMessage("/dummy", origArguments);
		byte[] byteArray = convertMessageToByteArray(message);
		OSCParser converter = new OSCSerializerAndParserBuilder().buildParser();
		final ByteBuffer bytes = ByteBuffer.wrap(byteArray).asReadOnlyBuffer();
		final OSCMessage packet = (OSCMessage) converter.convert(bytes);
		if (!packet.getAddress().equals("/dummy")) {
			Assertions.fail("Send Big Integer did not receive the correct address");
		}
		List<?> arguments = packet.getArguments();
		if (arguments.size() != 1) {
			Assertions.fail("Send Big Integer should have 1 argument, not " + arguments.size());
		}
		if (!(arguments.get(0) instanceof Long)) {
			Assertions.fail("arguments.get(0) should be a Long, not " + arguments.get(0).getClass());
		}
		if (!(Long.valueOf(1001L).equals(arguments.get(0)))) {
			Assertions.fail("Instead of Long(1001), received " + arguments.get(0));
		}
	}

	@Test
	public void testEncodeArray() throws OSCParseException {
		final List<Float> floats = new ArrayList<>(2);
		floats.add(10.0f);
		floats.add(100.0f);
		final List<?> origArguments = Collections.singletonList(floats);
		final OSCMessage message = new OSCMessage("/dummy", origArguments);
		final byte[] byteArray = convertMessageToByteArray(message);
		final OSCParser converter = new OSCSerializerAndParserBuilder().buildParser();
		final ByteBuffer bytes = ByteBuffer.wrap(byteArray).asReadOnlyBuffer();
		final OSCMessage packet = (OSCMessage) converter.convert(bytes);
		if (!packet.getAddress().equals("/dummy")) {
			Assertions.fail("We did not receive the correct address");
		}
		final List<?> arguments = packet.getArguments();
		if (arguments.size() != origArguments.size()) {
			Assertions.fail("We should have received " + origArguments + " arguments, not "
					+ arguments.size());
		}
		final Object firstArgument = arguments.get(0);
		if (!(firstArgument instanceof List)) {
			Assertions.fail("arguments.get(0) should be a List, not "
					+ ((firstArgument == null)
							? String.valueOf((Object) null)
							: firstArgument.getClass().toString()));
		}
		// We can safely suppress the warning, as we already made sure the cast will not fail.
		@SuppressWarnings("unchecked") final List<Object> theArray = (List<Object>) firstArgument;
		if (theArray.size() != floats.size()) {
			Assertions.fail("arguments.get(0) should be a List of size " + floats.size() + " not "
					+ theArray.size());
		}
		for (int i = 0; i < floats.size(); ++i) {
			if (!floats.get(i).equals(theArray.get(i))) {
				Assertions.fail("List element " + i + " should be " + floats.get(i) + " not "
						+ theArray.get(i));
			}
		}
	}

	@Test
	public void testAddressValidationFrontendConstructorNull() {

		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> new OSCMessage(null)
		);
	}

	@Test
	public void testAddressValidationFrontendConstructorValid() {

		// expect no exception, as the address is valid
		OSCMessage oscMessage = new OSCMessage("/hello/world");
	}

	@Test
	public void testAddressValidationFrontendConstructorInvalid() {

		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> new OSCMessage("/ hello/world")
		);
	}

	@Test
	public void testAddressValidation() {
		Assertions.assertFalse(OSCMessage.isValidAddress(null));
		Assertions.assertFalse(OSCMessage.isValidAddress(""));
		Assertions.assertFalse(OSCMessage.isValidAddress("hello/world"));
		Assertions.assertFalse(OSCMessage.isValidAddress("/ hello/world"));
		Assertions.assertFalse(OSCMessage.isValidAddress("/#hello/world"));
		Assertions.assertFalse(OSCMessage.isValidAddress("/*hello/world"));
		Assertions.assertFalse(OSCMessage.isValidAddress("/,hello/world"));
		Assertions.assertFalse(OSCMessage.isValidAddress("/?hello/world"));
		Assertions.assertFalse(OSCMessage.isValidAddress("/[hello/world"));
		Assertions.assertFalse(OSCMessage.isValidAddress("/]hello/world"));
		Assertions.assertFalse(OSCMessage.isValidAddress("/{hello/world"));
		Assertions.assertFalse(OSCMessage.isValidAddress("/}hello/world"));
		Assertions.assertFalse(OSCMessage.isValidAddress("//hello/world"));
		Assertions.assertFalse(OSCMessage.isValidAddress("#abc"));
		Assertions.assertFalse(OSCMessage.isValidAddress("#replyx"));
		Assertions.assertFalse(OSCMessage.isValidAddress("#replx"));
		Assertions.assertTrue( OSCMessage.isValidAddress("/hello"));
		Assertions.assertTrue( OSCMessage.isValidAddress("/hello/world"));
		Assertions.assertTrue( OSCMessage.isValidAddress("/hello/world/two"));
		Assertions.assertTrue( OSCMessage.isValidAddress("/123/world/two"));
		Assertions.assertTrue( OSCMessage.isValidAddress("/!hello/world"));
		Assertions.assertTrue( OSCMessage.isValidAddress("/~hello/world"));
		Assertions.assertTrue( OSCMessage.isValidAddress("/`hello/world"));
		Assertions.assertTrue( OSCMessage.isValidAddress("/@hello/world"));
		Assertions.assertTrue( OSCMessage.isValidAddress("/$hello/world"));
		Assertions.assertTrue( OSCMessage.isValidAddress("/%hello/world"));
		Assertions.assertTrue( OSCMessage.isValidAddress("/€hello/world"));
		Assertions.assertTrue( OSCMessage.isValidAddress("/^hello/world"));
		Assertions.assertTrue( OSCMessage.isValidAddress("/&hello/world"));
		Assertions.assertTrue( OSCMessage.isValidAddress("/(hello/world"));
		Assertions.assertTrue( OSCMessage.isValidAddress("/)hello/world"));
		Assertions.assertTrue( OSCMessage.isValidAddress("/-hello/world"));
		Assertions.assertTrue( OSCMessage.isValidAddress("/_hello/world"));
		Assertions.assertTrue( OSCMessage.isValidAddress("/+hello/world"));
		Assertions.assertTrue( OSCMessage.isValidAddress("/=hello/world"));
		Assertions.assertTrue( OSCMessage.isValidAddress("/.hello/world"));
		Assertions.assertTrue( OSCMessage.isValidAddress("/<hello/world"));
		Assertions.assertTrue( OSCMessage.isValidAddress("/>hello/world"));
		Assertions.assertTrue( OSCMessage.isValidAddress("/;hello/world"));
		Assertions.assertTrue( OSCMessage.isValidAddress("/:hello/world"));
		Assertions.assertTrue( OSCMessage.isValidAddress("/'hello/world"));
		Assertions.assertTrue( OSCMessage.isValidAddress("/\"hello/world"));
		Assertions.assertTrue( OSCMessage.isValidAddress("#reply"));
	}
}
