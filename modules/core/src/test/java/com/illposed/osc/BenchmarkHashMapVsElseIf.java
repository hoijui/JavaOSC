/*
 * Copyright (C) 2014, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * SPDX-License-Identifier: BSD-3-Clause
 * See file LICENSE.md for more information.
 */

package com.illposed.osc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger LOG = LoggerFactory.getLogger(BenchmarkHashMapVsElseIf.class);

	private static final Map<Class, Character> JAVA_CLASS_TO_OSC_TYPE;
	static {
		Map<Class, Character> classToType = new HashMap<>(6);

		classToType.put(Integer.class, 'i');
		classToType.put(Long.class, 'h');
		classToType.put(Float.class, 'f');
		classToType.put(Double.class, 'd');
		classToType.put(String.class, 's');
		classToType.put(Character.class, 'c');

		JAVA_CLASS_TO_OSC_TYPE = Collections.unmodifiableMap(classToType);
	}
	private static final List<Class> JAVA_CLASSES
			= new ArrayList<>(JAVA_CLASS_TO_OSC_TYPE.keySet());
	private static final Random TYPE_GENERATOR_RND = new Random();

	public static void main(String[] args) {

		final int numDataPoints = 1000000;
		final int numTestRuns = 100;

		runBenchmark(numDataPoints, numTestRuns);
	}

	@SuppressWarnings("WeakerAccess")
	public static void runBenchmark(final int numDataPoints, final int numTestRuns) {

		LOG.info("Generating {} data-points", numDataPoints);
		long start = System.currentTimeMillis();
		final List<Class> generateRandomTypes = generateRandomTypes(numDataPoints);
		final long timeGenerateData = System.currentTimeMillis() - start;
		LOG.info("Time data generation: {} ms", timeGenerateData);

		LOG.info("Running '... else if ...' benchmark {} times ...", numTestRuns);
		start = System.currentTimeMillis();
		for (int tri = 0; tri < numTestRuns; tri++) {
			for (Class type : generateRandomTypes) {
				convertToTypeElseIf(type);
			}
		}
		final long timeElseIf = (System.currentTimeMillis() - start) / numTestRuns;
		LOG.info("Average time '... else if ...': {} ms", timeElseIf);

		LOG.info("Running 'HashMap' benchmark {} times ...", numTestRuns);
		start = System.currentTimeMillis();
		for (int tri = 0; tri < numTestRuns; tri++) {
			for (Class type : generateRandomTypes) {
				convertToTypeHashMap(type);
			}
		}
		final long timeHashMap = (System.currentTimeMillis() - start) / numTestRuns;
		LOG.info("Average time 'HashMap':         {} ms", timeHashMap);
	}

	private static Class generateRandomType() {
		return JAVA_CLASSES.get(TYPE_GENERATOR_RND.nextInt(JAVA_CLASSES.size()));
	}

	private static List<Class> generateRandomTypes(int numEntries) {

		List<Class> types = new ArrayList<>(numEntries);
		for (int ti = 0; ti < numEntries; ti++) {
			types.add(generateRandomType());
		}

		return types;
	}

	@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
	public static Character convertToTypeHashMap(Class cls) {
		return JAVA_CLASS_TO_OSC_TYPE.get(cls);
	}

	@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
	public static Character convertToTypeElseIf(Class cls) {

		final char type;

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
