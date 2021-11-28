// SPDX-FileCopyrightText: 2015-2017 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc;

import com.illposed.osc.argument.ArgumentHandler;
import com.illposed.osc.argument.OSCImpulse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * A global hub, providing general information about this library.
 * The information available here comes either from the libraries manifest file at
 * "{JAR_ROOT}/META-INF/MANIFEST.MF", or is fetched directly from code inside this library.
 */
public final class LibraryInfo {

	private static final String MANIFEST_FILE = "/META-INF/MANIFEST.MF";
	// Public API
	@SuppressWarnings("WeakerAccess")
	public static final String UNKNOWN_VALUE = "<unknown>";
	private static final char MANIFEST_CONTINUATION_LINE_INDICATOR = ' ';
	/** 1 key + 1 value = 2 parts of a key-value pair */
	private static final int KEY_PLUS_VALUE_COUNT = 2;
	private static final Set<Package> UNINTERESTING_PKGS;
	private final Properties manifestProperties;

	static {
		final Set<Package> tmpUninterestingPkgs = new HashSet<>();
		tmpUninterestingPkgs.add(Package.getPackage("java.lang"));
		tmpUninterestingPkgs.add(Package.getPackage("java.util"));
		// NOTE We need to do it like this, because otherwise "java.awt" can not be found
		//   by this classes class-loader.
		final Class<?> javaAwtColorClass = getAwtColor();
		if (javaAwtColorClass != null) {
			tmpUninterestingPkgs.add(javaAwtColorClass.getPackage());
		}
		tmpUninterestingPkgs.add(OSCImpulse.class.getPackage());
		UNINTERESTING_PKGS = Collections.unmodifiableSet(tmpUninterestingPkgs);
	}

	public LibraryInfo() throws IOException {

		this.manifestProperties = readJarManifest();
	}

	private static Properties parseManifestFile(final InputStream manifestIn) throws IOException {

		final Properties manifestProps = new Properties();

		try (final BufferedReader manifestBufferedIn = new BufferedReader(new InputStreamReader(manifestIn,
				StandardCharsets.UTF_8)))
		{
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
							throw new IOException("Invalid manifest line: \"" + manifestLine + '"');
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
		}

		return manifestProps;
	}

	private static Properties readJarManifest() throws IOException {

		Properties mavenProps;

		try (final InputStream manifestFileIn = LibraryInfo.class.getResourceAsStream(MANIFEST_FILE)) {
			if (manifestFileIn == null) {
				throw new IOException("Failed locating resource in the classpath: " + MANIFEST_FILE);
			}
			mavenProps = parseManifestFile(manifestFileIn);
		}

		return mavenProps;
	}

	// Public API
	/**
	 * Returns this application JARs {@link #MANIFEST_FILE} properties.
	 * @return the contents of the manifest file as {@code String} to {@code String} mapping
	 */
	@SuppressWarnings("WeakerAccess")
	public Properties getManifestProperties() {
		return manifestProperties;
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public String getVersion() {
		return getManifestProperties().getProperty("Bundle-Version", UNKNOWN_VALUE);
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public String getOscSpecificationVersion() {
		return getManifestProperties().getProperty("Supported-OSC-Version", UNKNOWN_VALUE);
	}

	public String getLicense() {
		return getManifestProperties().getProperty("Bundle-License", UNKNOWN_VALUE);
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public boolean isArrayEncodingSupported() {
		return true;
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public boolean isArrayDecodingSupported() {
		return true;
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public List<ArgumentHandler> getEncodingArgumentHandlers() {
		return new ArrayList<>(getDecodingArgumentHandlers().values());
	}

	// Public API
	@SuppressWarnings("WeakerAccess")
	public Map<Character, ArgumentHandler> getDecodingArgumentHandlers() {
		return new OSCSerializerAndParserBuilder().getIdentifierToTypeMapping();
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
						+ ':' + markerValueStr;
			} catch (OSCParseException ex) {
				throw new IllegalStateException("Developer error; This should never happen", ex);
			}
		} else {
			classOrMarkerValue = extractPrettyClassName(type.getJavaClass());
		}

		return classOrMarkerValue;
	}

	// Public API
	@SuppressWarnings("unused")
	public String createManifestPropertiesString() {

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

	// Public API
	@SuppressWarnings("WeakerAccess")
	public String createLibrarySummary() {

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

	/**
	 * Checks for StandardProtocolFamily Jdk8 compatibility of the runtime.
	 * E.g. Android API 23 and lower has only a
	 * java 8 subset without java.net.StandardProtocolFamily
	 * @return true when the runtime supports java.net.StandardProtocolFamily
	 * (e.g. Android API 23 and lower)
	 */
	public static boolean hasStandardProtocolFamily() {
		try {
			Class.forName("java.net.StandardProtocolFamily");
			return true;
		} catch (ClassNotFoundException ignore) {
			return false;
		}
	}

	/**
	 * Checks if java.awt.Color is available.
	 * It's not available on Android for example.
	 * Some headless servers might also lack this class.
	 * @return true when the runtime supports java.awt.Color
	 * (e.g. Android)
	 */
	static Class<?> getAwtColor() {
		try {
			return Class.forName("java.awt.Color");
		} catch (ClassNotFoundException ignore) {
			return null;
		}
	}

	public static void main(final String[] args) throws IOException {

		final Logger log = LoggerFactory.getLogger(LibraryInfo.class);
		if (log.isInfoEnabled()) {
			final LibraryInfo libraryInfo = new LibraryInfo();
			log.info(libraryInfo.createLibrarySummary());
//			log.info(libraryInfo.createManifestPropertiesString());
		}
	}
}
