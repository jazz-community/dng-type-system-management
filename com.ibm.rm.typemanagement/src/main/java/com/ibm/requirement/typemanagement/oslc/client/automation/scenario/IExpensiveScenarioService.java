/*******************************************************************************
 * Copyright (c) 2012 - 2019 IBM Corporation.
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
package com.ibm.requirement.typemanagement.oslc.client.automation.scenario;

/**
 * Interface for a basic Expensive Scenario Service that passes the information
 * required between service calls using a string.
 *
 */
public interface IExpensiveScenarioService {

	/**
	 * Start an expensive scenario.
	 * 
	 * @return The request body that is needed in the stop command
	 * 
	 * @throws Exception
	 */
	public String start() throws Exception;

	/**
	 * Stop the expensive scenario.
	 * 
	 * @param startRequestResponse the response string from the start command
	 * @throws Exception
	 */
	public void stop(final String startRequestResponse) throws Exception;

	/**
	 * @return the scenario name, never null
	 */
	public Object getScenarioName();

}
