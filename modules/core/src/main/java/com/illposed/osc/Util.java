/*
 * Copyright (C) 2001-2020, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Util {
	public static byte[] concat(List<byte[]> byteArrays) {
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

	public static byte[] concat(byte[]... byteArrays) {
		return concat(Arrays.asList(byteArrays));
	}

	public static <T> byte[] concat(
    Function<T, byte[]> transform, List<T> things)
  {
		List<byte[]> byteArrays = new ArrayList<>();

		for (T thing : things) {
			byteArrays.add(transform.apply(thing));
		}

		return concat(byteArrays);
	}

	public static <T> byte[] concat(Function<T, byte[]> transform, T... things) {
		return concat(transform, Arrays.asList(things));
	}
}
