/*
 * Copyright (C) 2014, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Runs a very basic benchmark test to check whether using an else-if-chain or
 * a HashMap based approach is faster, when trying to convert a java class to
 * an OSC Type indicator char.
 */
public class BenchmarkHashMapVsElseIf {

	private static final Map<Class, Character> JAVA_CLASS_TO_OSC_TYPE;
	static {
		Map<Class, Character> classToType = new HashMap<Class, Character>(6);

		classToType.put(Integer.class, 'i');
		classToType.put(Long.class, 'h');
		classToType.put(Float.class, 'f');
		classToType.put(Double.class, 'd');
		classToType.put(String.class, 's');
		classToType.put(Character.class, 'c');

		JAVA_CLASS_TO_OSC_TYPE = Collections.unmodifiableMap(classToType);
	}
	private static final List<Class> JAVA_CLASSES = new ArrayList<Class>(JAVA_CLASS_TO_OSC_TYPE.keySet());
	private static final Random TYPE_GENERATOR_RND = new Random();

	public static void main(String[] args) {

		final int numDataPoints = 1000000;
		final int numTestRuns = 100;

		runBenchmark(numDataPoints, numTestRuns);
	}

	public static void runBenchmark(final int numDataPoints, final int numTestRuns) {

		System.err.printf("Generating %d data-points\n", numDataPoints);
		long start = System.currentTimeMillis();
		final List<Class> generateRandomTypes = generateRandomTypes(numDataPoints);
		final long timeGenerateData = System.currentTimeMillis() - start;
		System.err.printf("Time data generation: %d ms\n", timeGenerateData);

		System.err.printf("Running '... else if ...' benchmark %d times ...\n", numTestRuns);
		start = System.currentTimeMillis();
		for (int tri = 0; tri < numTestRuns; tri++) {
			for (Class type : generateRandomTypes) {
				convertToTypeElseIf(type);
			}
		}
		final long timeElseIf = (System.currentTimeMillis() - start) / numTestRuns;
		System.err.printf("Average time '... else if ...': %d ms\n", timeElseIf);

		System.err.printf("Running 'HashMap' benchmark %d times ...\n", numTestRuns);
		start = System.currentTimeMillis();
		for (int tri = 0; tri < numTestRuns; tri++) {
			for (Class type : generateRandomTypes) {
				convertToTypeHashMap(type);
			}
		}
		final long timeHashMap = (System.currentTimeMillis() - start) / numTestRuns;
		System.err.printf("Average time 'HashMap':         %d ms\n", timeHashMap);
	}

	private static Class generateRandomType() {
		return JAVA_CLASSES.get(TYPE_GENERATOR_RND.nextInt(JAVA_CLASSES.size()));
	}

	private static List<Class> generateRandomTypes(int numEntries) {

		List<Class> types = new ArrayList<Class>(numEntries);
		for (int ti = 0; ti < numEntries; ti++) {
			types.add(generateRandomType());
		}

		return types;
	}

	public static Character convertToTypeHashMap(Class cls) {
		return JAVA_CLASS_TO_OSC_TYPE.get(cls);
	}

	public static Character convertToTypeElseIf(Class cls) {

		final Character type;

		// A big ol' else-if chain -- what's polymorphism mean, again?
		// I really wish I could extend the base classes!
		if (Integer.class.equals(cls)) {
			type = 'i';
		} else if (Long.class.equals(cls)) {
			type = 'h';
		} else if (Float.class.equals(cls)) {
			type = 'f';
		} else if (Double.class.equals(cls)) {
			type = 'd';
		} else if (String.class.equals(cls)) {
			type = 's';
		} else if (Character.class.equals(cls)) {
			type = 'c';
		} else {
			throw new RuntimeException();
		}

		return type;
	}
}
