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

import org.apache.commons.cli.Options;

/**
 * Interface that commands must implement.
 *
 */
public interface ICommand {

	/**
	 * @return the command name of the current command
	 */
	public String getCommandName();

	/**
	 * The command is supposed to log info its syntax.
	 */
	public void printSyntax();

	/**
	 * Method to run the command
	 * 
	 * @param options Options to add the specific options for a command
	 * @param args    the arguments of this call
	 * @return true if command was successful, false otherwise.
	 */
	public boolean run(Options options, final String[] args);

}
