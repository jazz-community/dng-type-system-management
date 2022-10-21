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

import java.util.Collection;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.requirement.typemanagement.oslc.client.automation.framework.ICommand;

/**
 * Factory class dealing with commands. A new command has to be added to the
 * constructor.
 *
 */
public class CommandFactory {

	public static final Logger logger = LoggerFactory.getLogger(CommandFactory.class);
	private HashMap<String, ICommand> commandMap = new HashMap<String, ICommand>();

	/**
	 * Creates the list of supported commands
	 */
	@SuppressWarnings("deprecation")
	private CommandFactory() {
		super();
		put(new ExportConfigurationsCmd());
		put(new ExportConfigurationsByDescriptionCmd());
		put(new ExportAllConfigurationsByDescriptionCmd());
		put(new ImportTypeSystemCmd());
		put(new DeliverTypeSystemCmd());
		put(new ImportTypeSystemByDescriptionCmd());
		put(new DeliverTypeSystemByDescriptionCmd());
//		put(new ArchiveConfigurationCmd());
		put(new ArchiveConfigurationsCmd());
		put(new AnalyzeConfigurationsCmd());
		// Enable sample command
		// put(new SampleCommandCmd());
	}

	/**
	 * @return the factory
	 */
	public static CommandFactory INSTANCE() {
		return new CommandFactory();
	}

	/**
	 * Add a command to the list of supported commands
	 * 
	 * @param cmd
	 */
	private void put(final ICommand cmd) {
		commandMap.put(cmd.getCommandName(), cmd);
	}

	/**
	 * Print the syntax for all supported commands
	 */
	public void printCommandSyntax() {
		Collection<ICommand> commands = commandMap.values();
		for (ICommand iCommand : commands) {
			iCommand.printSyntax();
			logger.info("\n");
		}
	}

	/**
	 * @return the commandMap, never null
	 */
	public HashMap<String, ICommand> getCommandMap() {
		return commandMap;
	}

	/**
	 * Get a command by its name
	 * 
	 * @param commandName
	 * @return command or null
	 */
	public ICommand getCommand(final String commandName) {
		return commandMap.get(commandName);
	}

}
