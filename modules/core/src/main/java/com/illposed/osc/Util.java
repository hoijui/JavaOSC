/*
 * Copyright (C) 2001-2020, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.illposed.osc;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class Util {

	// Override the default constructor, preventing callers from instantiating a
	// utility class, which would make no sense.
	private Util() {}

	public static byte[] concat(final List<byte[]> byteArrays) {

		int totalLength = 0;

		for (byte[] byteArray : byteArrays) {
			totalLength += byteArray.length;
		}

		byte[] result = new byte[totalLength];

		int i = 0;
		for (byte[] byteArray : byteArrays) {
			System.arraycopy(byteArray, 0, result, i, byteArray.length);
			i += byteArray.length;
		}

		return result;
	}

	public static byte[] concat(final byte[]... byteArrays) {
		return concat(Arrays.asList(byteArrays));
	}

	public static <T> byte[] concat(
			final Function<T, byte[]> transform, final List<T> things)
	{
		List<byte[]> byteArrays = new ArrayList<>();

		for (T thing : things) {
			byteArrays.add(transform.apply(thing));
		}

		return concat(byteArrays);
	}

	public static <T> byte[] concat(
			final Function<T, byte[]> transform, final T... things)
	{
		return concat(transform, Arrays.asList(things));
	}
}
