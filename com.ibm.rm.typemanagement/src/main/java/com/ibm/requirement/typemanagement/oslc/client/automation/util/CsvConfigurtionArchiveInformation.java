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

import java.net.URISyntaxException;

import com.ibm.requirement.typemanagement.oslc.client.resources.Configuration;
import com.opencsv.bean.CsvBindByName;

/**
 * This class carries the information that is stored in a CSV file or read from
 * it. Annotations are used for the import and export process.
 * 
 * @see http://opencsv.sourceforge.net/
 *
 */
public class CsvConfigurtionArchiveInformation {

	@CsvBindByName
	private String configuration;
	/**
	 * The preferred order of columns for human usage
	 * 
	 * @return
	 */
	public static String[] getColumnMapping() {
		return new String[] { "configuration" };
	}

	public CsvConfigurtionArchiveInformation() {
		super();
	}

	@SuppressWarnings("deprecation")
	public CsvConfigurtionArchiveInformation(final Configuration configuration) throws URISyntaxException {
		super();
	}
	
	public String getConfiguration() {
		return this.configuration;
	}
}
