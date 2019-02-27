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

import com.ibm.requirement.typemanagement.oslc.client.automation.commands.ExportConfigurationsByDescription;
import com.ibm.requirement.typemanagement.oslc.client.automation.commands.ExportConfigurationsCmd;
import com.ibm.requirement.typemanagement.oslc.client.automation.framework.ContainsStringRule;
import com.ibm.requirement.typemanagement.oslc.client.automation.framework.IRule;
import com.ibm.requirement.typemanagement.oslc.client.automation.util.CsvExportImportInformation;
import com.ibm.requirement.typemanagement.oslc.client.resources.Component;
import com.ibm.requirement.typemanagement.oslc.client.resources.Configuration;

import net.oauth.OAuthException;

public class ConfigurationMappingUtil {

	public static final Logger logger = LoggerFactory.getLogger(ConfigurationMappingUtil.class);

	/**
	 * Get the source to target mapping for editable configurations for al project
	 * areas based on source and target description tag.
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
	public static List<CsvExportImportInformation> getMappingBydescriptionTag(JazzFormAuthClient client,
			JazzRootServicesHelper helper, String sourceTag, String targetTag)
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
	 * Convert the configurations into the mapping information needed for the export
	 * to prepare writing to CSV.
	 * 
	 * We are only interested in streams and the streams description must contain a
	 * tag specified by a rule.
	 * 
	 * @param configurations
	 * @param projectArea
	 * @param sourceRule
	 * @param targetRule
	 * @return
	 * @throws URISyntaxException
	 */
	public static List<CsvExportImportInformation> getConfigurationMapping(Collection<Configuration> configurations,
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
	 * Get the source to target mapping for editable configurations for a project
	 * area based on source and target description tag.
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
		ExportConfigurationsByDescription.logger.info("Getting Configurations");

		// Get the OSLC CM Service Provider
		String cmCatalogUrl = DngCmUtil.getCmServiceProvider(helper);
		if (cmCatalogUrl == null) {
			ExportConfigurationsByDescription.logger
					.error("Unable to access the OSLC Configuration Management Provider URL");
			return null;
		}

		// Find the OSLC service provider for the project area - assuming the project
		// area is CM enabled
		final ProjectAreaOslcServiceProvider rmProjectAreaOslcServiceProvider = ProjectAreaOslcServiceProvider
				.findProjectAreaOslcServiceProvider(client, catalogUrl, projectAreaName);
		if (rmProjectAreaOslcServiceProvider.getProjectAreaId() == null) {
			ExportConfigurationsByDescription.logger.error("Unable to find project area service provider for '{}'",
					projectAreaName);
			return null;
		}

		// Get the components and the configurations for the components
		Collection<Component> components = DngCmUtil.getComponentsForProjectArea(client, cmCatalogUrl,
				rmProjectAreaOslcServiceProvider.getProjectAreaId());
		Collection<Configuration> configurations = DngCmUtil.getConfigurationsForComponents(client, components);

		ExportConfigurationsByDescription.logger.info("Filtering for Configurations");
		IRule sourceRule = new ContainsStringRule(sourceTag);
		IRule targetRule = new ContainsStringRule(targetTag);
		List<CsvExportImportInformation> configurationList = getConfigurationMapping(configurations, projectAreaName,
				sourceRule, targetRule);
		if (configurationList == null) {
			ExportConfigurationsByDescription.logger.info("No valid configuration data found.");
			return null;
		}
		return configurationList;
	}

	/**
	 * Convert the configurations into the mapping information needed for the export
	 * to prepare writing to CSV.
	 * 
	 * We are only interested in streams and the streams description must contain a
	 * tag specified by a rule.
	 * 
	 * @param configurations
	 * @param projectArea
	 * @param sourceRule
	 * @param targetRule
	 * @return
	 * @throws URISyntaxException
	 */
	public static List<CsvExportImportInformation> getConfigurationMapping(Collection<Configuration> configurations,
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
					ExportConfigurationsByDescription.logger.info(
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
			ExportConfigurationsByDescription.logger.info("No match for source.");
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
		ExportConfigurationsCmd.logger.info("Getting Configurations");
		String cmCatalogUrl = DngCmUtil.getCmServiceProvider(helper);
		if (cmCatalogUrl == null) {
			ExportConfigurationsCmd.logger.error("Unable to access the OSLC Configuration Management Provider URL");
			return null;
		}

		final ProjectAreaOslcServiceProvider rmProjectAreaOslcServiceProvider = ProjectAreaOslcServiceProvider
				.findProjectAreaOslcServiceProvider(client, catalogUrl, projectAreaName);
		if (rmProjectAreaOslcServiceProvider.getProjectAreaId() == null) {
			ExportConfigurationsCmd.logger.error("Unable to find project area service provider for '{}'",
					projectAreaName);
			return null;
		}

		// Get the components and the configurations for the components
		Collection<Component> components = DngCmUtil.getComponentsForProjectArea(client, cmCatalogUrl,
				rmProjectAreaOslcServiceProvider.getProjectAreaId());
		Collection<Configuration> configurations = DngCmUtil.getConfigurationsForComponents(client, components);
		ExportConfigurationsCmd.logger.info("Filtering for Streams");
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
	public static List<CsvExportImportInformation> getStreams(Collection<Configuration> configurations,
			String projectArea) throws URISyntaxException {
		List<CsvExportImportInformation> configurationList = new ArrayList<CsvExportImportInformation>();
		for (Configuration target : configurations) {
			if (target.isStream()) {
				configurationList.add(new CsvExportImportInformation(null, target, projectArea));
			}
		}
		return configurationList;
	}

}
