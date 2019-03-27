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
package com.ibm.requirement.typemanagement.oslc.client.automation.commands;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.requirement.typemanagement.oslc.client.automation.DngTypeSystemManagementConstants;
import com.ibm.requirement.typemanagement.oslc.client.automation.framework.AbstractCommand;
import com.ibm.requirement.typemanagement.oslc.client.automation.framework.ICommand;

/**
 * This class represents a minimal sample for a custom command.
 * 
 * To extend the tool with a new command, you must implement {@link ICommand }.
 * 
 * The easiest way to do this is to create a subclass of {@link AbstractCommand}
 * and implement the abstract methods.
 * 
 * To finally enable this command, add this to {@link CommandFactory} in the
 * constructor like below
 * 
 * {@code 
  
 	private CommandFactory() {
 		super();
		put(new ExportConfigurationsCmd());
		put(new ImportTypeSystemCmd()); put(new DeliverTypeSystemCmd()); put(new
 * SampleCommand()); } }
 * 
 * to finalize the integration.
 */
public class SampleCommandCmd extends AbstractCommand implements ICommand {

	public static final Logger logger = LoggerFactory.getLogger(SampleCommandCmd.class);

	/**
	 * Constructor, set the command name which will be used as option value for the
	 * command option. The name is used in the UIs and the option parser.
	 */
	public SampleCommandCmd() {
		super(DngTypeSystemManagementConstants.CMD_SAMPLE);
	}

	/**
	 * Method to add the options this command requires.
	 */
	@Override
	public Options addCommandOptions(Options options) {
		// Add Options required for the command
		//
		// Example code
		options.addOption(DngTypeSystemManagementConstants.PARAMETER_SAMPLE_OPTION, true,
				DngTypeSystemManagementConstants.PARAMETER_SAMPLE_OPTION_DESCRIPTION);
		options.addOption(DngTypeSystemManagementConstants.PARAMETER_SAMPLE_OPTION_OPT, true,
				DngTypeSystemManagementConstants.PARAMETER_SAMPLE_OPTION_OPT_DESCRIPTION);
		return options;
	}

	/**
	 * Method to check if the required options/parameters required to perform the
	 * command are available.
	 */
	@Override
	public boolean checkParameters(CommandLine cmd) {
		// Check for required options
		boolean isValid = true;

		if (!cmd.hasOption(DngTypeSystemManagementConstants.PARAMETER_SAMPLE_OPTION)) {
			isValid = false;
		}
		return isValid;
	}

	/**
	 * Method to print the syntax in case of missing options.
	 */
	@Override
	public void printSyntax() {
		// Print syntax hint for the command
		//
		// Example code
		logger.info("{}", getCommandName());
		logger.info("\n\tA sample command that can be used as template for adding custom commands.");
		logger.info("\n\tSyntax : -{} {} -{} {} [ -{} {} ]", DngTypeSystemManagementConstants.PARAMETER_COMMAND,
				getCommandName(), DngTypeSystemManagementConstants.PARAMETER_SAMPLE_OPTION,
				DngTypeSystemManagementConstants.PARAMETER_SAMPLE_OPTION_PROTOTYPE,
				DngTypeSystemManagementConstants.PARAMETER_SAMPLE_OPTION_OPT,
				DngTypeSystemManagementConstants.PARAMETER_SAMPLE_OPTION_OPT_PROTOTYPE);
		logger.info("\tExample: -{} {} -{} {}", DngTypeSystemManagementConstants.PARAMETER_COMMAND, getCommandName(),
				DngTypeSystemManagementConstants.PARAMETER_SAMPLE_OPTION,
				DngTypeSystemManagementConstants.PARAMETER_SAMPLE_OPTION_EXAMPLE);
		logger.info("\tOptional parameter: -{} {}", DngTypeSystemManagementConstants.PARAMETER_SAMPLE_OPTION_OPT,
				DngTypeSystemManagementConstants.PARAMETER_SAMPLE_OPTION_OPT_PROTOTYPE);
		logger.info("\tExample optional parameter: -{} {}",
				DngTypeSystemManagementConstants.PARAMETER_SAMPLE_OPTION_OPT,
				DngTypeSystemManagementConstants.PARAMETER_SAMPLE_OPTION_OPT_EXAMPLE);
	}

	/**
	 * The main method that executes the behavior of this command.
	 */
	@SuppressWarnings("unused")
	@Override
	public boolean execute() {
		boolean result = false;
		// Execute the code
		// Get all the option values
		String mandatoryOption = getCmd().getOptionValue(DngTypeSystemManagementConstants.PARAMETER_SAMPLE_OPTION);
		String optionalOption = getCmd().getOptionValue(DngTypeSystemManagementConstants.PARAMETER_SAMPLE_OPTION_OPT);

		/**
		 * TODO: Your code goes here
		 * 
		 */

		return result;
	}
}
