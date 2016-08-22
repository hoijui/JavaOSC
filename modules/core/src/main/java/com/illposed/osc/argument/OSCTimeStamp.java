/*
 * Copyright (C) 2015, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.argument;

import java.io.Serializable;
import java.util.Date;

/**
 * This represents an OSC compliant time-stamp.
 * It uses the NTP (Network Time Protocol) time format,
 * and supports a different time range (smaller) and resolution (more precise)
 * then the Java <code>Date</code> class.
 * See <a href="https://en.wikipedia.org/wiki/Network_Time_Protocol#Timestamps">
 * the NTP time-stamp documentation on Wikipedia</a> for specification details.
 * TODO When advancing to Java 8, we should introduce <tt>toInstant()</tt>
 *   and <tt>valueOf(Instant)</tt> methods (see {@link java.time.Instant}),
 *   as it covers the range of the OSC time format, and nearly covers the precision
 *   (1 nanosecond accuracy instead of 233 picoseconds).
 */
public class OSCTimeStamp implements Cloneable, Serializable, Comparable<OSCTimeStamp> {

	/**
	 * Baseline NTP time if bit-0=0 is "7-Feb-2036 @ 06:28:16 UTC" as Java time-stamp.
	 * Used for all dates from those one up until "Tue Feb 26 10:42:23 CET 2104".
	 * Dates after that can not be represented with an OSC time-stamp.
	 */
	private static final long MSB_0_BASE_TIME = 2085978496000L;
	/**
	 * Baseline NTP time if bit-0=1 is "1-Jan-1900 @ 01:00:00 UTC" as Java time-stamp.
	 * It is used for all dates after this one up until the date of {@link #MSB_0_BASE_TIME}.
	 * Dates before this one can not be represented with an OSC time-stamp.
	 */
	private static final long MSB_1_BASE_TIME = -2208988800000L;
	/**
	 * The OSC time-stamp with the semantics of "immediately".
	 */
	public static final long IMMEDIATE_RAW = 0x1L;
	/**
	 * The OSC time-stamp with the semantics of "immediately".
	 */
	public static final OSCTimeStamp IMMEDIATE = new OSCTimeStamp(IMMEDIATE_RAW);
	/**
	 * The Java representation of an OSC timestamp with the semantics of
	 * "immediately".
	 */
	public static final Date IMMEDIATE_DATE = new Date(MSB_0_BASE_TIME);
	/**
	 * First value of the upper NTP range for seconds, used on top of {@link #MSB_1_BASE_TIME}.
	 * This value equals {@link Integer#MIN_VALUE}, but is interpreted as an unsigned integer.
	 */
	private static final long SECONDS_RANGE_UPPER_START = 0x80000000L;
	/**
	 * Last value of the lower NTP range for seconds, used on top of {@link #MSB_0_BASE_TIME}.
	 * This value equals {@link Integer#MAX_VALUE}, but is interpreted as an unsigned integer.
	 */
	private static final long SECONDS_RANGE_LOWER_END = 0x7FFFFFFFL;
	/**
	 * Filter for the 32 lower/least-significant bits of a long.
	 */
	private static final long FILTER_LOWER_32 = 0xFFFFFFFFL;
	/**
	 * Filter for the most significant bit in a 32bit value.
	 */
	private static final long FILTER_MSB_32 = 0x80000000L;
	/**
	 * Number of bits for storing the "seconds" value in NTP time.
	 */
	private static final int NTP_SECONDS_BITS = 32;

	public static final Date OSC_RANGE_DATE_MIN
			= new Date(MSB_1_BASE_TIME + (1000L * SECONDS_RANGE_UPPER_START));
	public static final Date OSC_RANGE_DATE_MAX
			= new Date(MSB_0_BASE_TIME + (1000L * SECONDS_RANGE_LOWER_END));

	private static final long serialVersionUID = 1L;

	private final long ntpTime;

	public OSCTimeStamp(final long ntpTime) {
		this.ntpTime = ntpTime;
	}

	/**
	 * Returns the NTP conform time-stamp value.
	 * @return MSB: base, 32 higher bits (including MSB): seconds, 32 lower bits: fraction
	 */
	public long getNtpTime() {
		return ntpTime;
	}

	/**
	 * The raw seconds counter.
	 * @return high-order 32-bits of the ntpTime 64bits value
	 */
	public long getSeconds() {
		return ntpTime >>> NTP_SECONDS_BITS;
	}

	/**
	 * The fraction counter.
	 * Denotes the number of seconds * (1 / 2^32), which allows to specify to a precision
	 * of about 233 pico seconds.
	 * @return lower-order 32-bits of the ntpTime 64bits value
	 */
	public long getFraction() {
		return ntpTime & FILTER_LOWER_32;
	}

	public boolean isImmediate() {
		return ntpTime == IMMEDIATE_RAW;
	}

	@Override
	public boolean equals(final Object other) {

		final boolean equal;
		if (other instanceof OSCTimeStamp) {
			equal = (getNtpTime() == ((OSCTimeStamp) other).getNtpTime());
		} else {
			equal = false;
		}

		return equal;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 89 * hash + (int) (this.ntpTime ^ (this.ntpTime >>> 32));
		return hash;
	}

	@Override
	public int compareTo(final OSCTimeStamp other) {
		return (int) (getNtpTime() - other.getNtpTime());
	}

	@Override
	public OSCTimeStamp clone() throws CloneNotSupportedException {
		return (OSCTimeStamp) super.clone();
	}

	/**
	 * Returns the Java Date closest to this time-stamps value
	 * @return this time-stamps value rounded to the closest full millisecond
	 */
	public Date toDate() {
		return new Date(toJavaTime());
	}

	/**
	 * The most significant bit (MSB) on the seconds field denotes the base to use.
	 * The following text is a quote from RFC-2030 (SNTP v4):
	 * <quote>
	 * If bit 0 is set, the UTC time is in the range 1968-2036 and UTC time
	 * is reckoned from 0h 0m 0s UTC on 1 January 1900. If bit 0 is not set,
	 * the time is in the range 2036-2104 and UTC time is reckoned from
	 * 6h 28m 16s UTC on 7 February 2036.
	 * </quote>
	 */
	private boolean isUsingBase1() {

		final long msb = getSeconds() & FILTER_MSB_32;
		return (msb == FILTER_MSB_32);
	}

	private long evaluateBase() {

		final long baseTime;
		if (isUsingBase1()) {
			baseTime = MSB_1_BASE_TIME;
		} else {
			baseTime = MSB_0_BASE_TIME;
		}
		return baseTime;
	}

	private long toJavaTime() {

		// use round-off on the fractional part to preserve going to lower precision
		final long fractionInMs = Math.round(1000D * getFraction() / 0x100000000L);

		final long seconds = getSeconds();
		final long baseTime = evaluateBase();
		final long secondsInMs = seconds * 1000L;
		return baseTime + secondsInMs + fractionInMs;
	}

	public static OSCTimeStamp valueOf(final long ntpTime) {
		return new OSCTimeStamp(ntpTime);
	}

	public static OSCTimeStamp valueOf(final Date javaTime) {
		return new OSCTimeStamp(javaToNtpTimeStamp(javaTime.getTime()));
	}

	private static String toExtensiveString(final Date date) {
		return "\"" + date.toString() + "\" (time-stamp: " + date.getTime() + ")";
	}

	/**
	 * Converts a Java time-stamp (milliseconds since 1970) to a 64-bit NTP time representation.
	 * This code was copied from the "Apache Jakarta Commons - Net" library,
	 * which is licensed under the
	 * <a href="http://www.apache.org/licenses/LICENSE-2.0.html">ASF 2.0 license</a>.
	 * The original source file can be found
	 * <a href="http://svn.apache.org/viewvc/commons/proper/net/trunk/src/main/java/org/apache/commons/net/ntp/TimeStamp.java?view=co">here</a>.
	 * @param javaTime Java time-stamp, as returned by {@link Date#getTime()}
	 * @return NTP time-stamp representation of the Java time value.
	 */
	private static long javaToNtpTimeStamp(final long javaTime) {

		if ((javaTime < OSC_RANGE_DATE_MIN.getTime())
				|| (javaTime > OSC_RANGE_DATE_MAX.getTime()))
		{
			throw new IllegalArgumentException("Java Date " + toExtensiveString(new Date(javaTime))
					+ " lies outside the NTP time-stamp range, which OSC uses: "
					+ toExtensiveString(OSC_RANGE_DATE_MIN) + " - "
					+ toExtensiveString(OSC_RANGE_DATE_MAX));
		}
		final boolean useBase1 = javaTime < MSB_0_BASE_TIME;
		final long baseTime;
		if (useBase1) {
			baseTime = MSB_1_BASE_TIME;
		} else {
			baseTime = MSB_0_BASE_TIME;
		}
		final long baseTimeAddition = javaTime - baseTime;

		final long seconds = baseTimeAddition / 1000L;
		final long fraction = Math.round(((baseTimeAddition % 1000L) * 0x100000000L) / 1000D);

		// We do not have to do that, because if we use base 1 but the MSB is not yet set,
		// we would be trying to describe a date outside the OSC time-stamp range,
		// for which we already checked earlier.
//		if (useBase1) {
//			seconds |= FILTER_MSB_32; // set MSB if base 1 is used
//		}

		final long ntpTime = seconds << NTP_SECONDS_BITS | fraction;

		return ntpTime;
	}
}
