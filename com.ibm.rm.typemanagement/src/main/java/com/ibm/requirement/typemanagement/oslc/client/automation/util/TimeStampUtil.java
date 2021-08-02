/*******************************************************************************
 * Copyright (c) 2012 - 2013, 2018 IBM Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *
 *    Ralph Schoon - Initial implementation
 *******************************************************************************/
package com.ibm.requirement.typemanagement.oslc.client.automation.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class to allow creation of printable time stamps. Conversion of .
 *
 */
public class TimeStampUtil {

	public static final String SIMPLE_DATE_FORMAT_PATTERN_YYYY_MM_DD_HH_MM_SS_Z = "yyyy/MM/dd HH:mm:ss z";
	public static final String SIMPLE_DATE_FORMAT_PATTERN_YYYY_MM_DD = "yyyy/MM/dd";

	/**
	 * Get a string representation for a timestamp in a specified pattern
	 * 
	 * @param date
	 *            A time stamp data to convert into a string.
	 * @param timeFormatPattern
	 *            A time format pattern or null (which results in a default
	 *            pattern being used)
	 * @return The string representation of the time stamp create with the
	 *         specified format pattern
	 */
	public static String getDate(final Timestamp date, final String timeFormatPattern) {
		String pattern = SIMPLE_DATE_FORMAT_PATTERN_YYYY_MM_DD_HH_MM_SS_Z;
		if (null != timeFormatPattern) {
			pattern = timeFormatPattern;
		}
		SimpleDateFormat sDFormat = new SimpleDateFormat(pattern);
		return sDFormat.format(date);
	}

	/**
	 * @return a current timestamp as string
	 */
	public static String getTimestamp() {
		return getDate(new Timestamp((new Date()).getTime()), null);
	}

	/**
	 * Get the date as a string
	 * 
	 * @param date
	 * @return
	 */
	public static String getDate(Date date) {
		return getDate(new Timestamp(date.getTime()), null);
	}

}
