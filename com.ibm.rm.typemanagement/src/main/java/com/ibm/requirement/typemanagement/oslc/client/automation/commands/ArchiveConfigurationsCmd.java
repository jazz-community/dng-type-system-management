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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

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
import com.ibm.requirement.typemanagement.oslc.client.automation.framework.ICommand;
import com.ibm.requirement.typemanagement.oslc.client.automation.scenario.ExpensiveScenarioService;
import com.ibm.requirement.typemanagement.oslc.client.automation.scenario.IExpensiveScenarioService;
import com.ibm.requirement.typemanagement.oslc.client.automation.util.CsvConfigurtionArchiveInformation;
import com.ibm.requirement.typemanagement.oslc.client.automation.util.CsvUtil;
import com.ibm.requirement.typemanagement.oslc.client.automation.util.TimeStampUtil;
import com.ibm.requirement.typemanagement.oslc.client.dngcm.CallStatus;
import com.ibm.requirement.typemanagement.oslc.client.dngcm.DependentConfigurationsApi;
import com.ibm.requirement.typemanagement.oslc.client.dngcm.InternalConfigurationArchiveApi;
import com.ibm.requirement.typemanagement.oslc.client.dngcm.IsConfigurationArchivedApi;
import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

/**
 * Use a CSV file as input to deliver the changes to a type system in a
 * stream/configuration to other streams/configurations.
 * 
 * @see https://jazz.net/wiki/bin/view/Main/DNGConfigManagement
 * @see https://jazz.net/wiki/bin/view/Main/DNGTaskTracker
 *
 */
public class ArchiveConfigurationsCmd extends AbstractCommand implements ICommand {

	public static final Logger logger = LoggerFactory.getLogger(ArchiveConfigurationsCmd.class);
	char seperator = CSVWriter.DEFAULT_SEPARATOR;
	char quoteChar = CSVWriter.DEFAULT_QUOTE_CHARACTER;
	char escapeChar = CSVWriter.NO_ESCAPE_CHARACTER;
	String lineEnd = CSVWriter.DEFAULT_LINE_END;
	Integer archiveCount = 0;

	public void setCsvInfo() {

	}

	public void setCsvInfo(char seperator, char quoteChar) {
		this.seperator = seperator;
		this.quoteChar = quoteChar;
	}

	public void setCsvInfo(char seperator, char quoteChar, char escapeChar) {
		this.seperator = seperator;
		this.quoteChar = quoteChar;
		this.escapeChar = escapeChar;
	}

	public char getSeperator() {
		return seperator;
	}

	public void setSeperator(char seperator) {
		this.seperator = seperator;
	}

	public char getQuoteChar() {
		return quoteChar;
	}

	public void setQuoteChar(char quoteChar) {
		this.quoteChar = quoteChar;
	}

	public char getEscapeChar() {
		return escapeChar;
	}

	public void setEscapeChar(char escapeChar) {
		this.escapeChar = escapeChar;
	}

	public String getLineEnd() {
		return lineEnd;
	}

	/**
	 * Create new command and give it the name
	 */
	public ArchiveConfigurationsCmd() {
		super(DngTypeSystemManagementConstants.CMD_ARCHIVE_CONFIGURATIONS);
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
		options.addOption(DngTypeSystemManagementConstants.PARAMETER_SIMULATION, true,
				DngTypeSystemManagementConstants.PARAMETER_SIMULATION_DESCRIPTION);
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
		logger.info("\n\tReads a CSV file with a list of configuration and archives the configurations from the list.");
		logger.info("\n\tSyntax : -{} {} -{} {} -{} {} -{} {} -{} {} [ -{} {} ] [ -{} {} ]",
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
				DngTypeSystemManagementConstants.PARAMETER_CSV_DELIMITER_PROTOTYPE,
				DngTypeSystemManagementConstants.PARAMETER_SIMULATION,
				DngTypeSystemManagementConstants.PARAMETER_SIMULATION_PROTOTYPE);
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
		logger.info("\tOptional parameter: -{} {}", DngTypeSystemManagementConstants.PARAMETER_SIMULATION,
				DngTypeSystemManagementConstants.PARAMETER_SIMULATION_PROTOTYPE);
		logger.info("\tExample optional parameter: -{} {}", DngTypeSystemManagementConstants.PARAMETER_SIMULATION,
				DngTypeSystemManagementConstants.PARAMETER_SIMULATION_EXAMPLE);
	}

	@Override
	public boolean execute() {

		boolean result = false;
		boolean doSimulation = false;
		// Get all the option values
		String webContextUrl = getCmd().getOptionValue(DngTypeSystemManagementConstants.PARAMETER_URL);
		String user = getCmd().getOptionValue(DngTypeSystemManagementConstants.PARAMETER_USER);
		String passwd = getCmd().getOptionValue(DngTypeSystemManagementConstants.PARAMETER_PASSWORD);
		String csvFilePath = getCmd().getOptionValue(DngTypeSystemManagementConstants.PARAMETER_CSV_FILE_PATH);
		String csvDelimiter = getCmd().getOptionValue(DngTypeSystemManagementConstants.PARAMETER_CSV_DELIMITER);
		doSimulation = getCmd().hasOption(DngTypeSystemManagementConstants.PARAMETER_SIMULATION);

		
		JazzFormAuthClient client = null;
		IExpensiveScenarioService scenarioService = null;
		String scenarioInstance = null;
		try {
			// Import the data
			CsvUtil csv = new CsvUtil();
			if (null != csvDelimiter && csvDelimiter != "") {
				csv.setSeperator(csvDelimiter.charAt(0));
			}

			logger.info(TimeStampUtil.getTimestamp() + " Using csv file '{}'", csvFilePath);
			// Login
			JazzRootServicesHelper helper = new JazzRootServicesHelper(webContextUrl, OSLCConstants.OSLC_RM_V2);
			logger.trace("Login");
			String authUrl = webContextUrl.replaceFirst("/rm", "/jts");
			client = helper.initFormClient(user, passwd, authUrl);

			if (client.login() == HttpStatus.SC_OK) {
				scenarioService = ExpensiveScenarioService.createScenarioService(client, webContextUrl,
						getCommandName());
				scenarioInstance = ExpensiveScenarioService.startScenario(scenarioService);
				// Get the URL of the OSLC ChangeManagement catalog
				String cmCatalogUrl = helper.getCatalogUrl();
				if (cmCatalogUrl == null) {
					logger.error("Unable to access the OSLC Configuration Management Provider URL for '{}'",
							webContextUrl);
					return result;
				}

				result = archiveConfigurations(client, csvFilePath, doSimulation);
				logger.info(TimeStampUtil.getTimestamp() + " Archived configurations: {}", archiveCount.toString());
				logger.trace("End");
			}
		} catch (RootServicesException re) {
			logger.error("Unable to access the Jazz rootservices document at: " + webContextUrl + "/rootservices", re);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		} finally {
			ExpensiveScenarioService.stopScenario(scenarioService, scenarioInstance);
		}
		return result;
	}

	public boolean archiveConfigurations(JazzFormAuthClient client, String filePath, boolean doSimulation) {
		int suppressArchive = 0;
		if(doSimulation) {
			suppressArchive = 3;
		}
		int count = 0;
		// set the archive count to 0.
		archiveCount=0;
		boolean result = true;
		try {
			FileReader reader = new FileReader(filePath);
			CsvToBean<CsvConfigurtionArchiveInformation> csvToBean = new CsvToBeanBuilder<CsvConfigurtionArchiveInformation>(
					reader).withType(CsvConfigurtionArchiveInformation.class).withSeparator(getSeperator())
					.withQuoteChar(getQuoteChar()).withEscapeChar(getEscapeChar()).build();
			for (CsvConfigurtionArchiveInformation configurationToArchive : csvToBean) {
				count++;
				result &= archiveConfiguration(client, configurationToArchive, count, 0, 0, suppressArchive);

				// Runs multiple tests against the same configuration
				//result &= archiveConfigurationTest(client, configurationToArchive, count, 0, 0, 0);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			logger.error("File not found '{}'", filePath);
		} catch (IOException e) {
			logger.error("IOException " + e.getMessage());
		}
		return result;
	}
	
	public boolean archiveConfigurationTest(JazzFormAuthClient client, CsvConfigurtionArchiveInformation configuration,
			int count, int isArchivedError, int hasDependentError, int doArchiveError) {
		boolean result = true;
		result &= archiveConfiguration(client, configuration, count, 0, 0, 3);
		result &= archiveConfiguration(client, configuration, count, 1, 1, 3);
		result &= archiveConfiguration(client, configuration, count, 2, 2, 3);
		result &= archiveConfiguration(client, configuration, count, 0, 1, 1);
		result &= archiveConfiguration(client, configuration, count, 0, 2, 2);
		result &= archiveConfiguration(client, configuration, count, 0, 0, 3);
		result &= archiveConfiguration(client, configuration, count, 0, 0, 1);
		result &= archiveConfiguration(client, configuration, count, 0, 0, 2);
				
		return result;
	}
	
	public boolean archiveConfiguration(JazzFormAuthClient client, CsvConfigurtionArchiveInformation configuration,
			int count, int isArchivedError, int hasDependentError, int doArchiveError) {
		boolean result = false;
		String conf = configuration.getConfiguration();

		// Step 1, check if the configuration is already archived.
		String isArchivedErrormessage = "";
		String dependentErrorMessage = "";
		String archiveErrorMessage = "";
		CallStatus isArchived = IsConfigurationArchivedApi.isArchived(client, conf, isArchivedError);
		String archived = "".concat(Integer.toString(count)).concat(" \tConfiguration: ").concat(conf).concat("\t ");

		// Detect if the configuration is already archived
		if (isArchived.callFailed()) {
			isArchivedErrormessage = isArchivedErrormessage.concat(" Query archived call failed - ")
					.concat(isArchived.getMessage());
			result = false;
		} else if (isArchived.getCallResult()) {
			archived = archived.concat("\t is already archived");
			result = true;
		} else {
			archived = archived.concat("\t is not archived");

			// Step 2, check if the configuration can be archived
			CallStatus hasDependentConfigs = DependentConfigurationsApi.hasNoDependentConfiguration(client, conf, hasDependentError);
			if (hasDependentConfigs.callFailed()) {
				dependentErrorMessage = dependentErrorMessage.concat("\t Dependent configuration call failed - ")
						.concat(isArchived.getMessage());
				result = false;
			} else if (hasDependentConfigs.getCallResult()) {
				archived = archived.concat("\t has no dependencies ");		
				CallStatus archive = InternalConfigurationArchiveApi.archiveWithDescendants(client, conf, doArchiveError);
				if (archive.callFailed()) {
					archiveErrorMessage = archiveErrorMessage.concat("\t Archiving call failed - ")
							.concat(archive.getMessage());
					result = false;
				} else {
					result = true;
					archived = archived.concat("\t was archived ");
					archiveCount++;
				}
			} else {
				archived = archived
						.concat(""
								+ ""
								+ "\t has dependencies " + hasDependentConfigs.getNoResults() + "\t can not be archived");
				result = true;
			}
		}
		if(isArchivedErrormessage != "" ) {
			archived = archived.concat(" Test for is archived: ").concat(isArchivedErrormessage);
		}
		if(dependentErrorMessage != "" ) {
			archived = archived.concat(" Test for has dependencies: ").concat(dependentErrorMessage);
		}
		if(archiveErrorMessage != "" ) {
			archived = archived.concat(" Execute archive configuration: ").concat(isArchivedErrormessage);
		}
		logger.info(archived);
		return result;
	}

}
