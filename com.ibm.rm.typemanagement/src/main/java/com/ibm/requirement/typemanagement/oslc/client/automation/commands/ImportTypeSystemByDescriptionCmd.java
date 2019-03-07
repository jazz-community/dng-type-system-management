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

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.http.HttpStatus;
import org.eclipse.lyo.client.oslc.OSLCConstants;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.eclipse.lyo.client.oslc.jazz.JazzRootServicesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.requirement.typemanagement.oslc.client.automation.DngTypeSystemManagementConstants;
import com.ibm.requirement.typemanagement.oslc.client.automation.framework.AbstractCommand;
import com.ibm.requirement.typemanagement.oslc.client.automation.util.CsvExportImportInformation;
import com.ibm.requirement.typemanagement.oslc.client.automation.util.ExpensiveScenarioService;
import com.ibm.requirement.typemanagement.oslc.client.automation.util.IExpensiveScenarioService;
import com.ibm.requirement.typemanagement.oslc.client.dngcm.ConfigurationMappingUtil;

/**
 * Exports the streams/configurations of a project area to CSV/Excel.
 *
 */
public class ImportTypeSystemByDescriptionCmd extends AbstractCommand {

	public static final Logger logger = LoggerFactory.getLogger(ImportTypeSystemByDescriptionCmd.class);

	/**
	 * Create new command and give it the name
	 */
	public ImportTypeSystemByDescriptionCmd() {
		super(DngTypeSystemManagementConstants.CMD_IMPORT_TYPESYSTEM_BY_DESCRIPTION);
	}

	@Override
	public Options addCommandOptions(Options options) {
		options.addOption(DngTypeSystemManagementConstants.PARAMETER_URL, true,
				DngTypeSystemManagementConstants.PARAMETER_URL_DESCRIPTION);
		options.addOption(DngTypeSystemManagementConstants.PARAMETER_USER, true,
				DngTypeSystemManagementConstants.PARAMETER_USER_ID_DESCRIPTION);
		options.addOption(DngTypeSystemManagementConstants.PARAMETER_PASSWORD, true,
				DngTypeSystemManagementConstants.PARAMETER_PASSWORD_DESCRIPTION);
		options.addOption(DngTypeSystemManagementConstants.PARAMETER_SOURCE_TAG, true,
				DngTypeSystemManagementConstants.PARAMETER_SOURCE_TAG_DESCRIPTION);
		options.addOption(DngTypeSystemManagementConstants.PARAMETER_TARGET_TAG, true,
				DngTypeSystemManagementConstants.PARAMETER_TARGET_TAG_DESCRIPTION);
		return options;
	}

	@Override
	public boolean checkParameters(final CommandLine cmd) {
		boolean isValid = true;

		if (!(cmd.hasOption(DngTypeSystemManagementConstants.PARAMETER_URL)
				&& cmd.hasOption(DngTypeSystemManagementConstants.PARAMETER_USER)
				&& cmd.hasOption(DngTypeSystemManagementConstants.PARAMETER_PASSWORD)
				&& cmd.hasOption(DngTypeSystemManagementConstants.PARAMETER_SOURCE_TAG)
				&& cmd.hasOption(DngTypeSystemManagementConstants.PARAMETER_TARGET_TAG))) {
			isValid = false;
		}
		return isValid;
	}

	@Override
	public void printSyntax() {
		logger.info("{}", getCommandName());
		logger.info("\n\tUses string tags in the description to identify a source stream and one or many target streams. For each target stream, it imports the type system changes of the source stream into a new changes set and delivers the change to the target streams.");
		logger.info("\n\tSyntax : -{} {} -{} {} -{} {} -{} {} -{} {} -{} {}",
				DngTypeSystemManagementConstants.PARAMETER_COMMAND, getCommandName(),
				DngTypeSystemManagementConstants.PARAMETER_URL,
				DngTypeSystemManagementConstants.PARAMETER_URL_PROTOTYPE,
				DngTypeSystemManagementConstants.PARAMETER_USER,
				DngTypeSystemManagementConstants.PARAMETER_USER_PROTOTYPE,
				DngTypeSystemManagementConstants.PARAMETER_PASSWORD,
				DngTypeSystemManagementConstants.PARAMETER_PASSWORD_PROTOTYPE,
				DngTypeSystemManagementConstants.PARAMETER_SOURCE_TAG,
				DngTypeSystemManagementConstants.PARAMETER_TAG_PROTOTYPE,
				DngTypeSystemManagementConstants.PARAMETER_TARGET_TAG,
				DngTypeSystemManagementConstants.PARAMETER_TAG_PROTOTYPE);
		logger.info("\tExample: -{} {} -{} {} -{} {} -{} {} -{} {} -{} {}",
				DngTypeSystemManagementConstants.PARAMETER_COMMAND, getCommandName(),
				DngTypeSystemManagementConstants.PARAMETER_URL, DngTypeSystemManagementConstants.PARAMETER_URL_EXAMPLE,
				DngTypeSystemManagementConstants.PARAMETER_USER,
				DngTypeSystemManagementConstants.PARAMETER_USER_ID_EXAMPLE,
				DngTypeSystemManagementConstants.PARAMETER_PASSWORD,
				DngTypeSystemManagementConstants.PARAMETER_PASSWORD_EXAMPLE,
				DngTypeSystemManagementConstants.PARAMETER_SOURCE_TAG,
				DngTypeSystemManagementConstants.PARAMETER_SOURCE_TAG_EXAMPLE,
				DngTypeSystemManagementConstants.PARAMETER_TARGET_TAG,
				DngTypeSystemManagementConstants.PARAMETER_TARGET_TAG_EXAMPLE);
	}

	@Override
	public boolean execute() {
		boolean result = false;

		// Get all the option values
		String webContextUrl = getCmd().getOptionValue(DngTypeSystemManagementConstants.PARAMETER_URL);
		String user = getCmd().getOptionValue(DngTypeSystemManagementConstants.PARAMETER_USER);
		String passwd = getCmd().getOptionValue(DngTypeSystemManagementConstants.PARAMETER_PASSWORD);
		String sourceTag = getCmd().getOptionValue(DngTypeSystemManagementConstants.PARAMETER_SOURCE_TAG);
		String targetTag = getCmd().getOptionValue(DngTypeSystemManagementConstants.PARAMETER_TARGET_TAG);

		JazzFormAuthClient client = null;
		IExpensiveScenarioService scenarioService=null;
		String scenarioInstance=null;
		try {

			// Login
			JazzRootServicesHelper helper = new JazzRootServicesHelper(webContextUrl, OSLCConstants.OSLC_RM_V2);
			logger.trace("Login");
			String authUrl = webContextUrl.replaceFirst("/rm", "/jts");
			client = helper.initFormClient(user, passwd, authUrl);

			if (client.login() == HttpStatus.SC_OK) {

				scenarioService = new ExpensiveScenarioService(webContextUrl, getCommandName()+"Scenario");
				scenarioInstance = scenarioService.start(client);
				List<CsvExportImportInformation> configurations = ConfigurationMappingUtil
						.getEditableConfigurationMappingBydescriptionTag(client, helper, sourceTag, targetTag);
				if (configurations != null) {
					result = ConfigurationMappingUtil.importConfigurations(client, configurations);
					logger.trace("End");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		} finally {
			if(scenarioInstance!=null) {
				try {
					scenarioService.stop(client, scenarioInstance);
				} catch (Exception e) {
					logger.trace("Failed to stop resource intensive scenario '{}'", scenarioInstance);
				}
			}
		}
		return result;
	}

}
