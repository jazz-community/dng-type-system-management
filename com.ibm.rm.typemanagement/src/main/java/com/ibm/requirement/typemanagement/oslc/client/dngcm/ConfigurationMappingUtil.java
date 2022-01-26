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
package com.ibm.requirement.typemanagement.oslc.client.dngcm;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.lyo.client.exception.ResourceNotFoundException;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.eclipse.lyo.client.oslc.jazz.JazzRootServicesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.requirement.typemanagement.oslc.client.automation.framework.ContainsStringRule;
import com.ibm.requirement.typemanagement.oslc.client.automation.framework.IRule;
import com.ibm.requirement.typemanagement.oslc.client.automation.util.CsvExportImportInformation;
import com.ibm.requirement.typemanagement.oslc.client.resources.Changeset;
import com.ibm.requirement.typemanagement.oslc.client.resources.Component;
import com.ibm.requirement.typemanagement.oslc.client.resources.Configuration;
import com.ibm.requirement.typemanagement.oslc.client.resources.DngCmDeliverySession;
import com.ibm.requirement.typemanagement.oslc.client.resources.DngCmTypeSystemImportSession;

import net.oauth.OAuthException;

public class ConfigurationMappingUtil {

	public static final Logger logger = LoggerFactory.getLogger(ConfigurationMappingUtil.class);

	/**
	 * Get the source to target mapping for editable configurations for al
	 * project areas based on source and target description tag.
	 * 
	 * @param client
	 * @param helper
	 * @param sourceTag
	 * @param targetTag
	 * @return
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 * @throws ResourceNotFoundException
	 */
	public static List<CsvExportImportInformation> getEditableConfigurationMappingBydescriptionTag(
			JazzFormAuthClient client, JazzRootServicesHelper helper, String sourceTag, String targetTag)
			throws IOException, OAuthException, URISyntaxException, ResourceNotFoundException {
		// Get the URL of the OSLC ChangeManagement catalog
		logger.info("Getting Configurations");
		String cmCatalogUrl = DngCmUtil.getCmServiceProvider(helper);
		if (cmCatalogUrl == null) {
			logger.error("Unable to access the OSLC Configuration Management Provider URL");
			return null;
		}

		// Get the components and the configurations for the components
		Collection<Component> components = DngCmUtil.getComponentsForAllProjectAreas(client, cmCatalogUrl);
		Collection<Configuration> configurations = DngCmUtil.getConfigurationsForComponents(client, components);

		logger.info("Filtering for Configurations");
		IRule sourceRule = new ContainsStringRule(sourceTag);
		IRule targetRule = new ContainsStringRule(targetTag);
		List<CsvExportImportInformation> configurationList = getConfigurationMapping(configurations, sourceRule,
				targetRule);
		if (configurationList == null) {
			logger.info("No valid configuration data found.");
			return null;
		}
		return configurationList;
	}

	/**
	 * Convert the configurations into the mapping information needed for the
	 * export to prepare writing to CSV.
	 * 
	 * We are only interested in streams and the streams description must
	 * contain a tag specified by a rule.
	 * 
	 * @param configurations
	 * @param projectArea
	 * @param sourceRule
	 * @param targetRule
	 * @return
	 * @throws URISyntaxException
	 */
	private static List<CsvExportImportInformation> getConfigurationMapping(Collection<Configuration> configurations,
			IRule sourceRule, IRule targetRule) throws URISyntaxException {
		List<CsvExportImportInformation> configurationList = new ArrayList<CsvExportImportInformation>();
		Configuration source = null;
		for (Configuration config : configurations) {
			if (!config.isStream()) {
				continue;
			}
			if (targetRule.matches(config.getDescription())) {
				configurationList.add(new CsvExportImportInformation(null, config, ""));
			}
			if (sourceRule.matches(config.getDescription())) {
				if (source != null) {
					logger.info(
							"Ambiguous sources found source 1 URI '{}' title '{}' source 2 URI '{}' title '{}' exiting.",
							source.getAbout().toString(), source.getTitle(), config.getAbout().toString(),
							config.getTitle());
					return null;
				}
				source = config;
			}
		}
		if (source != null) {
			for (CsvExportImportInformation csvExportImportInformation : configurationList) {
				csvExportImportInformation.setSource(source.getAbout().toString());
			}
		} else {
			logger.info("No match for source.");
			return null;
		}
		return configurationList;
	}

	/**
	 * Get the source to target mapping for editable configurations for a
	 * project area based on source and target description tag.
	 * 
	 * @param client
	 * @param helper
	 * @param projectAreaName
	 * @param sourceTag
	 * @param targetTag
	 * @return
	 * @throws URISyntaxException
	 * @throws ResourceNotFoundException
	 * @throws IOException
	 * @throws OAuthException
	 */
	public static List<CsvExportImportInformation> getEditableConfigurationMappingForProjectAreaByDescriptionTag(
			JazzFormAuthClient client, JazzRootServicesHelper helper, String projectAreaName, String sourceTag,
			String targetTag) throws URISyntaxException, ResourceNotFoundException, IOException, OAuthException {
		// Get rootservices
		String catalogUrl = helper.getCatalogUrl();
		logger.info("Getting Configurations");

		// Get the OSLC CM Service Provider
		String cmCatalogUrl = DngCmUtil.getCmServiceProvider(helper);
		if (cmCatalogUrl == null) {
			logger.error("Unable to access the OSLC Configuration Management Provider URL");
			return null;
		}

		// Find the OSLC service provider for the project area - assuming the
		// project
		// area is CM enabled
		final ProjectAreaOslcServiceProvider rmProjectAreaOslcServiceProvider = ProjectAreaOslcServiceProvider
				.findProjectAreaOslcServiceProvider(client, catalogUrl, projectAreaName);
		if (rmProjectAreaOslcServiceProvider == null) {
			logger.error("Unable to find project area '{}'");
			return null;
		}
		if (rmProjectAreaOslcServiceProvider.getProjectAreaId() == null) {
			logger.error("Unable to find project area service provider for '{}'", projectAreaName);
			return null;
		}

		// Get the components and the configurations for the components
		Collection<Component> components = DngCmUtil.getComponentsForProjectArea(client, cmCatalogUrl,
				rmProjectAreaOslcServiceProvider.getProjectAreaId());
		Collection<Configuration> configurations = DngCmUtil.getConfigurationsForComponents(client, components);

		logger.info("Filtering for Configurations");
		IRule sourceRule = new ContainsStringRule(sourceTag);
		IRule targetRule = new ContainsStringRule(targetTag);
		List<CsvExportImportInformation> configurationList = getConfigurationMapping(configurations, projectAreaName,
				sourceRule, targetRule);
		if (configurationList == null) {
			logger.info("No valid configuration data found.");
			return null;
		}
		return configurationList;
	}

	/**
	 * Convert the configurations into the mapping information needed for the
	 * export to prepare writing to CSV.
	 * 
	 * We are only interested in streams and the streams description must
	 * contain a tag specified by a rule.
	 * 
	 * @param configurations
	 * @param projectArea
	 * @param sourceRule
	 * @param targetRule
	 * @return
	 * @throws URISyntaxException
	 */
	private static List<CsvExportImportInformation> getConfigurationMapping(Collection<Configuration> configurations,
			String projectArea, IRule sourceRule, IRule targetRule) throws URISyntaxException {
		List<CsvExportImportInformation> configurationList = new ArrayList<CsvExportImportInformation>();
		Configuration source = null;
		for (Configuration config : configurations) {
			if (!config.isStream()) {
				continue;
			}
			if (targetRule.matches(config.getDescription())) {
				configurationList.add(new CsvExportImportInformation(null, config, projectArea));
			}
			if (sourceRule.matches(config.getDescription())) {
				if (source != null) {
					logger.info(
							"Ambiguous sources found source 1 URI '{}' title '{}' source 2 URI '{}' title '{}' exiting.",
							source.getAbout().toString(), source.getTitle(), config.getAbout().toString(),
							config.getTitle());
					return null;
				}
				source = config;
			}
		}
		if (source != null) {
			for (CsvExportImportInformation csvExportImportInformation : configurationList) {
				csvExportImportInformation.setSource(source.getAbout().toString());
			}
		} else {
			logger.info("No match for source.");
			return null;
		}
		return configurationList;
	}

	/**
	 * Get the editable configurations (Streams) for a project area
	 * 
	 * @param client
	 * @param helper
	 * @param projectAreaName
	 * @return
	 * @throws URISyntaxException
	 * @throws ResourceNotFoundException
	 * @throws IOException
	 * @throws OAuthException
	 */
	public static List<CsvExportImportInformation> getEditableConfigurationsForProjectArea(JazzFormAuthClient client,
			JazzRootServicesHelper helper, String projectAreaName)
			throws URISyntaxException, ResourceNotFoundException, IOException, OAuthException {
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
		Collection<Configuration> configurations = DngCmUtil.getConfigurationsForComponents(client, components);
		logger.info("Filtering for Streams");
		List<CsvExportImportInformation> configurationList = getStreams(configurations, projectAreaName);
		return configurationList;
	}

	/**
	 * Convert the configurations into the information needed for the export to
	 * prepare writing to CSV.
	 * 
	 * @param configurations
	 * @param projectArea
	 * @return
	 * @throws URISyntaxException
	 */
	private static List<CsvExportImportInformation> getStreams(Collection<Configuration> configurations,
			String projectArea) throws URISyntaxException {
		List<CsvExportImportInformation> configurationList = new ArrayList<CsvExportImportInformation>();
		for (Configuration target : configurations) {
			if (target.isStream()) {
				configurationList.add(new CsvExportImportInformation(null, target, projectArea));
			}
		}
		return configurationList;
	}

	/**
	 * Import Type System changes based on a mapping.
	 * 
	 * @param client
	 * @param configurations
	 * @return
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 * @throws ResourceNotFoundException
	 */
	public static boolean importConfigurations(JazzFormAuthClient client,
			List<CsvExportImportInformation> configurations)
			throws IOException, OAuthException, URISyntaxException, ResourceNotFoundException {
		boolean totalResult = true;
		for (CsvExportImportInformation exportImportInformation : configurations) {
			logger.info("-----------------------------------------------------------------------------");
			logger.info("Import from '{}' to '{}' ", exportImportInformation.getProjectAreaName(),
					exportImportInformation.getSource(), exportImportInformation.getTarget());

			// Get the source and the target configuration
			Configuration sourceConfiguration = DngCmUtil.getConfiguration(client, exportImportInformation.getSource());
			Configuration targetConfiguration = DngCmUtil.getConfiguration(client, exportImportInformation.getTarget());

			// Create the change set as target for the import.
			Boolean operationResult = false;
			Changeset changeSet = new Changeset(client, targetConfiguration);
			if (changeSet.getAbout() == null) {
				logger.info("Failed to create change set as import target.");
				totalResult &= operationResult;
				continue;
			}
			logger.trace("Change set'{}'", changeSet.getAbout().toString());
			Configuration changeSetConfiguration = DngCmUtil.getConfiguration(client, changeSet.getAbout().toString());
			if (changeSetConfiguration == null) {
				totalResult &= operationResult;
				logger.info("Failed to create change set as import target.");
				continue;
			}
			// Import the type system changes from the source stream into the
			// change set
			operationResult = DngCmTypeSystemImportSession.performTypeImport(client, sourceConfiguration,
					changeSetConfiguration);
			if (!operationResult) {
				totalResult &= operationResult;
				logger.info("Failed to Import into change set '{}'.", changeSetConfiguration.getAbout().toString());
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
		return totalResult;
	}

	/**
	 * Deliver Type System changes based on a mapping.
	 * 
	 * @param client
	 * @param configurations
	 * @return
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 * @throws ResourceNotFoundException
	 */
	public static boolean deliverConfigurations(JazzFormAuthClient client,
			List<CsvExportImportInformation> configurations)
			throws IOException, OAuthException, URISyntaxException, ResourceNotFoundException {
		boolean delivery = true;
		for (CsvExportImportInformation exportImportInformation : configurations) {
			logger.info("-----------------------------------------------------------------------------");
			logger.info("Deliver from '{}' to '{}' ", exportImportInformation.getSource(),
					exportImportInformation.getTarget());

			// Get the source and the target configuration
			Configuration sourceConfiguration = DngCmUtil.getConfiguration(client, exportImportInformation.getSource());
			Configuration targetConfiguration = DngCmUtil.getConfiguration(client, exportImportInformation.getTarget());
			String projectAreaServiceProviderUrl = targetConfiguration.getServiceProvider().toString();
			Changeset changeSet = new Changeset(client, targetConfiguration);
			// Deliver
			Boolean deliverresult = DngCmDeliverySession.performDelivery(client, projectAreaServiceProviderUrl,
					sourceConfiguration, targetConfiguration);
			logger.trace("Result: {}", deliverresult.toString());
			if (!deliverresult) {
				logger.info("The delivery has failed or there were no differences to deliver!");
			}
			delivery &= deliverresult;
		}
		return delivery;
	}

}
