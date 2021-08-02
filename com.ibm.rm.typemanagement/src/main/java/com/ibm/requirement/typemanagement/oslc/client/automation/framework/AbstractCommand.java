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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class representing the basic workflow of a command.
 */
public abstract class AbstractCommand implements ICommand {

	public static final Logger logger = LoggerFactory.getLogger(AbstractCommand.class);
	private CommandLine cmd = null;
	private String commandName;

	/**
	 * @param commandName
	 *            name of the command
	 */
	public AbstractCommand(final String commandName) {
		super();
		this.commandName = commandName;
	}

	/**
	 * @return the parsed commandLine
	 */
	public CommandLine getCmd() {
		return cmd;
	}

	/**
	 * @return the name of the command. This is used to find the command that
	 *         needs to be called.
	 */
	public String getCommandName() {
		return this.commandName;
	}

	/**
	 * Execute the workflow
	 * 
	 * @return true if the the execution succeeded.
	 */
	public boolean run(Options options, final String[] args) {
		try {
			options = addCommandOptions(options);
			// Parse the command line
			CommandLineParser cliParser = new GnuParser();
			this.cmd = cliParser.parse(options, args);
			if (!checkParameters(cmd)) {
				printSyntax();
				return false;
			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return execute();
	}

	/**
	 * Add the parser options for the subclass. Method to be implemented in
	 * subclasses.
	 * 
	 * @param options
	 * @return
	 */
	public abstract Options addCommandOptions(Options options);

	/**
	 * Validate the parameters for the command. Method to be implemented in
	 * subclasses.
	 * 
	 * @param cmd
	 * @return true if the required parameters are available.
	 */
	public abstract boolean checkParameters(final CommandLine cmd);

	/**
	 * Used to print the syntax of a command. Method to be implemented in
	 * subclasses.
	 */
	public abstract void printSyntax();

	/**
	 * Execute the command. Implement this method to create the desired
	 * behavior. Method to be implemented in subclasses.
	 * 
	 * @return
	 */
	public abstract boolean execute();

}
