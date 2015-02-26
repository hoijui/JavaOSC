/*
 * Copyright (C) 2015, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc;

import com.illposed.osc.argument.ArgumentHandler;
import com.illposed.osc.argument.OSCImpulse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A global hub, providing general information about this library.
 * The information available here comes either from the libraries manifest file at
 * "{JAR_ROOT}/META-INF/MANIFEST.MF", or is fetched directly from code inside this library.
 */
public final class LibraryInfo {

	private static final String MANIFEST_FILE = "/META-INF/MANIFEST.MF";
	public static final String UNKNOWN_VALUE = "<unknown>";
	private static final char MANIFEST_CONTINUATION_LINE_INDICATOR = ' ';
	/** 1 key + 1 value = 2 parts of a key-value pair */
	private static final int KEY_PLUS_VALUE_COUNT = 2;
	private static final Set<Package> UNINTERESTING_PKGS;
	private static Properties manifestProperties;
	private static final Lock MANIFEST_PROPERTIES_INIT = new ReentrantLock();

	static {
		final Set<Package> tmpUninterestingPkgs = new HashSet<Package>();
		tmpUninterestingPkgs.add(Package.getPackage("java.lang"));
		tmpUninterestingPkgs.add(Package.getPackage("java.util"));
		// NOTE We need to do it like this, because otherwise "java.awt" can not be found
		//   by this classes class-loader.
		tmpUninterestingPkgs.add(java.awt.Color.class.getPackage());
		tmpUninterestingPkgs.add(OSCImpulse.class.getPackage());
		UNINTERESTING_PKGS = Collections.unmodifiableSet(tmpUninterestingPkgs);
	}

	private LibraryInfo() {
		// utility class
	}

	private static Properties parseManifestFile(final InputStream manifestIn) throws IOException {

		final Properties manifestProps = new Properties();

		BufferedReader manifestBufferedIn = null;
		try {
			manifestBufferedIn = new BufferedReader(new InputStreamReader(manifestIn,
					Charset.forName("UTF-8")));
			String manifestLine = manifestBufferedIn.readLine();
			String currentKey = null;
			final StringBuilder currentValue = new StringBuilder(80);
			// NOTE one property can be specified on multiple lines.
			//   This is done by prepending all but the first line with white-space, for example:
			//   "My-Key: hello, this is my very long property value, which is sp"
			//   " lit over multiple lines, and because we also want to show the "
			//   " third line, we write a little more."
			while (manifestLine != null) {
				// filter out empty lines and comments
				// NOTE Is there really a comment syntax defined for manifest files?
				if (!manifestLine.trim().isEmpty() && !manifestLine.startsWith("[#%]")) {
					if (manifestLine.charAt(0) == MANIFEST_CONTINUATION_LINE_INDICATOR) {
						// remove the initial space and add it to the already read value
						currentValue.append(manifestLine.substring(1));
					} else {
						if (currentKey != null) {
							manifestProps.setProperty(currentKey, currentValue.toString());
						}
						final String[] keyAndValue = manifestLine.split(": ", KEY_PLUS_VALUE_COUNT);
						if (keyAndValue.length < KEY_PLUS_VALUE_COUNT) {
							throw new IOException("Invalid manifest line: \"" + manifestLine + "\"");
						}
						currentKey = keyAndValue[0];
						currentValue.setLength(0);
						currentValue.append(keyAndValue[1]);
					}
				}
				manifestLine = manifestBufferedIn.readLine();
			}
			if (currentKey != null) {
				manifestProps.setProperty(currentKey, currentValue.toString());
			}
		} finally {
			if (manifestBufferedIn != null) {
				manifestBufferedIn.close();
			}
		}

		return manifestProps;
	}

	private static Properties readJarManifest() throws IOException {

		Properties mavenProps = null;

		InputStream manifestFileIn = null;
		try {
			manifestFileIn = LibraryInfo.class.getResourceAsStream(MANIFEST_FILE);
			if (manifestFileIn == null) {
				throw new IOException("Failed locating resource in the classpath: "
						+ MANIFEST_FILE);
			}
			mavenProps = parseManifestFile(manifestFileIn);
		} finally {
			if (manifestFileIn != null) {
				manifestFileIn.close();
			}
		}

		return mavenProps;
	}

	/**
	 * Reads this application JARs {@link #MANIFEST_FILE} properties file.
	 * @return the contents of the manifest file as {@code String} to {@code String} mapping
	 */
	public static Properties getManifestProperties() {

		if (manifestProperties == null) {
			try {
				MANIFEST_PROPERTIES_INIT.lockInterruptibly();
				if (manifestProperties == null) {
					manifestProperties = readJarManifest();
				}
			} catch (final IOException ex) {
				throw new IllegalStateException(ex);
			} catch (final InterruptedException ex) {
				throw new IllegalStateException(ex);
			} finally {
				MANIFEST_PROPERTIES_INIT.unlock();
			}
		}

		return manifestProperties;
	}

	public static String getVersion() {
		return getManifestProperties().getProperty("Bundle-Version", UNKNOWN_VALUE);
	}

	public static String getOscSpecificationVersion() {
		return getManifestProperties().getProperty("Supported-OSC-Version", UNKNOWN_VALUE);
	}

	public static String getLicense() {
		return getManifestProperties().getProperty("Bundle-License", UNKNOWN_VALUE);
	}

	public static boolean isArrayEncodingSupported() {
		return true;
	}

	public static boolean isArrayDecodingSupported() {
		return true;
	}

	public static List<ArgumentHandler> getEncodingArgumentHandlers() {
		return OSCSerializerFactory.createDefaultFactory().getArgumentHandlers();
	}

	public static Map<Character, ArgumentHandler> getDecodingArgumentHandlers() {
		return OSCParserFactory.createDefaultFactory().getIdentifierToTypeMapping();
	}

	private static String extractPrettyClassName(final Class javaClass) {

		final String prettyClassName;
		if (UNINTERESTING_PKGS.contains(javaClass.getPackage())) {
			prettyClassName = javaClass.getSimpleName();
		} else {
			prettyClassName = javaClass.getCanonicalName();
		}
		return prettyClassName;
	}

	private static String extractTypeClassOrMarkerValue(final ArgumentHandler type) {

		final String classOrMarkerValue;
		if (type.isMarkerOnly()) {
			try {
				final Object markerValue = type.parse(null);
				final String markerValueStr = (markerValue == null) ? "null"
						: markerValue.toString();
				classOrMarkerValue = extractPrettyClassName(type.getJavaClass())
						+ ":" + markerValueStr;
			} catch (OSCParseException ex) {
				throw new IllegalStateException("Developper error; This should never happen", ex);
			}
		} else {
			classOrMarkerValue = extractPrettyClassName(type.getJavaClass());
		}

		return classOrMarkerValue;
	}

	public static String createManifestPropertiesString() {

		final StringBuilder info = new StringBuilder(1024);

		for (final Map.Entry<Object, Object> manifestEntry : getManifestProperties().entrySet()) {
			final String key = (String) manifestEntry.getKey();
			final String value = (String) manifestEntry.getValue();
			info
					.append(String.format("%32s", key))
					.append(" -> ")
					.append(value)
					.append('\n');
		}

		return info.toString();
	}

	public static String createLibrarySummary() {

		final StringBuilder summary = new StringBuilder(1024);

		summary
				.append("\nName:        ").append(getManifestProperties().getProperty("Bundle-Name", UNKNOWN_VALUE))
				.append("\nDescription: ").append(getManifestProperties().getProperty("Bundle-Description", UNKNOWN_VALUE))
				.append("\nVersion:     ").append(getVersion())
				.append("\nOSC-Spec.:   ").append(getOscSpecificationVersion())
				.append("\nLicense:     ").append(getLicense())
				.append("\nArgument serialization:"
						+ "\n                  [Java] -> [OSC]");
		if (isArrayEncodingSupported()) {
			summary.append("\n        ")
					.append(String.format("%16s", extractPrettyClassName(List.class)))
					.append(" -> '")
					.append(OSCParser.TYPE_ARRAY_BEGIN)
					.append("'...'")
					.append(OSCParser.TYPE_ARRAY_END)
					.append('\'');
		}
		for (final ArgumentHandler encodingType : getEncodingArgumentHandlers()) {
			summary.append("\n        ")
					.append(String.format("%16s", extractTypeClassOrMarkerValue(encodingType)))
					.append(" -> '")
					.append(encodingType.getDefaultIdentifier())
					.append('\'');
		}

		summary
				.append("\nArgument parsing:"
						+ "\n                   [OSC] -> [Java]");
		if (isArrayDecodingSupported()) {
			summary.append("\n               '")
					.append(OSCParser.TYPE_ARRAY_BEGIN)
					.append("'...'")
					.append(OSCParser.TYPE_ARRAY_END)
					.append("' -> ")
					.append(extractPrettyClassName(List.class));
		}
		for (final Map.Entry<Character, ArgumentHandler> decodingType : getDecodingArgumentHandlers().entrySet()) {
			summary.append("\n                     '")
					.append(decodingType.getKey())
					.append("' -> ")
					.append(extractTypeClassOrMarkerValue(decodingType.getValue()));
		}

		return summary.toString();
	}

	public static void main(final String[] args) {

		System.out.println(createLibrarySummary());
//		System.out.println(createManifestPropertiesString());
	}
}
