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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.http.HttpStatus;
import org.eclipse.lyo.client.exception.ResourceNotFoundException;
import org.eclipse.lyo.client.oslc.OSLCConstants;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.eclipse.lyo.client.oslc.jazz.JazzRootServicesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.requirement.typemanagement.oslc.client.automation.DngTypeSystemManagementConstants;
import com.ibm.requirement.typemanagement.oslc.client.automation.framework.AbstractCommand;
import com.ibm.requirement.typemanagement.oslc.client.automation.scenario.ExpensiveScenarioService;
import com.ibm.requirement.typemanagement.oslc.client.automation.scenario.IExpensiveScenarioService;
import com.ibm.requirement.typemanagement.oslc.client.automation.util.TimeStampUtil;
import com.ibm.requirement.typemanagement.oslc.client.dngcm.DngCmUtil;
import com.ibm.requirement.typemanagement.oslc.client.dngcm.ProjectAreaOslcServiceProvider;
import com.ibm.requirement.typemanagement.oslc.client.resources.Component;
import com.ibm.requirement.typemanagement.oslc.client.resources.Configuration;

import net.oauth.OAuthException;

/**
 * Exports the streams/configurations of a project area to CSV/Excel.
 *
 */
public class AnalyzeConfigurationsCmd extends AbstractCommand {

	private static final int REPORT_PROGRESS_SIZE = 10;
	public static final Logger logger = LoggerFactory.getLogger(AnalyzeConfigurationsCmd.class);
	private int itemcount = 0;
	private int maxcount = -1;
	private PrintWriter fWriter = null;
	private String fDelimiter = ";";

	/**
	 * Create new command and give it the name
	 */
	public AnalyzeConfigurationsCmd() {
		super(DngTypeSystemManagementConstants.CMD_ANALYZE_CONFIGURATIONS);
	}

	@Override
	public Options addCommandOptions(Options options) {
		options.addOption(DngTypeSystemManagementConstants.PARAMETER_URL, true,
				DngTypeSystemManagementConstants.PARAMETER_URL_DESCRIPTION);
		options.addOption(DngTypeSystemManagementConstants.PARAMETER_USER, true,
				DngTypeSystemManagementConstants.PARAMETER_USER_ID_DESCRIPTION);
		options.addOption(DngTypeSystemManagementConstants.PARAMETER_PASSWORD, true,
				DngTypeSystemManagementConstants.PARAMETER_PASSWORD_DESCRIPTION);
		options.addOption(DngTypeSystemManagementConstants.PARAMETER_PROJECT_AREA, true,
				DngTypeSystemManagementConstants.PARAMETER_PROJECT_AREA_DESCRIPTION);
		options.addOption(DngTypeSystemManagementConstants.PARAMETER_CSV_FILE_PATH, true,
				DngTypeSystemManagementConstants.PARAMETER_CSV_FILE_PATH_DESCRIPTION);
		options.addOption(DngTypeSystemManagementConstants.PARAMETER_CSV_DELIMITER, true,
				DngTypeSystemManagementConstants.PARAMETER_CSV_DELIMITER_DESCRIPTION);
		options.addOption(DngTypeSystemManagementConstants.PARAMETER_PROCESS_ITEMS_LIMIT, true,
				DngTypeSystemManagementConstants.PARAMETER_PROCESS_ITEMS_LIMIT_DESCRIPTION);
		return options;
	}

	@Override
	public boolean checkParameters(final CommandLine cmd) {
		boolean isValid = true;

		if (!(cmd.hasOption(DngTypeSystemManagementConstants.PARAMETER_URL)
				&& cmd.hasOption(DngTypeSystemManagementConstants.PARAMETER_USER)
				&& cmd.hasOption(DngTypeSystemManagementConstants.PARAMETER_PASSWORD)
				&& cmd.hasOption(DngTypeSystemManagementConstants.PARAMETER_PROJECT_AREA)
				&& cmd.hasOption(DngTypeSystemManagementConstants.PARAMETER_CSV_FILE_PATH))) {
			isValid = false;
		}
		return isValid;
	}

	@Override
	public void printSyntax() {
		logger.info("{}", getCommandName());
		logger.info(
				"\n\tFinds all editable configurations of a project area and exports the information into a CSV file.");
		logger.info("\n\tSyntax : -{} {} -{} {} -{} {} -{} {} -{} {} -{} {} [ -{} {} ]",
				DngTypeSystemManagementConstants.PARAMETER_COMMAND, getCommandName(),
				DngTypeSystemManagementConstants.PARAMETER_URL,
				DngTypeSystemManagementConstants.PARAMETER_URL_PROTOTYPE,
				DngTypeSystemManagementConstants.PARAMETER_USER,
				DngTypeSystemManagementConstants.PARAMETER_USER_PROTOTYPE,
				DngTypeSystemManagementConstants.PARAMETER_PASSWORD,
				DngTypeSystemManagementConstants.PARAMETER_PASSWORD_PROTOTYPE,
				DngTypeSystemManagementConstants.PARAMETER_PROJECT_AREA,
				DngTypeSystemManagementConstants.PARAMETER_PROJECT_AREA_PROTOTYPE,
				DngTypeSystemManagementConstants.PARAMETER_CSV_FILE_PATH,
				DngTypeSystemManagementConstants.PARAMETER_CSV_FILE_PATH_PROTOTYPE,
				DngTypeSystemManagementConstants.PARAMETER_CSV_DELIMITER,
				DngTypeSystemManagementConstants.PARAMETER_CSV_DELIMITER_PROTOTYPE);
		logger.info("\tExample: -{} {} -{} {} -{} {} -{} {} -{} {} -{} {}",
				DngTypeSystemManagementConstants.PARAMETER_COMMAND, getCommandName(),
				DngTypeSystemManagementConstants.PARAMETER_URL, DngTypeSystemManagementConstants.PARAMETER_URL_EXAMPLE,
				DngTypeSystemManagementConstants.PARAMETER_USER,
				DngTypeSystemManagementConstants.PARAMETER_USER_ID_EXAMPLE,
				DngTypeSystemManagementConstants.PARAMETER_PASSWORD,
				DngTypeSystemManagementConstants.PARAMETER_PASSWORD_EXAMPLE,
				DngTypeSystemManagementConstants.PARAMETER_PROJECT_AREA,
				DngTypeSystemManagementConstants.PARAMETER_PROJECT_AREA_EXAMPLE,
				DngTypeSystemManagementConstants.PARAMETER_CSV_FILE_PATH,
				DngTypeSystemManagementConstants.PARAMETER_CSV_FILE_PATH_EXAMPLE);

		logger.info("\tOptional parameter: -{} {}", DngTypeSystemManagementConstants.PARAMETER_CSV_DELIMITER,
				DngTypeSystemManagementConstants.PARAMETER_CSV_DELIMITER_PROTOTYPE);
		logger.info("\tExample optional parameter: -{} {}", DngTypeSystemManagementConstants.PARAMETER_CSV_DELIMITER,
				DngTypeSystemManagementConstants.PARAMETER_CSV_DELIMITER_EXAMPLE);

		logger.info("\tOptional parameter: -{} {}", DngTypeSystemManagementConstants.PARAMETER_PROCESS_ITEMS_LIMIT,
				DngTypeSystemManagementConstants.PARAMETER_PROCESS_ITEMS_LIMIT_PROTOTYPE);
		logger.info("\tExample optional parameter: -{} {}", DngTypeSystemManagementConstants.PARAMETER_PROCESS_ITEMS_LIMIT,
				DngTypeSystemManagementConstants.PARAMETER_PROCESS_ITEMS_LIMIT_EXAMPLE);

	}

	@Override
	public boolean execute() {
		boolean result = false;

		// Get all the option values
		String webContextUrl = getCmd().getOptionValue(DngTypeSystemManagementConstants.PARAMETER_URL);
		String user = getCmd().getOptionValue(DngTypeSystemManagementConstants.PARAMETER_USER);
		String passwd = getCmd().getOptionValue(DngTypeSystemManagementConstants.PARAMETER_PASSWORD);
		String projectAreaName = getCmd().getOptionValue(DngTypeSystemManagementConstants.PARAMETER_PROJECT_AREA);
		String csvFilePath = getCmd().getOptionValue(DngTypeSystemManagementConstants.PARAMETER_CSV_FILE_PATH);
		String csvDelimiter = getCmd().getOptionValue(DngTypeSystemManagementConstants.PARAMETER_CSV_DELIMITER);
		String maxProcessItems = getCmd()
				.getOptionValue(DngTypeSystemManagementConstants.PARAMETER_PROCESS_ITEMS_LIMIT);
		if (maxProcessItems != null) {
			maxcount = Integer.valueOf(maxProcessItems);
		}

		fDelimiter = csvDelimiter;
		JazzFormAuthClient client = null;
		IExpensiveScenarioService scenarioService = null;
		String scenarioInstance = null;
		try {

			// Login
			JazzRootServicesHelper helper = new JazzRootServicesHelper(webContextUrl, OSLCConstants.OSLC_RM_V2);
			logger.trace("Login");
			String authUrl = webContextUrl.replaceFirst("/rm", "/jts");
			client = helper.initFormClient(user, passwd, authUrl);

			if (client.login() == HttpStatus.SC_OK) {
				scenarioService = ExpensiveScenarioService.createScenarioService(client, webContextUrl,
						getCommandName());
				scenarioInstance = ExpensiveScenarioService.startScenario(scenarioService);
				createWriter(csvFilePath);
				getHeader();

				analyzeConfigurations(client, helper, projectAreaName);

				fWriter.flush();
				fWriter.close();
				result = true;
			}
		} catch (Exception e) {
			// e.printStackTrace();
			logger.error(e.getMessage(), e);
		} finally {
			ExpensiveScenarioService.stopScenario(scenarioService, scenarioInstance);
		}
		return result;
	}

	private Object analyzeConfigurations(JazzFormAuthClient client, JazzRootServicesHelper helper,
			String projectAreaName) throws ResourceNotFoundException, URISyntaxException, IOException, OAuthException {
		logger.info("Analyzing Configurations");
		// Get the URL of the OSLC ChangeManagement catalog
		String catalogUrl = helper.getCatalogUrl();
		logger.info("Getting Configurations");
		String cmCatalogUrl = DngCmUtil.getCmServiceProvider(helper);
		if (cmCatalogUrl == null) {
			logger.error("Unable to access the OSLC Configuration Management Provider URL");
			return null;
		}

		final ProjectAreaOslcServiceProvider rmProjectAreaOslcServiceProvider = ProjectAreaOslcServiceProvider
				.findProjectAreaOslcServiceProvider(client, catalogUrl, projectAreaName);
		if (rmProjectAreaOslcServiceProvider == null) {
			logger.error("Unable to find project '{}'", projectAreaName);
			return null;
		}
		if (rmProjectAreaOslcServiceProvider.getProjectAreaId() == null) {
			logger.error("Unable to find project area service provider for '{}'", projectAreaName);
			return null;
		}

		// Get the components and the configurations for the components
		Collection<Component> components = DngCmUtil.getComponentsForProjectArea(client, cmCatalogUrl,
				rmProjectAreaOslcServiceProvider.getProjectAreaId());
		analyzeComponents(client, components);

		return null;
	}

	private boolean analyzeComponents(JazzFormAuthClient client, Collection<Component> components)
			throws IOException, OAuthException, URISyntaxException {
		// Collection<Configuration> configurations =
		// DngCmUtil.getConfigurationsForComponents(client, components);
		for (Iterator<Component> iterator = components.iterator(); iterator.hasNext();) {
			Component component = (Component) iterator.next();
			if (!analyzeComponent(client, component)) {
				return false; // cap
			}
		}
		return true;
	}

	private boolean analyzeComponent(JazzFormAuthClient client, Component component)
			throws IOException, OAuthException, URISyntaxException {

		String message = componentAsString(component, "\t");
		logger.info(message);
		Collection<Configuration> configurations = DngCmUtil.getComponentConfigurations(client, component);
		for (Configuration configuration : configurations) {
			if (!doKeepGoing()) {
				return false;
			}
			String confMessage = configurationAsString(configuration, "\t\t");
			writeColumn(component, configuration);
			// DngCmUtil.createChangeSet(client, configuration);
			logger.info(confMessage);
		}
		return true;
	}

	private void writeColumn(Component component, Configuration configuration) {
		StringBuilder sb = new StringBuilder();
		sb.append(getString(configuration.getIdentifier()));
		sb.append(fDelimiter);
		sb.append("\""+getConfigurationType(configuration)+"\"");
		sb.append(fDelimiter);
		sb.append(getString(configuration.getAbout()));
		sb.append(fDelimiter);
		sb.append(getString(configuration.getTitle()));
		sb.append(fDelimiter);
		sb.append(getString(configuration.getDescription()));
		sb.append(fDelimiter);
		sb.append(getString(configuration.getCreated()));
		sb.append(fDelimiter);
		sb.append(getString(configuration.getCreator()));
		sb.append(fDelimiter);
		sb.append(getString(configuration.getWasDerivedFrom()));
		sb.append(fDelimiter);
		sb.append(getString(configuration.getBaselineOfStream()));
		sb.append(fDelimiter);
		sb.append(getString(configuration.getPreviousBaseline()));
		sb.append(fDelimiter);

		sb.append(getString(component.getAbout()));
		sb.append(fDelimiter);
		sb.append(getString(component.getTitle()));
		sb.append(fDelimiter);
		sb.append(getString(component.getDescription()));
		sb.append(fDelimiter);
		sb.append(getString(component.getProjectArea()));
		sb.append("\n");
		fWriter.write(sb.toString());
	}

	private String getHeader() {

		StringBuilder sb = new StringBuilder();

		sb.append(getString("Configuration ID"));
		sb.append(fDelimiter);
		sb.append(getString("Configuration Type"));
		sb.append(fDelimiter);
		sb.append(getString("Configuration URI"));
		sb.append(fDelimiter);
		sb.append(getString("Configuration Title"));
		sb.append(fDelimiter);
		sb.append(getString("Configuration Description"));
		sb.append(fDelimiter);
		sb.append(getString("Configuration Created"));
		sb.append(fDelimiter);
		sb.append(getString("Configuration Creator"));
		sb.append(fDelimiter);
		sb.append(getString("Configuration Was Derived From"));
		sb.append(fDelimiter);
		sb.append(getString("Configuration Baseline Of Stream"));
		sb.append(fDelimiter);
		sb.append(getString("Configuration Previous Baseline"));
		sb.append(fDelimiter);
		sb.append(getString("Component URI"));
		sb.append(fDelimiter);
		sb.append(getString("Component Title"));
		sb.append(fDelimiter);
		sb.append(getString("Component Description"));
		sb.append(fDelimiter);
		sb.append(getString("Component ProjectArea"));
		sb.append("\n");
		fWriter.write(sb.toString());
		return sb.toString();
	}

	private String getString(Date date) {
		if (date == null) {
			return "\"\"";
		}
		return getString(TimeStampUtil.getDate(date));
	}

	private String getString(URI uri) {
		if (uri == null) {
			return "\"\"";
		}
		return getString(uri.toString());
	}

	private String getString(String value) {
		if (value != null) {
			return "\""+removeDelimiter(value)+"\"";
		}
		return "\"\"";
	}

	private String removeDelimiter(String value) {
		if (value.contains(fDelimiter)) {
			logger.info("Delimiter detected in \'" + value + "'");
			return value.replace(fDelimiter, "-");
		}
		return value;
	}

	private void analyzeConfiguration(JazzFormAuthClient client, Configuration configuration)
			throws IOException, OAuthException, URISyntaxException {
		configuration.getAbout();
	}

	private String configurationAsString(Configuration configuration) {
		return configurationAsString(configuration, null);
	}

	private String configurationAsString(Configuration configuration, String prefix) {
		if (prefix == null) {
			prefix = "";
		}
		String confIdentifier = configuration.getIdentifier();
		// String confType_ = configuration.getType().toString();
		URI baselineOfStream = configuration.getBaselineOfStream();
		URI overrides = configuration.getOverrides();
		URI prevBaseline = configuration.getPreviousBaseline();
		URI wasDerivedFrom = configuration.getWasDerivedFrom();

		String confTitle = configuration.getTitle();
		String confDescription = configuration.getDescription();
		String confURI = configuration.getAbout().toString();
		String confType = getConfigurationType(configuration);

		String confMessage = prefix + " " + confIdentifier + "\t" + confType + " '" + confTitle + "'"; // "'
																								// '"
																								// +
																								// confDescription
																								// +
																								// "'
																								// "
																								// +
																								// confURI
																								// +
																								// "";
		return confMessage;
	}

	private String getConfigurationType(Configuration configuration) {
		String confType = "unknown";
		if (configuration.isStream()) {
			confType = "Stream";
		} else if (configuration.isBaseline()) {
			confType = "Baseline";
		} else if (configuration.isChangeset()) {
			confType = "Changeset";
		}
		return confType;
	}

	private String componentAsString(Component component) {
		return componentAsString(component, null);
	}

	private String componentAsString(Component component, String prefix) {
		if (prefix == null) {
			prefix = "";
		}
		// String compIdentifier = component.getIdentifier();
		String compTitle = component.getTitle();
		String compURI = component.getAbout().toString();
		String compPaURI = component.getProjectArea().toString();
		String compServiceProvider = component.getServiceProvider().toString();
		String compDescription = component.getDescription();
		String message = prefix + "Component '" + compTitle + " '" + compDescription + "' " + compURI + "";
		return message;
	}

	private boolean doKeepGoing() {
		int showProgress = ++itemcount % REPORT_PROGRESS_SIZE;
		if (showProgress == 0) {
			logger.info(TimeStampUtil.getTimestamp() + " Processed items:" + Integer.valueOf(itemcount));
		}
		if (itemcount < maxcount) {
			return true;
		}
		// If maxcount smaller than 0 we execute limitless
		if (maxcount < 0) {
			return true;
		}
		logger.info("Exceeded processed items limit. Items processed: " + Integer.valueOf(itemcount));
		return false;
	}

	private void createWriter(String fileName) throws FileNotFoundException, UnsupportedEncodingException {
		fWriter = new PrintWriter(new File(fileName), "UTF-8");
	}

}
