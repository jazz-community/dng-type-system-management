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
import org.eclipse.lyo.client.exception.RootServicesException;
import org.eclipse.lyo.client.oslc.OSLCConstants;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.eclipse.lyo.client.oslc.jazz.JazzRootServicesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.requirement.typemanagement.oslc.client.automation.DngTypeSystemManagementConstants;
import com.ibm.requirement.typemanagement.oslc.client.automation.framework.AbstractCommand;
import com.ibm.requirement.typemanagement.oslc.client.automation.util.CsvExportImportInformation;
import com.ibm.requirement.typemanagement.oslc.client.automation.util.CsvUtil;
import com.ibm.requirement.typemanagement.oslc.client.dngcm.DngCmUtil;
import com.ibm.requirement.typemanagement.oslc.client.resources.Changeset;
import com.ibm.requirement.typemanagement.oslc.client.resources.Configuration;
import com.ibm.requirement.typemanagement.oslc.client.resources.DngCmDeliverySession;
import com.ibm.requirement.typemanagement.oslc.client.resources.DngCmTypeSystemImportSession;

/**
 * Use a CSV file as input to import the type system in streams/configurations
 * from another stream/configuration.
 * 
 * https://jazz.net/wiki/bin/view/Main/DNGTypeImport
 *
 *
 */
public class ImportTypeSystemCmd extends AbstractCommand {

	public static final Logger logger = LoggerFactory.getLogger(ImportTypeSystemCmd.class);

	/**
	 * Create new command and give it the name
	 */
	public ImportTypeSystemCmd() {
		super(DngTypeSystemManagementConstants.CMD_IMPORT_TYPE_SYSTEM);
	}

	@Override
	public Options addCommandOptions(Options options) {
		options.addOption(DngTypeSystemManagementConstants.PARAMETER_URL, true,
				DngTypeSystemManagementConstants.PARAMETER_URL_DESCRIPTION);
		options.addOption(DngTypeSystemManagementConstants.PARAMETER_USER, true,
				DngTypeSystemManagementConstants.PARAMETER_USER_ID_DESCRIPTION);
		options.addOption(DngTypeSystemManagementConstants.PARAMETER_PASSWORD, true,
				DngTypeSystemManagementConstants.PARAMETER_PASSWORD_DESCRIPTION);
		options.addOption(DngTypeSystemManagementConstants.PARAMETER_CSV_FILE_PATH, true,
				DngTypeSystemManagementConstants.PARAMETER_CSV_FILE_PATH_DESCRIPTION);
		options.addOption(DngTypeSystemManagementConstants.PARAMETER_CSV_DELIMITER, true,
				DngTypeSystemManagementConstants.PARAMETER_CSV_DELIMITER_DESCRIPTION);
		return options;
	}

	@Override
	public boolean checkParameters(final CommandLine cmd) {
		boolean isValid = true;

		if (!(cmd.hasOption(DngTypeSystemManagementConstants.PARAMETER_URL)
				&& cmd.hasOption(DngTypeSystemManagementConstants.PARAMETER_USER)
				&& cmd.hasOption(DngTypeSystemManagementConstants.PARAMETER_PASSWORD)
				&& cmd.hasOption(DngTypeSystemManagementConstants.PARAMETER_CSV_FILE_PATH))) {

			isValid = false;
		}
		return isValid;
	}

	@Override
	public void printSyntax() {
		logger.info("{}", getCommandName());
		logger.info("\tSyntax : -{} {} -{} {} -{} {} -{} {} -{} {} [ -{} {} ]",
				DngTypeSystemManagementConstants.PARAMETER_COMMAND, getCommandName(),
				DngTypeSystemManagementConstants.PARAMETER_URL,
				DngTypeSystemManagementConstants.PARAMETER_URL_PROTOTYPE,
				DngTypeSystemManagementConstants.PARAMETER_USER,
				DngTypeSystemManagementConstants.PARAMETER_USER_PROTOTYPE,
				DngTypeSystemManagementConstants.PARAMETER_PASSWORD,
				DngTypeSystemManagementConstants.PARAMETER_PASSWORD_PROTOTYPE,
				DngTypeSystemManagementConstants.PARAMETER_CSV_FILE_PATH,
				DngTypeSystemManagementConstants.PARAMETER_CSV_FILE_PATH_PROTOTYPE,
				DngTypeSystemManagementConstants.PARAMETER_CSV_DELIMITER,
				DngTypeSystemManagementConstants.PARAMETER_CSV_DELIMITER_PROTOTYPE);
		logger.info("\tExample: -{} {} -{} {} -{} {} -{} {} -{} {}", DngTypeSystemManagementConstants.PARAMETER_COMMAND,
				getCommandName(), DngTypeSystemManagementConstants.PARAMETER_URL,
				DngTypeSystemManagementConstants.PARAMETER_URL_EXAMPLE, DngTypeSystemManagementConstants.PARAMETER_USER,
				DngTypeSystemManagementConstants.PARAMETER_USER_ID_EXAMPLE,
				DngTypeSystemManagementConstants.PARAMETER_PASSWORD,
				DngTypeSystemManagementConstants.PARAMETER_PASSWORD_EXAMPLE,
				DngTypeSystemManagementConstants.PARAMETER_CSV_FILE_PATH,
				DngTypeSystemManagementConstants.PARAMETER_CSV_FILE_PATH_EXAMPLE);

		logger.info("\tOptional parameter: -{} {}", DngTypeSystemManagementConstants.PARAMETER_CSV_DELIMITER,
				DngTypeSystemManagementConstants.PARAMETER_CSV_DELIMITER_PROTOTYPE);
		logger.info("\tExample optional parameter: -{} {}", DngTypeSystemManagementConstants.PARAMETER_CSV_DELIMITER,
				DngTypeSystemManagementConstants.PARAMETER_CSV_DELIMITER_EXAMPLE);
	}

	@Override
	public boolean execute() {
		boolean result = false;
		boolean totalResult = true;

		// Get all the option values
		String webContextUrl = getCmd().getOptionValue(DngTypeSystemManagementConstants.PARAMETER_URL);
		String user = getCmd().getOptionValue(DngTypeSystemManagementConstants.PARAMETER_USER);
		String passwd = getCmd().getOptionValue(DngTypeSystemManagementConstants.PARAMETER_PASSWORD);
		String csvFilePath = getCmd().getOptionValue(DngTypeSystemManagementConstants.PARAMETER_CSV_FILE_PATH);
		String csvDelimiter = getCmd().getOptionValue(DngTypeSystemManagementConstants.PARAMETER_CSV_DELIMITER);

		try {

			// Login
			JazzRootServicesHelper helper = new JazzRootServicesHelper(webContextUrl, OSLCConstants.OSLC_RM_V2);
			logger.trace("Login");
			String authUrl = webContextUrl.replaceFirst("/rm", "/jts");
			JazzFormAuthClient client = helper.initFormClient(user, passwd, authUrl);

			if (client.login() == HttpStatus.SC_OK) {

				// Get the data
				CsvUtil csv = new CsvUtil();
				if (null != csvDelimiter && csvDelimiter != "") {
					csv.setSeperator(csvDelimiter.charAt(0));
				}
				logger.info("Using csv file '{}'", csvFilePath);
				List<CsvExportImportInformation> configurations = csv.readConfigurations(csvFilePath);
				if (configurations == null) {
					return result;
				}
				for (CsvExportImportInformation exportImportInformation : configurations) {
					logger.info("-----------------------------------------------------------------------------");
					logger.info("Import '{}' from '{}' to '{}' ", exportImportInformation.getProjectAreaName(),
							exportImportInformation.getSource(), exportImportInformation.getTarget());

					// Get the source and the target configuration
					Configuration sourceConfiguration = DngCmUtil.getConfiguration(client,
							exportImportInformation.getSource());
					Configuration targetConfiguration = DngCmUtil.getConfiguration(client,
							exportImportInformation.getTarget());

					// Create the change set as target for the import.
					Boolean operationResult = false;
					Changeset changeSet = new Changeset(client, targetConfiguration);
					if (changeSet.getAbout() == null) {
						logger.info("Failed to create change set as import target.");
						totalResult &= operationResult;
						continue;
					}
					logger.trace("Change set'{}'", changeSet.getAbout().toString());
					Configuration changeSetConfiguration = DngCmUtil.getConfiguration(client,
							changeSet.getAbout().toString());
					if (changeSetConfiguration == null) {
						totalResult &= operationResult;
						logger.info("Failed to create change set as import target.");
						continue;
					}
					// Import the type system changes from the source stream into the change set
					operationResult = DngCmTypeSystemImportSession.performTypeImport(client, sourceConfiguration,
							changeSetConfiguration);
					if (!operationResult) {
						totalResult &= operationResult;
						logger.info("Failed to Import into change set '{}'.",
								changeSetConfiguration.getAbout().toString());
						continue;
					}
					String projectAreaServiceProviderUrl = changeSetConfiguration.getServiceProvider().toString();
					// Deliver the change set with its changes to the target stream
					operationResult = DngCmDeliverySession.performDelivery(client, projectAreaServiceProviderUrl,
							changeSetConfiguration, targetConfiguration);

					if (!operationResult) {
						totalResult &= operationResult;
						logger.info("The delivery has failed or there were no differences to deliver!");
						Boolean deleted = DngCmUtil.discardChangeSet(client, changeSetConfiguration);
						logger.error("Failed to deliver change set '{}' to stream. '{}'. Changeset discarded: '{}'",
								changeSetConfiguration.getAbout().toString(), targetConfiguration.getAbout().toString(),
								deleted.toString());
						continue;
					}
					logger.trace("Result: {}", operationResult.toString());
					totalResult &= operationResult;
				}
				result = totalResult;
				logger.trace("End");
			}
		} catch (RootServicesException re) {
			logger.error("Unable to access the Jazz rootservices document at: " + webContextUrl + "/rootservices", re);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		return result;
	}

}
