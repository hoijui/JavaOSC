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
 * This represents an OSC <i>Timetag</i>.
 * It uses the NTP (Network Time Protocol) time-tag format,
 * and supports a different time range (smaller) and resolution (more precise)
 * then {@link java.util.Date}.
 * See <a href="http://opensoundcontrol.org/node/3/#timetags">
 * the OSC specification for Timetags</a>
 * and <a href="https://en.wikipedia.org/wiki/Network_Time_Protocol#Timestamps">
 * the NTP time-stamp documentation on Wikipedia</a> for specification details.
 * OSC <i>Timetag</i> aswell as <code>Date</code> are time-zone agnostic,
 * though <code>Date</code> might use the default {@link java.util.Locale}
 * in some cases, like when formatting as a <code>String</code>.
 * The epoch 0 starts in 1900 and ends in 2036.
 * Dates before or after the end of this first epoch are represented with
 * a rolled over (the same) value range again, which means one needs to
 * define the epoch to convert to,
 * when converting from the short epoch OSC Timetag to a bigger range one like
 * Java <code>Date</code>.
 * We do this with the
 * {@link com.illposed.osc.argument.handler.DateTimeStampArgumentHandler#PROP_NAME_EPOCH_INDICATOR_TIME}
 * property.
 * By default, it uses the epoch we are currently in.
 * TODO When advancing to Java 8, we should introduce <tt>toInstant()</tt>
 *   and <tt>valueOf(Instant)</tt> methods (see {@link java.time.Instant}),
 *   as it covers the range of the OSC time format, and nearly covers the precision
 *   (1 nanosecond accuracy instead of 233 picoseconds).
 * TODO Introduce 128bit version of this class (64bit seconds, 64bit fraction)
 *   as described by NTPv4.
 */
public class OSCTimeTag64 implements Cloneable, Serializable, Comparable<OSCTimeTag64> {

	/**
	 * OSC epoch length in milliseconds.
	 * An epoch is the maximum range of a 64bit OSC Timetag,
	 * which is (uint32_max + 1) * 1000 milliseconds.
	 */
	public static final long EPOCH_LENGTH_JAVA_TIME = 0x100000000L * 1000L;
	/**
	 * Start of the first epoch expressed in Java time
	 * (as used by {@link java.util.Date#Date(long)}
	 * and {@link java.util.Date#getTime()}).
	 * This is "1-Jan-1900 @ 00:00:00", and we use UTC as time-zone.
	 * Dates before this can not be represented with an OSC Timetag.
	 */
	public static final long EPOCH_START_JAVA_TIME_0 = -2208992400000L;
	/**
	 * Start of the current epoch expressed in Java time.
	 */
	public static final long EPOCH_START_JAVA_TIME_CURRENT
			= findEpochStartJavaTime(new Date().getTime());
	/**
	 * The OSC time-tag with the semantics of "immediately"/"now".
	 */
	public static final long IMMEDIATE_RAW = 0x1L;
	/**
	 * The OSC time-tag with the semantics of "immediately"/"now".
	 */
	public static final OSCTimeTag64 IMMEDIATE = valueOf(IMMEDIATE_RAW);
	/**
	 * The Java representation of an OSC time-tag with the semantics of
	 * "immediately"/"now".
	 */
	public static final Date IMMEDIATE_DATE = IMMEDIATE.toDate(new Date(EPOCH_START_JAVA_TIME_0));
	/**
	 * Filter for the 32 lower/least-significant bits of a long.
	 */
	private static final long FILTER_LOWER_32 = 0xFFFFFFFFL;
	/**
	 * Number of bits for storing the "seconds" value in NTP time.
	 */
	private static final int NTP_SECONDS_BITS = 32;
	private static final long serialVersionUID = 1L;

	/**
	 * This Timetags value as specified by the OSC protocol standard.
	 * It is treated as a 64bit unsigned integer,
	 * with the higher-order 32bit representing the whole seconds
	 * from the beginning of the epoch,
	 * and the lower-order 32bit representing the fractions of a second
	 * from the beginning of the last full second.
	 */
	private final long ntpTime;

	protected OSCTimeTag64(final long ntpTime) {
		this.ntpTime = ntpTime;
	}

	/**
	 * Returns the OSC/NTP conform <i>Timetag</i> value.
	 * @return 64bits:
	 *   32 higher bits (including MSB): seconds,
	 *   32 lower bits: fraction (of a second)
	 */
	public long getNtpTime() {
		return ntpTime;
	}

	/**
	 * Returns the number of whole seconds from the start of the epoch
	 * of this time-tag.
	 * @return high-order 32-bit unsigned value representing the "seconds" part
	 *   of this OSC <i>Timetag</i>
	 */
	public long getSeconds() {
		return ntpTime >>> NTP_SECONDS_BITS;
	}

	/**
	 * Returns the fraction of a second from the start of the epoch + seconds
	 * of this time-tag.
	 * Denotes a number of seconds * (1 / 2^32),
	 * which allows to specify to a precision of about 233 pico seconds.
	 * @return lower-order 32-bit unsigned value representing the "fraction"
	 *   part of this OSC <i>Timetag</i>
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
		if (other instanceof OSCTimeTag64) {
			equal = (getNtpTime() == ((OSCTimeTag64) other).getNtpTime());
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
	public int compareTo(final OSCTimeTag64 other) {
		return (int) (getNtpTime() - other.getNtpTime());
	}

	@Override
	public OSCTimeTag64 clone() throws CloneNotSupportedException {
		return (OSCTimeTag64) super.clone();
	}

	/**
	 * Returns the Java date closest to this time-tags value.
	 * @param epochIndicatorTime the resulting date will be
	 *   in the same OSC epoch as this Java date-stamp
	 * @return this time-tags value rounded to the closest full millisecond
	 */
	public Date toDate(final long epochIndicatorTime) {
		return new Date(toJavaTime(epochIndicatorTime));
	}

	/**
	 * Returns the Java date closest to this time-tags value.
	 * @param epochIndicator the resulting date will be
	 *   in the same OSC epoch as this
	 * @return this time-tags value rounded to the closest full millisecond
	 */
	public Date toDate(final Date epochIndicator) {
		return (epochIndicator == null)
				? toDate()
				: toDate(epochIndicator.getTime());
	}

	/**
	 * Returns the Java date closest to this time-tags value.
	 * The resulting date will be in the current OSC epoch.
	 * @return this time-tags value rounded to the closest full millisecond
	 */
	private Date toDate() {
		return new Date(toJavaTimeInEpoch(EPOCH_START_JAVA_TIME_CURRENT));
	}

	private static long findEpochStartJavaTime(final long javaTime) {

		final long epochIndex = (javaTime - EPOCH_START_JAVA_TIME_0) / EPOCH_LENGTH_JAVA_TIME;
		return EPOCH_START_JAVA_TIME_0 + (epochIndex * EPOCH_LENGTH_JAVA_TIME);
	}

	private long toJavaTimeInEpoch(final long epochStart) {

		final long secondsInMs = getSeconds() * 1000L;
		// use round-off on the fractional part to preserve going to lower precision
		final long fractionInMs = Math.round(1000D * getFraction() / 0x100000000L);

		return epochStart + (secondsInMs | fractionInMs);
	}

	private long toJavaTime(final Long epochIndicatorTime) {

		final long epochStart;
		if (epochIndicatorTime == null) {
			epochStart = EPOCH_START_JAVA_TIME_CURRENT;
		} else {
			epochStart = findEpochStartJavaTime(epochIndicatorTime);
		}

		return toJavaTimeInEpoch(epochStart);
	}

	private static long toNtpTimeTag(final long seconds, final long fraction) {
		return seconds << NTP_SECONDS_BITS | fraction;
	}

	public static OSCTimeTag64 valueOf(final long ntpTime) {
		return new OSCTimeTag64(ntpTime);
	}

	public static OSCTimeTag64 valueOf(final long seconds, final long fraction) {
		return new OSCTimeTag64(toNtpTimeTag(seconds, fraction));
	}

	public static OSCTimeTag64 valueOf(final Date javaTime) {
		return new OSCTimeTag64(javaToNtpTimeStamp(javaTime.getTime()));
	}

	@Override
	public String toString() {
		return toDate().toString();
	}

	/**
	 * Converts a Java time-stamp (milliseconds since 1970)
	 * to a 64-bit OSC time representation.
	 * This code was copied from the "Apache Jakarta Commons - Net" library,
	 * which is licensed under the
	 * <a href="http://www.apache.org/licenses/LICENSE-2.0.html">ASF 2.0 license
	 * </a>.
	 * The original source file can be found
	 * <a href="http://svn.apache.org/viewvc/commons/proper/net/trunk/src/main/java/org/apache/commons/net/ntp/TimeStamp.java?view=co">
	 * here</a>.
	 * @param javaTime Java time-stamp, as returned by {@link Date#getTime()}
	 * @return OSC time-tag representation of the Java time value.
	 */
	private static long javaToNtpTimeStamp(final long javaTime) {

		final long epochStart = findEpochStartJavaTime(javaTime);
		final long millisecInEpoch = javaTime - epochStart;
		final long seconds = millisecInEpoch / 1000L;
		final long fraction = Math.round(((millisecInEpoch % 1000L) * 0x100000000L) / 1000D);
		final long ntpTime = toNtpTimeTag(seconds, fraction);

		return ntpTime;
	}
}
