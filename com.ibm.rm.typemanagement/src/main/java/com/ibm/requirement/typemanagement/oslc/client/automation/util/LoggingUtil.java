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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Some convenient tooling for logging
 *
 */
public class LoggingUtil {

	public static final String OFF = "OFF";
	public static final String FATAL = "FATAL";
	public static final String ERROR = "ERROR";
	public static final String WARN = "WARN";
	public static final String INFO = "INFO";
	public static final String DEBUG = "DEBUG";
	public static final String TRACE = "TRACE";
	public static final String ALL = "ALL";

	/**
	 * Can be used to change the logging level dynamically.
	 * 
	 * @param level
	 *            the level that should be used.
	 */
	public static void setLoggingLevel(final String level) {
		Logger logger4j = Logger.getRootLogger();
		logger4j.setLevel(Level.toLevel(level));
	}

}
