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
package com.ibm.requirement.typemanagement.oslc.client.automation.framework;

/**
 * Simple rule matching a sting containing a substring
 *
 */
public class ContainsStringRule implements IRule {

	private final String contained;

	public ContainsStringRule(final String contained) {
		super();
		this.contained = contained;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.requirement.typemanagement.oslc.client.automation.framework.
	 * IRule# matches(java.lang.Object)
	 */
	@Override
	public boolean matches(final Object value) {
		if (value == null) {
			return false;
		}
		if (this.contained == null) {
			return false;
		}
		if (value instanceof String) {
			String compare = (String) value;
			boolean result = compare.contains(this.contained);
			return result;
		}
		return false;
	}
}
